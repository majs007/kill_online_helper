package kill.online.helper.zeroTier.service

import android.util.Log
import java.net.InetAddress
import java.nio.ByteBuffer

// TODO: clear up
class ARPTable {
    // mac ------> ARP 表项
    private val macToARPEntriesMap = HashMap<Long, ARPEntry>()
    // ip ------> ARP 表项
    private val ipToARPEntriesMap = HashMap<InetAddress?, ARPEntry>()

    // ip ------> mac
    private val ipToMacMap = HashMap<InetAddress, Long>()
    // mac ------> ip
    private val macToIpMap = HashMap<Long, InetAddress>()

    // 超时线程，执行 ARP 表项超时清理
    private val timeoutThread: Thread = object : Thread("ARP Timeout Thread") {
        @Synchronized
        override fun run() {
            while (!isInterrupted) {
                try {
                    for (arpEntry in macToARPEntriesMap.values) {
                        if (arpEntry.time + ENTRY_TIMEOUT < System.currentTimeMillis()) {
                            Log.d(
                                TAG,
                                "Removing " + arpEntry.address.toString() + " from ARP cache"
                            )
                            synchronized(macToIpMap) { macToIpMap.remove(arpEntry.mac) }
                            synchronized(ipToMacMap) { ipToMacMap.remove(arpEntry.address) }
                            synchronized(macToARPEntriesMap) { macToARPEntriesMap.remove(arpEntry.mac) }
                            synchronized(ipToARPEntriesMap) { ipToARPEntriesMap.remove(arpEntry.address) }
                        }
                    }
                    sleep(1000)
                    setAddress(InetAddress.getByName("255.255.255.255"), 0xffffffffffff)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Tun/Tap Interrupted")
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
            Log.d(TAG, "ARP Timeout Thread Ended.")
        }
    }

    init {
        timeoutThread.start()
    }

    fun stop() {
        try {
            timeoutThread.interrupt()
            timeoutThread.join()
        } catch (ignored: InterruptedException) {

        }
    }


    fun setAddress(inetAddress: InetAddress, mac: Long) {
        val arpEntry = ARPEntry(mac, inetAddress)
        synchronized(ipToMacMap) { ipToMacMap.put(inetAddress, mac) }
        synchronized(macToIpMap) { macToIpMap.put(mac, inetAddress) }
        synchronized(macToARPEntriesMap) { macToARPEntriesMap.put(mac, arpEntry) }
        synchronized(ipToARPEntriesMap) { ipToARPEntriesMap.put(inetAddress, arpEntry) }
    }

    private fun updateArpEntryTime(mac: Long) {
        synchronized(macToARPEntriesMap) {
            val arpEntry = macToARPEntriesMap[mac]
            arpEntry?.updateTime()
        }
    }

    private fun updateArpEntryTime(inetAddress: InetAddress?) {
        synchronized(ipToARPEntriesMap) {
            val arpEntry = ipToARPEntriesMap[inetAddress]
            arpEntry?.updateTime()
        }
    }


    fun getMacForAddress(inetAddress: InetAddress): Long {
        synchronized(ipToMacMap) {
            if (!ipToMacMap.containsKey(inetAddress)) {
                return -1
            }
            Log.d(TAG, "Returning MAC for $inetAddress")
            val mac = ipToMacMap[inetAddress]
            if (mac != null) {
                updateArpEntryTime(mac)
                return mac
            }
        }
        return -1
    }


    fun getAddressForMac(mac: Long): InetAddress? {
        synchronized(macToIpMap) {
            if (!macToIpMap.containsKey(mac)) {
                return null
            }
            val inetAddress = macToIpMap[mac]
            updateArpEntryTime(inetAddress)
            return inetAddress
        }
    }

    fun hasMacForAddress(inetAddress: InetAddress): Boolean {
        var containsKey: Boolean
        synchronized(ipToMacMap) {
            containsKey = ipToMacMap.containsKey(inetAddress)
        }
        return containsKey
    }

    fun hasAddressForMac(mac: Long): Boolean {
        var containsKey: Boolean
        synchronized(macToIpMap) {
            containsKey = macToIpMap.containsKey(mac)
        }
        return containsKey
    }

    fun getRequestPacket(mac: Long, inetAddress: InetAddress, inetAddress2: InetAddress): ByteArray {
        return getARPPacket(1, mac, 0, inetAddress, inetAddress2)
    }

    fun getReplyPacket(
        mac1: Long,
        inetAddress1: InetAddress,
        mac2: Long,
        inetAddress2: InetAddress
    ): ByteArray {
        return getARPPacket(2, mac1, mac2, inetAddress1, inetAddress2)
    }

    fun getARPPacket(
        i: Int,
        mac1: Long,
        mac2: Long,
        inetAddress1: InetAddress,
        inetAddress2: InetAddress
    ): ByteArray {
        val bArr = ByteArray(28)
        bArr[0] = 0
        bArr[1] = 1
        bArr[2] = 8
        bArr[3] = 0
        bArr[4] = 6
        bArr[5] = 4
        bArr[6] = 0
        bArr[7] = i.toByte()
        System.arraycopy(longToBytes(mac1), 2, bArr, 8, 6)
        System.arraycopy(inetAddress1.address, 0, bArr, 14, 4)
        System.arraycopy(longToBytes(mac2), 2, bArr, 18, 6)
        System.arraycopy(inetAddress2.address, 0, bArr, 24, 4)
        return bArr
    }

    fun processARPPacket(packetData: ByteArray): ARPReplyData? {
        val srcAddress: InetAddress?
        val dstAddress: InetAddress?
        Log.d(TAG, "Processing ARP packet")

        // 解析包内 IP、MAC 地址
        val rawSrcMac = ByteArray(8)
        System.arraycopy(packetData, 8, rawSrcMac, 2, 6)
        val rawSrcAddress = ByteArray(4)
        System.arraycopy(packetData, 14, rawSrcAddress, 0, 4)
        val rawDstMac = ByteArray(8)
        System.arraycopy(packetData, 18, rawDstMac, 2, 6)
        val rawDstAddress = ByteArray(4)
        System.arraycopy(packetData, 24, rawDstAddress, 0, 4)
        srcAddress = try {
            InetAddress.getByAddress(rawSrcAddress)
        } catch (unused: Exception) {
            null
        }
        dstAddress = try {
            InetAddress.getByAddress(rawDstAddress)
        } catch (unused: Exception) {
            null
        }
        val srcMac = ByteBuffer.wrap(rawSrcMac).getLong()
        val dstMac = ByteBuffer.wrap(rawDstMac).getLong()

        // 更新 ARP 表项
        if (srcMac != 0L && srcAddress != null) {
            setAddress(srcAddress, srcMac)
        }
        if (dstMac != 0L && dstAddress != null) {
            setAddress(dstAddress, dstMac)
        }

        // 处理响应行为
        val packetType = packetData[7]
        return if (packetType.toInt() == REQUEST) {
            // ARP 请求，返回应答数据
            Log.d(TAG, "Reply needed")
            ARPReplyData(srcMac, srcAddress)
        } else {
            null
        }
    }

    companion object {
        const val TAG = "ARPTable"
        private const val ENTRY_TIMEOUT: Long = 120000
        private const val REPLY = 2
        private const val REQUEST = 1
        fun longToBytes(j: Long): ByteArray {
            val allocate = ByteBuffer.allocate(8)
            allocate.putLong(j)
            return allocate.array()
        }
    }
}
