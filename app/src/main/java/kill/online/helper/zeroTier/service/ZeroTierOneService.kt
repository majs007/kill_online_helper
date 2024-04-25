package kill.online.helper.zeroTier.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zerotier.sdk.Event
import com.zerotier.sdk.EventListener
import com.zerotier.sdk.Node
import com.zerotier.sdk.ResultCode
import com.zerotier.sdk.VirtualNetworkConfig
import com.zerotier.sdk.VirtualNetworkConfigListener
import com.zerotier.sdk.VirtualNetworkConfigOperation
import com.zerotier.sdk.util.StringUtils.addressToString
import kill.online.helper.MainActivity
import kill.online.helper.R
import kill.online.helper.data.AppSetting
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.FileUtils.read
import kill.online.helper.zeroTier.model.UserNetworkConfig
import kill.online.helper.zeroTier.model.type.DNSMode
import kill.online.helper.zeroTier.util.Constants
import kill.online.helper.zeroTier.util.InetAddressUtils
import kill.online.helper.zeroTier.util.NetworkInfoUtils
import kill.online.helper.zeroTier.util.NetworkInfoUtils.CurrentConnection
import kill.online.helper.zeroTier.util.StringUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.properties.Delegates

class ZeroTierOneService : VpnService(), Runnable, EventListener, VirtualNetworkConfigListener {
    private val mBinder: IBinder = ZeroTierBinder()
    private val dataStore = DataStore(this)
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null
    private var disableIPv6 by Delegates.notNull<Boolean>()
    private var startID by Delegates.notNull<Int>()
    private var networkId by Delegates.notNull<Long>()
    var nextBackgroundTaskDeadline: Long = 0
    private var svrSocket: DatagramSocket = DatagramSocket(null)
    val node: Node = Node(System.currentTimeMillis())
    private var vpnSocket: ParcelFileDescriptor? = null
    private var notificationManager: NotificationManager? = null
    private var udpCom: UdpCom = UdpCom(this, svrSocket)
    private var udpThread: Thread = Thread(udpCom, "UDP Communication Thread")

