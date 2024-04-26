package kill.online.helper.zeroTier.service

import android.os.ParcelFileDescriptor
import android.util.Log
import com.zerotier.sdk.Node
import com.zerotier.sdk.ResultCode
import com.zerotier.sdk.VirtualNetworkConfig
import com.zerotier.sdk.VirtualNetworkFrameListener
import com.zerotier.sdk.util.StringUtils
import kill.online.helper.zeroTier.util.IPPacketUtils
import kill.online.helper.zeroTier.util.InetAddressUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and


class TunTapAdapter(private val ztService: ZeroTierOneService, private val networkId: Long) :
    VirtualNetworkFrameListener {
    private var node: Node? = null
    private val routeMap = HashMap<Route, Long>()
    private var arpTable: ARPTable? = ARPTable()
    private var ndpTable: NDPTable? = NDPTable()
    private var `in`: FileInputStream? = null
    private var out: FileOutputStream? = null
    val isRunning: Boolean
        get() {
            val thread = receiveThread ?: return false
            return thread.isAlive
        }
    private var receiveThread: Thread? = null
    private var vpnSocket: ParcelFileDescriptor? = null
    private var onHandleIPPacket: (packetData: ByteArray) -> ByteArray = { it }
    private fun addMulticastRoutes() {}
    fun setNode(node: Node?) {
        this.node = node
        try {
            val multicastAddress = InetAddress.getByName("224.224.224.224")
            node?.let {
                val result: ResultCode = it
                    .multicastSubscribe(networkId, multicastAddressToMAC(multicastAddress))

                if (result != ResultCode.RESULT_OK) {
                    Log.e(TAG, "Error when calling multicastSubscribe: $result")
                }
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, e.toString(), e)
        }
    }

    fun setVpnSocket(vpnSocket: ParcelFileDescriptor?) {
        this.vpnSocket = vpnSocket
    }

    fun setFileStreams(fileInputStream: FileInputStream?, fileOutputStream: FileOutputStream?) {
        `in` = fileInputStream
        out = fileOutputStream
    }

    fun addRouteAndNetwork(route: Route, networkId: Long) {
        synchronized(routeMap) { routeMap.put(route, networkId) }
    }

    fun clearRouteMap() {
        synchronized(routeMap) {
            routeMap.clear()
            addMulticastRoutes()
        }
    }

    private fun isIPv4Multicast(inetAddress: InetAddress): Boolean {
        return inetAddress.address[0].toInt() and 0xF0 == 224
    }

    private fun isIPv6Multicast(inetAddress: InetAddress): Boolean {
        return inetAddress.address[0].toInt() and 0xFF == 0xFF
    }

    fun startThreads() {
        receiveThread = object : Thread("Tunnel Receive Thread") {
            override fun run() {
                // 创建 ARP、NDP 表
                if (ndpTable == null) {
                    ndpTable = NDPTable()
                }
                if (arpTable == null) {
                    arpTable = ARPTable()
                }
                // 转发 TUN 消息至 Zerotier
                try {
                    Log.d(TAG, "TUN Receive Thread Started")
                    val buffer = ByteBuffer.allocate(32767)
                    buffer.order(ByteOrder.LITTLE_ENDIAN)
                    while (!isInterrupted) {
                        try {
                            var noDataBeenRead = true
                            val readCount = `in`!!.read(buffer.array())
                            if (readCount > 0) {
                                Log.d(TAG, "Sending packet to ZeroTier. $readCount bytes.")
                                val readData = ByteArray(readCount)
                                System.arraycopy(buffer.array(), 0, readData, 0, readCount)
                                val iPVersion: Byte = IPPacketUtils.getIPVersion(readData)
                                if (iPVersion.toInt() == 4) {
                                    handleIPv4Packet(readData)
                                } else if (iPVersion.toInt() == 6) {
                                    handleIPv6Packet(readData)
                                } else {
                                    Log.e(TAG, "Unknown IP version")
                                }
                                buffer.clear()
                                noDataBeenRead = false
                            }
                            if (noDataBeenRead) {
                                sleep(10)
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "Error in TUN Receive: " + e.message, e)
                        }
                    }
                } catch (ignored: InterruptedException) {
                }
                Log.d(TAG, "TUN Receive Thread ended")
                // 关闭 ARP、NDP 表
                ndpTable!!.stop()
                ndpTable = null
                arpTable!!.stop()
                arpTable = null
            }
        }
        receiveThread?.run { start() }
    }

    private fun handleIPv4Packet(packetData: ByteArray) {
        Log.i(TAG, "handleIPv4Packet: packetData: $packetData")
        var handledPacketData: ByteArray = onHandleIPPacket(packetData)
        val isMulticast: Boolean
        val destMac: Long
        var destIP: InetAddress? = IPPacketUtils.getDestIP(handledPacketData)
        val sourceIP: InetAddress? = IPPacketUtils.getSourceIP(handledPacketData)
        val virtualNetworkConfig: VirtualNetworkConfig? = ztService.node.networkConfig(networkId)
        if (virtualNetworkConfig == null) {
            Log.e(TAG, "TunTapAdapter has no network config yet")
            return
        } else if (destIP == null) {
            Log.e(TAG, "destAddress is null")
            return
        } else if (sourceIP == null) {
            Log.e(TAG, "sourceAddress is null")
            return
        }
        isMulticast = if (isIPv4Multicast(destIP)) {
            val result: ResultCode =
                node!!.multicastSubscribe(networkId, multicastAddressToMAC(destIP))
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error when calling multicastSubscribe: $result")
            }
            true
        } else {
            false
        }
        val route = routeForDestination(destIP)
        val gateway = route?.gateway

        // 查找当前节点的 v4 地址
        val ztAddresses: Array<InetSocketAddress> = virtualNetworkConfig.assignedAddresses
        var localV4Address: InetAddress? = null
        var cidr = 0
        for (address in ztAddresses) {
            if (address.address is Inet4Address) {
                localV4Address = address.address
                cidr = address.port
                break
            }
        }
        val destRoute = InetAddressUtils.addressToRouteNo0Route(destIP, cidr)
        val sourceRoute = InetAddressUtils.addressToRouteNo0Route(sourceIP, cidr)
        if (gateway != null && destRoute != sourceRoute) {
            destIP = gateway
        }
        if (localV4Address == null) {
            Log.e(TAG, "Couldn't determine local address")
            return
        }
        val localMac: Long = virtualNetworkConfig.mac
        val nextDeadline = LongArray(1)
        if (isMulticast || arpTable!!.hasMacForAddress(destIP)) {
            // 已确定目标 MAC，直接发送
            destMac = if (isIPv4Multicast(destIP)) {
                multicastAddressToMAC(destIP)
            } else {
                arpTable!!.getMacForAddress(destIP)
            }
            val result: ResultCode = node!!.processVirtualNetworkFrame(
                System.currentTimeMillis(),
                networkId,
                localMac,
                destMac,
                IPV4_PACKET,
                0,
                handledPacketData,
                nextDeadline
            )
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error calling processVirtualNetworkFrame: $result")
                return
            }
            Log.d(TAG, "Packet sent to ZT")
            ztService.nextBackgroundTaskDeadline = nextDeadline[0]
        } else {
            // 目标 MAC 未知，进行 ARP 查询
            Log.d(TAG, "Unknown dest MAC address.  Need to look it up. $destIP")
            destMac = InetAddressUtils.BROADCAST_MAC_ADDRESS
            handledPacketData = arpTable!!.getRequestPacket(localMac, localV4Address, destIP)
            val result: ResultCode = node!!.processVirtualNetworkFrame(
                System.currentTimeMillis(),
                networkId,
                localMac,
                destMac,
                ARP_PACKET,
                0,
                handledPacketData,
                nextDeadline
            )
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error sending ARP packet: $result")
                return
            }
            Log.d(TAG, "ARP Request Sent!")
            ztService.nextBackgroundTaskDeadline = nextDeadline[0]
        }
    }

    private fun handleIPv6Packet(packetData: ByteArray) {
        var handledPacketData: ByteArray = onHandleIPPacket(packetData)
        var destIP: InetAddress? = IPPacketUtils.getDestIP(handledPacketData)
        val sourceIP: InetAddress? = IPPacketUtils.getSourceIP(handledPacketData)
        val virtualNetworkConfig: VirtualNetworkConfig? = ztService.node.networkConfig(networkId)
        if (virtualNetworkConfig == null) {
            Log.e(TAG, "TunTapAdapter has no network config yet")
            return
        } else if (destIP == null) {
            Log.e(TAG, "destAddress is null")
            return
        } else if (sourceIP == null) {
            Log.e(TAG, "sourceAddress is null")
            return
        }
        if (isIPv6Multicast(destIP)) {
            val result: ResultCode =
                node!!.multicastSubscribe(networkId, multicastAddressToMAC(destIP))
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error when calling multicastSubscribe: $result")
            }
        }
        val route = routeForDestination(destIP)
        val gateway = route?.gateway

        // 查找当前节点的 v6 地址
        val ztAddresses: Array<InetSocketAddress> = virtualNetworkConfig.assignedAddresses
        var localV4Address: InetAddress? = null
        var cidr = 0
        for (address in ztAddresses) {
            if (address.address is Inet6Address) {
                localV4Address = address.address
                cidr = address.port
                break
            }
        }
        val destRoute = InetAddressUtils.addressToRouteNo0Route(destIP, cidr)
        val sourceRoute = InetAddressUtils.addressToRouteNo0Route(sourceIP, cidr)
        if (gateway != null && destRoute != sourceRoute) {
            destIP = gateway
        }
        if (localV4Address == null) {
            Log.e(TAG, "handleIPv6Packet: Couldn't determine local address")
            return
        }
        val localMac: Long = virtualNetworkConfig.mac
        val nextDeadline = LongArray(1)

        // 确定目标 MAC 地址
        var destMac: Long
        var sendNSPacket = false
        if (isNeighborSolicitation(handledPacketData)) {
            // 收到本地 NS 报文，根据 NDP 表记录确定是否广播查询
            destMac = if (ndpTable!!.hasMacForAddress(destIP)) {
                ndpTable!!.getMacForAddress(destIP)
            } else {
                InetAddressUtils.ipv6ToMulticastAddress(destIP)
            }
        } else if (isIPv6Multicast(destIP)) {
            // 多播报文
            destMac = multicastAddressToMAC(destIP)
        } else if (isNeighborAdvertisement(handledPacketData)) {
            // 收到本地 NA 报文
            destMac = if (ndpTable!!.hasMacForAddress(destIP)) {
                ndpTable!!.getMacForAddress(destIP)
            } else {
                // 目标 MAC 未知，不发送数据包
                0L
            }
            sendNSPacket = true
        } else {
            // 收到普通数据包，根据 NDP 表记录确定是否发送 NS 请求
            if (ndpTable!!.hasMacForAddress(destIP)) {
                // 目标地址 MAC 已知
                destMac = ndpTable!!.getMacForAddress(destIP)
            } else {
                destMac = 0L
                sendNSPacket = true
            }
        }
        // 发送数据包
        if (destMac != 0L) {
            val result: ResultCode = node!!.processVirtualNetworkFrame(
                System.currentTimeMillis(),
                networkId,
                localMac,
                destMac,
                IPV6_PACKET,
                0,
                handledPacketData,
                nextDeadline
            )
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error calling processVirtualNetworkFrame: $result")
            } else {
                Log.d(TAG, "Packet sent to ZT")
                ztService.nextBackgroundTaskDeadline = nextDeadline[0]
            }
        }
        // 发送 NS 请求
        if (sendNSPacket) {
            if (destMac == 0L) {
                destMac = InetAddressUtils.ipv6ToMulticastAddress(destIP)
            }
            Log.d(TAG, "Sending Neighbor Solicitation")
            handledPacketData = ndpTable!!.getNeighborSolicitationPacket(sourceIP, destIP, localMac)
            val result: ResultCode = node!!.processVirtualNetworkFrame(
                System.currentTimeMillis(),
                networkId,
                localMac,
                destMac,
                IPV6_PACKET,
                0,
                handledPacketData,
                nextDeadline
            )
            if (result != ResultCode.RESULT_OK) {
                Log.e(TAG, "Error calling processVirtualNetworkFrame: $result")
            } else {
                Log.d(TAG, "Neighbor Solicitation sent to ZT")
                ztService.nextBackgroundTaskDeadline = nextDeadline[0]
            }
        }
    }

    fun setOnHandleIPPacket(lambda: (packetData: ByteArray) -> ByteArray) {
        this.onHandleIPPacket = lambda
    }

    fun interrupt() {
        if (receiveThread != null) {
            try {
                `in`!!.close()
                out!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error stopping in/out: " + e.message, e)
            }
            receiveThread!!.interrupt()
            try {
                receiveThread!!.join()
            } catch (ignored: InterruptedException) {
            }
        }
    }

    @Throws(InterruptedException::class)
    fun join() {
        receiveThread!!.join()
    }

    private fun isNeighborSolicitation(packetData: ByteArray?): Boolean {
        return packetData!![6].toInt() == 58 && packetData[40].toInt() == -121
    }

    private fun isNeighborAdvertisement(packetData: ByteArray?): Boolean {
        return packetData!![6].toInt() == 58 && packetData[40].toInt() == -120
    }


    /**
     * 响应并处理 ZT 网络发送至本节点的以太网帧
     */
    override fun onVirtualNetworkFrame(
        networkId: Long, srcMac: Long, destMac: Long, etherType: Long,
        vlanId: Long, frameData: ByteArray
    ) {
        Log.d(
            TAG, "Got Virtual Network Frame. " +
                    " Network ID: " + StringUtils.networkIdToString(networkId) +
                    " Source MAC: " + StringUtils.macAddressToString(srcMac) +
                    " Dest MAC: " + StringUtils.macAddressToString(destMac) +
                    " Ether type: " + StringUtils.etherTypeToString(etherType) +
                    " VLAN ID: " + vlanId + " Frame Length: " + frameData.size
        )
        if (vpnSocket == null) {
            Log.e(TAG, "vpnSocket is null!")
        } else if (`in` == null || out == null) {
            Log.e(TAG, "no in/out streams")
        } else if (etherType == ARP_PACKET.toLong()) {
            // 收到 ARP 包。更新 ARP 表，若需要则进行应答
            Log.d(TAG, "Got ARP Packet")
            val arpReply = arpTable!!.processARPPacket(frameData)
            if (arpReply != null && arpReply.destMac != 0L && arpReply.destAddress != null) {
                // 获取本地 V4 地址
                val networkConfig: VirtualNetworkConfig = node!!.networkConfig(networkId)
                var localV4Address: InetAddress? = null
                for (address in networkConfig.assignedAddresses) {
                    if (address.address is Inet4Address) {
                        localV4Address = address.address
                        break
                    }
                }
                // 构造并返回 ARP 应答
                if (localV4Address != null) {
                    val nextDeadline = LongArray(1)
                    val packetData = arpTable!!.getReplyPacket(
                        networkConfig.mac,
                        localV4Address, arpReply.destMac, arpReply.destAddress
                    )
                    node?.let {
                        val result: ResultCode = it
                            .processVirtualNetworkFrame(
                                System.currentTimeMillis(), networkId,
                                networkConfig.mac, srcMac, ARP_PACKET, 0,
                                packetData, nextDeadline
                            )
                        if (result != ResultCode.RESULT_OK) {
                            Log.e(TAG, "Error sending ARP packet: $result")
                            return
                        }
                    }

                    Log.d(TAG, "ARP Reply Sent!")
                    ztService.nextBackgroundTaskDeadline = nextDeadline[0]
                }
            }
        } else if (etherType == IPV4_PACKET.toLong()) {
            // 收到 IPv4 包。根据需要发送至 TUN
            Log.d(TAG, "Got IPv4 packet. Length: " + frameData.size + " Bytes")
            try {
                val sourceIP: InetAddress? = IPPacketUtils.getSourceIP(frameData)
                if (sourceIP != null) {
                    if (isIPv4Multicast(sourceIP)) {
                        val result: ResultCode = node!!.multicastSubscribe(
                            this.networkId,
                            multicastAddressToMAC(sourceIP)
                        )
                        if (result != ResultCode.RESULT_OK) {
                            Log.e(TAG, "Error when calling multicastSubscribe: $result")
                        }
                    } else {
                        arpTable!!.setAddress(sourceIP, srcMac)
                    }
                }
                out!!.write(frameData)
            } catch (e: Exception) {
                Log.e(TAG, "Error writing data to vpn socket: " + e.message, e)
            }
        } else if (etherType == IPV6_PACKET.toLong()) {
            // 收到 IPv6 包。根据需要发送至 TUN，并更新 NDP 表
            Log.d(TAG, "Got IPv6 packet. Length: " + frameData.size + " Bytes")
            try {
                val sourceIP: InetAddress? = IPPacketUtils.getSourceIP(frameData)
                if (sourceIP != null) {
                    if (isIPv6Multicast(sourceIP)) {
                        val result: ResultCode = node!!.multicastSubscribe(
                            this.networkId,
                            multicastAddressToMAC(sourceIP)
                        )
                        if (result != ResultCode.RESULT_OK) {
                            Log.e(TAG, "Error when calling multicastSubscribe: $result")
                        }
                    } else {
                        ndpTable!!.setAddress(sourceIP, srcMac)
                    }
                }
                out!!.write(frameData)
            } catch (e: Exception) {
                Log.e(TAG, "Error writing data to vpn socket: " + e.message, e)
            }
        } else if (frameData.size >= 14) {
            Log.d(
                TAG,
                "Unknown Packet Type Received: 0x" + String.format(
                    "%02X%02X",
                    frameData[12],
                    frameData[13]
                )
            )
        } else {
            Log.d(TAG, "Unknown Packet Received.  Packet Length: " + frameData.size)
        }
    }

    private fun routeForDestination(destAddress: InetAddress): Route? {
        synchronized(routeMap) {
            for (route in routeMap.keys) {
                if (route.belongsToRoute(destAddress)) {
                    return route
                }
            }
            return null
        }
    }

    private fun networkIdForDestination(destAddress: InetAddress): Long {
        synchronized(routeMap) {
            for (route in routeMap.keys) {
                if (route.belongsToRoute(destAddress)) {
                    return routeMap[route]!!
                }
            }
            return 0
        }
    }

    companion object {
        const val TAG = "TunTapAdapter"
        private const val ARP_PACKET = 2054
        private const val IPV4_PACKET = 2048
        private const val IPV6_PACKET = 34525
        fun multicastAddressToMAC(inetAddress: InetAddress): Long {
            return when (inetAddress) {
                is Inet4Address -> {
                    val address = inetAddress.getAddress()
                    ByteBuffer.wrap(
                        byteArrayOf(
                            0,
                            0,
                            1,
                            0,
                            94,
                            (address[1] and Byte.MAX_VALUE),
                            address[2],
                            address[3]
                        )
                    ).getLong()
                }

                !is Inet6Address -> {
                    0
                }

                else -> {
                    val address2 = inetAddress.getAddress()
                    ByteBuffer.wrap(
                        byteArrayOf(
                            0,
                            0,
                            51,
                            51,
                            address2[12],
                            address2[13],
                            address2[14],
                            address2[15]
                        )
                    ).getLong()
                }
            }
        }
    }
}