    private lateinit var tunTapAdapter: TunTapAdapter
    private var vpnThread: Thread = Thread(this, "VPN Thread")
    private var v4MulticastScanner: Thread = object : Thread() {
        var subscriptions: List<String> = ArrayList()
        override fun run() {
            Log.d(TAG, "IPv4 Multicast Scanner Thread Started.")
            while (!isInterrupted) {
                try {
                    val groups = NetworkInfoUtils.listMulticastGroupOnInterface("tun0", false)
                    val arrayList2 = ArrayList(subscriptions)
                    val arrayList3 = ArrayList(groups)
                    arrayList3.removeAll(arrayList2.toSet())
                    for (str in arrayList3) {
                        try {
                            val hexStringToByteArray = StringUtils.hexStringToBytes(str)
                            for (i in 0 until hexStringToByteArray.size / 2) {
                                val b = hexStringToByteArray[i]
                                hexStringToByteArray[i] =
                                    hexStringToByteArray[hexStringToByteArray.size - i - 1]
                                hexStringToByteArray[hexStringToByteArray.size - i - 1] = b
                            }
                            val multicastSubscribe = node.multicastSubscribe(
                                networkId, TunTapAdapter.multicastAddressToMAC(
                                    InetAddress.getByAddress(hexStringToByteArray)
                                )
                            )
                            if (multicastSubscribe != ResultCode.RESULT_OK) {
                                Log.e(
                                    TAG,
                                    "Error when calling multicastSubscribe: $multicastSubscribe"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString(), e)
                        }
                    }
                    arrayList2.removeAll(ArrayList(groups).toSet())
                    for (str2 in arrayList2) {
                        try {
                            val hexStringToByteArray2 = StringUtils.hexStringToBytes(str2)
                            for (i2 in 0 until hexStringToByteArray2.size / 2) {
                                val b2 = hexStringToByteArray2[i2]
                                hexStringToByteArray2[i2] =
                                    hexStringToByteArray2[hexStringToByteArray2.size - i2 - 1]
                                hexStringToByteArray2[hexStringToByteArray2.size - i2 - 1] = b2
                            }
                            val multicastUnsubscribe = node.multicastUnsubscribe(
                                networkId, TunTapAdapter.multicastAddressToMAC(
                                    InetAddress.getByAddress(hexStringToByteArray2)
                                )
                            )
                            if (multicastUnsubscribe != ResultCode.RESULT_OK) {
                                Log.e(
                                    TAG,
                                    "Error when calling multicastUnsubscribe: $multicastUnsubscribe"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString(), e)
                        }
                    }
                    subscriptions = groups
                    sleep(1000)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "V4 Multicast Scanner Thread Interrupted", e)
                    break
                }
            }
            Log.d(TAG, "IPv4 Multicast Scanner Thread Ended.")
        }
    }
    private var v6MulticastScanner: Thread = object : Thread() {
        var subscriptions: List<String> = ArrayList()
        override fun run() {
            Log.d(TAG, "IPv6 Multicast Scanner Thread Started.")
            while (!isInterrupted) {
                try {
                    val groups = NetworkInfoUtils.listMulticastGroupOnInterface("tun0", true)
                    val arrayList2 = ArrayList(subscriptions)
                    val arrayList3 = ArrayList(groups)
                    arrayList3.removeAll(arrayList2.toSet())
                    for (str in arrayList3) {
                        try {
                            val multicastSubscribe = node.multicastSubscribe(
                                networkId, TunTapAdapter.multicastAddressToMAC(
                                    InetAddress.getByAddress(StringUtils.hexStringToBytes(str))
                                )
                            )
                            if (multicastSubscribe != ResultCode.RESULT_OK) {
                                Log.e(
                                    TAG,
                                    "Error when calling multicastSubscribe: $multicastSubscribe"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString(), e)
                        }
                    }
                    arrayList2.removeAll(ArrayList(groups).toSet())
                    for (str2 in arrayList2) {
                        try {
                            val multicastUnsubscribe = node.multicastUnsubscribe(
                                networkId, TunTapAdapter.multicastAddressToMAC(
                                    InetAddress.getByAddress(StringUtils.hexStringToBytes(str2))
                                )
                            )
                            if (multicastUnsubscribe != ResultCode.RESULT_OK) {
                                Log.e(
                                    TAG,
                                    "Error when calling multicastUnsubscribe: $multicastUnsubscribe"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString(), e)
                        }
                    }
                    subscriptions = groups
                    sleep(1000)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "V6 Multicast Scanner Thread Interrupted", e)
                    break
                }
            }
            Log.d(TAG, "IPv6 Multicast Scanner Thread Ended.")
        }
    }
    private var onStartZeroTier: () -> Unit = {}
    private var onStopZeroTier: () -> Unit = {}

    @SuppressLint("BinderGetCallingInMainThread")
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Bound by: " + packageManager.getNameForUid(Binder.getCallingUid()))
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Unbound by: " + packageManager.getNameForUid(Binder.getCallingUid()))
        return false
    }

    @Synchronized
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (startId == 3) {
            Log.i(TAG, "Authorizing VPN")
            return START_NOT_STICKY
        }
        startID = startId
        // 确定待启动的网络 ID
        // Intent 中指定了目标网络，直接使用此 ID
        networkId = intent.getLongExtra(ZT_NETWORK_ID, 0)
        tunTapAdapter = TunTapAdapter(this, networkId)
        // 检查当前的网络设置项
        val appSetting = read(
            context = this, dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting, defValue = AppSetting()
        )
        val useCellularData = appSetting.useCellularData
        disableIPv6 = appSetting.disableIpv6
        //查询当前网络连接情况
        val currentConnection = NetworkInfoUtils.getNetworkInfoCurrentConnection(this)
        if (currentConnection == CurrentConnection.CONNECTION_NONE) {
            // 未连接网络
            Toast.makeText(this, R.string.toast_no_network, Toast.LENGTH_SHORT).show()
            stopSelf(startID)
            return START_NOT_STICKY
        } else if (currentConnection == CurrentConnection.CONNECTION_MOBILE &&
            !useCellularData
        ) {
            // 使用移动网络，但未在设置中允许移动网络访问
            Toast.makeText(this, R.string.toast_mobile_data, Toast.LENGTH_LONG).show()
            stopSelf(startID)
            return START_NOT_STICKY
        }
        // 启动 ZT 服务
        try {
            // 创建本地 ZT 服务 Socket，监听本地端口
            svrSocket.setReuseAddress(true)
            svrSocket.setSoTimeout(1000)
            svrSocket.bind(InetSocketAddress(9994))
            if (!protect(svrSocket)) {
                Log.e(TAG, "Error protecting UDP socket from feedback loop.")
            }
            // 创建节点对象并初始化
            val result = node.init(
                dataStore, dataStore, udpCom, this,
                tunTapAdapter, this, null
            )
            if (result == ResultCode.RESULT_OK) {
                Log.d(TAG, "ZeroTierOne Node Initialized")
            } else {
                Log.e(TAG, "Error starting ZT1 Node: $result")
                return START_NOT_STICKY
            }
            udpCom.setNode(node)
            tunTapAdapter.setNode(node)
            // 启动 UDP 消息处理线程
            udpThread.start()
            // 启动 VPN 服务线程
            vpnThread.start()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return START_NOT_STICKY
        }
        node.join(networkId)
        return START_STICKY
    }

    //stopService，系统调用，关闭服务清理内存
    override fun onDestroy() {
        stopZeroTier()
        vpnSocket = null
        stopSelf(startID)
        super.onDestroy()
    }

    override fun onRevoke() {
        stopZeroTier()
        stopSelf(startID)
        super.onRevoke()
    }

    //VPN Tread 入口函数
    override fun run() {
        val TAG = "VPN Thread"
        Log.d(TAG, "ZeroTierOne Service Started")
        Log.d(TAG, "This Node Address: " + addressToString(node.address()))
        while (!Thread.interrupted()) {
            try {
                // 在后台任务截止期前循环进行后台任务
                val taskDeadline = nextBackgroundTaskDeadline
                val currentTime = System.currentTimeMillis()
                val cmp = taskDeadline.compareTo(currentTime)
                if (cmp <= 0) {
                    val newDeadline = longArrayOf(0)
                    val taskResult = node.processBackgroundTasks(currentTime, newDeadline)
                    synchronized(this) { nextBackgroundTaskDeadline = newDeadline[0] }
                    if (taskResult != ResultCode.RESULT_OK) {
                        Log.e(TAG, "Error on processBackgroundTasks: $taskResult")
                        stopZeroTier()
                    }
                }
                Thread.sleep(if (cmp > 0) taskDeadline - currentTime else 100)
            } catch (ignored: InterruptedException) {
                break
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        Log.d(TAG, "ZeroTierOne Service Ended")
    }

    fun stopZeroTier() {
        svrSocket.close()
        if (udpThread.isAlive) {
            udpThread.interrupt()
            try {
                udpThread.join()
            } catch (ignored: InterruptedException) {
            }
        }
        if (tunTapAdapter.isRunning) {
            tunTapAdapter.interrupt()
            try {
                tunTapAdapter.join()
            } catch (ignored: InterruptedException) {
            }
        }
        if (vpnThread.isAlive) {
            vpnThread.interrupt()
            try {
                vpnThread.join()
            } catch (ignored: InterruptedException) {
            }
        }
        v4MulticastScanner.interrupt()
        try {
            v4MulticastScanner.join()
        } catch (ignored: InterruptedException) {
        }
        v6MulticastScanner.interrupt()
        try {
            v6MulticastScanner.join()
        } catch (ignored: InterruptedException) {
        }
        try {
            vpnSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN socket: $e", e)
        }
        vpnSocket = null
        node.close()
        notificationManager?.cancel(ZT_NOTIFICATION_TAG)
        if (!stopSelfResult(startID)) {
            Log.e(TAG, "stopSelfResult: failed!")
        }
        onStopZeroTier()
    }


    override fun onEvent(event: Event) {
        Log.d(TAG, "Event: $event")
    }

    override fun onTrace(str: String) {
        Log.d(TAG, "Trace: $str")
    }

    fun setOnHandleIPPacket(lambda: (packetData: ByteArray) -> ByteArray) {
        this.tunTapAdapter.setOnHandleIPPacket(lambda)
    }

    override fun onNetworkConfigurationUpdated(
        networkId: Long,
        op: VirtualNetworkConfigOperation,
        config: VirtualNetworkConfig
    ): Int {
        Log.i(TAG, "Virtual Network Config Operation: $op")
        return try {
            when (op) {
                VirtualNetworkConfigOperation.VIRTUAL_NETWORK_CONFIG_OPERATION_UP ->
                    Log.d(
                        TAG,
                        "Network Type: ${config.type} Network Status: ${config.status} Network Name: ${config.name}"
                    )

                VirtualNetworkConfigOperation.VIRTUAL_NETWORK_CONFIG_OPERATION_CONFIG_UPDATE -> {
                    Log.i(TAG, "Network Config Update!")
                    updateTunnelConfig(config)
                    onStartZeroTier()
                }

                VirtualNetworkConfigOperation.VIRTUAL_NETWORK_CONFIG_OPERATION_DOWN, VirtualNetworkConfigOperation.VIRTUAL_NETWORK_CONFIG_OPERATION_DESTROY -> {
                    Log.d(TAG, "Network Down!")
                }
            }
            0
        } finally {

        }
    }

    //android 系统vpn配置
    fun updateTunnelConfig(
        virtualNetworkConfig: VirtualNetworkConfig
    ): Boolean {
        val userNetworkConfig = read<List<UserNetworkConfig>>(
            context = this,
            itemName = FileUtils.ItemName.NetworkConfig,
            defValue = listOf()
        ).first {
            it.networkId == networkId.toULong().toString(16)
        }
        // 重启 TUN TAP
        if (tunTapAdapter.isRunning) {
            tunTapAdapter.interrupt()
            try {
                tunTapAdapter.join()
            } catch (ignored: InterruptedException) {
            }
        }
        tunTapAdapter.clearRouteMap()

        // 重启 VPN Socket
        try {
            vpnSocket?.close()
            inputStream?.close()
            outputStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN socket: $e", e)
        }
        vpnSocket = null
        inputStream = null
        outputStream = null

        // 配置 VPN
        Log.i(TAG, "Configuring VpnService.Builder")
        val builder = Builder()
        val assignedAddresses = virtualNetworkConfig.assignedAddresses
        Log.i(TAG, "address length: " + assignedAddresses.size)
        val isRouteViaZeroTier: Boolean = userNetworkConfig?.routeViaZeroTier ?: true

        // 遍历 ZT 网络中当前设备的 IP 地址，组播配置
        for (vpnAddress in assignedAddresses) {
            Log.d(
                TAG, "Adding VPN Address: " + vpnAddress.address
                        + " Mac: " + com.zerotier.sdk.util.StringUtils.macAddressToString(
                    virtualNetworkConfig.mac
                )
            )
            val rawAddress = vpnAddress.address.address
            //ipv4情况
            if (!disableIPv6 || vpnAddress.address !is Inet6Address) {
                val address = vpnAddress.address
                val port = vpnAddress.port
                val route = InetAddressUtils.addressToRoute(address, port)
                if (route == null) {
                    Log.e(TAG, "NULL route calculated!")
                    continue
                }
                // 计算 VPN 地址相关的组播 MAC 与 ADI
                var multicastGroup: Long
                var multicastAdi: Long
                if (rawAddress.size == 4) {
                    // IPv4
                    multicastGroup = InetAddressUtils.BROADCAST_MAC_ADDRESS
                    multicastAdi = ByteBuffer.wrap(rawAddress).getInt().toLong()
                } else {
                    // IPv6
                    multicastGroup = ByteBuffer.wrap(
                        byteArrayOf(
                            0,
                            0,
                            0x33,
                            0x33,
                            0xFF.toByte(),
                            rawAddress[13],
                            rawAddress[14],
                            rawAddress[15]
                        )
                    )
                        .getLong()
                    multicastAdi = 0
                }

                // 订阅组播并添加至 TUN TAP 路由
                val result = node.multicastSubscribe(networkId, multicastGroup, multicastAdi)
                if (result == ResultCode.RESULT_OK) {
                    Log.d(TAG, "Joined multicast group")
                } else {
                    Log.e(TAG, "Error joining multicast group")
                }
                builder.addAddress(address, port)
                builder.addRoute(route, port)
                tunTapAdapter.addRouteAndNetwork(Route(route, port), networkId)
            }
        }

        // 遍历网络的路由规则，将网络负责路由的地址路由至 VPN
        try {
            val v4Loopback = InetAddress.getByName("0.0.0.0")
            val v6Loopback = InetAddress.getByName("::")
            if (virtualNetworkConfig.routes.isNotEmpty()) {
                for (routeConfig in virtualNetworkConfig.routes) {
                    val target = routeConfig.target
                    val via = routeConfig.via
                    val targetAddress = target.address
                    val targetPort = target.port
                    val viaAddress = InetAddressUtils.addressToRoute(targetAddress, targetPort)
                    val isIPv6Route = targetAddress is Inet6Address || viaAddress is Inet6Address
                    val isDisabledV6Route = disableIPv6 && isIPv6Route
                    val shouldRouteToZerotier =
                        viaAddress != null && (isRouteViaZeroTier || viaAddress != v4Loopback && viaAddress != v6Loopback)
                    if (!isDisabledV6Route && shouldRouteToZerotier) {
                        builder.addRoute(viaAddress!!, targetPort)
                        val route = Route(viaAddress, targetPort)
                        if (via != null) {
                            route.gateway = via.address
                        }
                        tunTapAdapter.addRouteAndNetwork(route, networkId)
                    }
                }
            }
            builder.addRoute(InetAddress.getByName("224.0.0.0"), 4)
            builder.addRoute("255.255.255.255", 32)
        } catch (e: Exception) {
            return false
        }
        if (Build.VERSION.SDK_INT >= 29) {
            builder.setMetered(false)
        }

        addDNSServers(builder, userNetworkConfig)

        // 配置 MTU
        var mtu = virtualNetworkConfig.mtu
        Log.i(TAG, "MTU from Network Config: $mtu")
        if (mtu == 0) {
            mtu = 2800
        }
        Log.i(TAG, "MTU Set: $mtu")
        builder.setMtu(mtu)
        builder.setSession(Constants.VPN_SESSION_NAME)

        // 设置部分 APP 不经过 VPN
        if (!isRouteViaZeroTier && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (app in DISALLOWED_APPS) {
                try {
                    builder.addDisallowedApplication(app)
                } catch (e3: Exception) {
                    Log.e(TAG, "Cannot disallow app", e3)
                }
            }
        }

        // 建立 VPN 连接
        vpnSocket = builder.establish()
        inputStream = FileInputStream(vpnSocket?.fileDescriptor)
        outputStream = FileOutputStream(vpnSocket?.fileDescriptor)
        tunTapAdapter.setVpnSocket(vpnSocket)
        tunTapAdapter.setFileStreams(inputStream, outputStream)
        tunTapAdapter.startThreads()
        // 状态栏提示
        if (notificationManager == null) {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val channelName = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val channel = NotificationChannel(
                Constants.CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = description
            notificationManager?.createNotificationChannel(channel)
        }

        var pendingIntentFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= 31) {
            pendingIntentFlag = pendingIntentFlag or PendingIntent.FLAG_IMMUTABLE
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP
                ), pendingIntentFlag
        )
        val notification: Notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setPriority(1)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title_connected))
            .setContentText(
                getString(
                    R.string.notification_text_connected,
                    networkId.toULong().toString(16)
                )
            ).setSmallIcon(android.R.drawable.stat_notify_sync)
            .setColor(ContextCompat.getColor(applicationContext, R.color.notifyIconColor))
            .setContentIntent(pendingIntent).build()
        notificationManager?.notify(ZT_NOTIFICATION_TAG, notification)
        Log.i(TAG, "ZeroTier One Connected")

        // 旧版本 Android 多播处理
        if (Build.VERSION.SDK_INT < 29) {
            if (!v4MulticastScanner.isAlive) {
                v4MulticastScanner.start()
            }
            if (!disableIPv6 && !v6MulticastScanner.isAlive) {
                v6MulticastScanner.start()
            }
        }
        return true
    }

    private fun addDNSServers(builder: Builder, userNetworkConfig: UserNetworkConfig) {
        val virtualNetworkConfig =
            node.networkConfig(BigInteger(userNetworkConfig.networkId, 16).toLong())
        when (userNetworkConfig.dnsMode) {
            DNSMode.NETWORK_DNS -> {
                if (virtualNetworkConfig!!.dns == null) {
                    return
                }
                builder.addSearchDomain(virtualNetworkConfig.dns.domain)
                for (inetSocketAddress in virtualNetworkConfig.dns.servers) {
                    val address = inetSocketAddress.address
                    if (address is Inet4Address) {
                        builder.addDnsServer(address)
                    } else if (address is Inet6Address && !disableIPv6) {
                        builder.addDnsServer(address)
                    }
                }
            }

            DNSMode.CUSTOM_DNS -> for (dnsService in userNetworkConfig.dnsServers) {
                try {
                    val inetAddress = InetAddress.getByName(dnsService)
                    if (inetAddress is Inet4Address) {
                        builder.addDnsServer(inetAddress)
                    } else if (inetAddress is Inet6Address && !disableIPv6) {
                        builder.addDnsServer(inetAddress)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception parsing DNS server: $e", e)
                }
            }

            else -> {}
        }
    }

    fun setCallBack(onStartZeroTier: () -> Unit = {}, onStopZeroTier: () -> Unit = {}) {
        this.onStartZeroTier = onStartZeroTier
        this.onStopZeroTier = onStopZeroTier
    }

    inner class ZeroTierBinder : Binder() {
        val service: ZeroTierOneService
            get() = this@ZeroTierOneService
    }

    companion object {
        private const val TAG = "ZT_Service"
        const val MSG_JOIN_NETWORK = 1
        const val MSG_LEAVE_NETWORK = 2
        const val ZT_NETWORK_ID = "com.zerotier.one.network_id"
        const val ZT_USE_DEFAULT_ROUTE = "com.zerotier.one.use_default_route"
        private val DISALLOWED_APPS = arrayOf("com.android.vending")
        private const val ZT_NOTIFICATION_TAG = 5919812
    }
}
