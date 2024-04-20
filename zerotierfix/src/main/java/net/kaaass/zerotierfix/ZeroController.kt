package kill.online.helper.zeroTier

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import com.zerotier.sdk.Peer
import com.zerotier.sdk.PeerRole
import kill.online.helper.zeroTier.events.AfterJoinNetworkEvent
import kill.online.helper.zeroTier.events.IsServiceRunningReplyEvent
import kill.online.helper.zeroTier.events.IsServiceRunningRequestEvent
import kill.online.helper.zeroTier.events.NetworkListReplyEvent
import kill.online.helper.zeroTier.events.NetworkListRequestEvent
import kill.online.helper.zeroTier.events.NodeDestroyedEvent
import kill.online.helper.zeroTier.events.NodeIDEvent
import kill.online.helper.zeroTier.events.NodeStatusEvent
import kill.online.helper.zeroTier.events.NodeStatusRequestEvent
import kill.online.helper.zeroTier.events.PeerInfoReplyEvent
import kill.online.helper.zeroTier.events.PeerInfoRequestEvent
import kill.online.helper.zeroTier.events.StopEvent
import kill.online.helper.zeroTier.events.VPNErrorEvent
import kill.online.helper.zeroTier.events.VirtualNetworkConfigChangedEvent
import kill.online.helper.zeroTier.model.DnsServer
import kill.online.helper.zeroTier.model.DnsServerDao
import kill.online.helper.zeroTier.model.Network
import kill.online.helper.zeroTier.model.NetworkConfig
import kill.online.helper.zeroTier.model.NetworkDao
import kill.online.helper.zeroTier.model.type.NetworkStatus
import kill.online.helper.zeroTier.service.ZeroTierOneService
import kill.online.helper.zeroTier.service.ZeroTierOneService.ZeroTierBinder
import kill.online.helper.zeroTier.util.NetworkIdUtils
import org.apache.commons.validator.routines.InetAddressValidator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.query.WhereCondition

class ZeroTier(
    val activity: Activity,
    private val eventBus: EventBus = EventBus.getDefault()
) {
    private val networkConfig: ZeroTierConfig =
        ZeroTierConfig(
            "a09acf02339ffab1", true, DnsConfig.NoDNS,
            null, null, null, null
        )

    private var isBound = false
    private var boundService: ZeroTierOneService? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@ZeroTier.boundService = (iBinder as ZeroTierBinder).service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            this@ZeroTier.boundService = null
            this@ZeroTier.isBound = false
        }
    }
    private val vpnAuthLauncher: ActivityResultLauncher<Intent>? = null
    private val networks = mutableListOf<Network>()
    private val peers = mutableListOf<Peer>()
    private var onNodeStatus: (event: NodeStatusEvent) -> Unit = {}


    init {
        this.eventBus.register(this)

        eventBus.post(NetworkListRequestEvent())
        // 初始化节点及服务状态
        eventBus.post(NodeStatusRequestEvent())
        eventBus.post(IsServiceRunningRequestEvent())
        // 检查通知权限
        val notificationManager = NotificationManagerCompat.from(activity)
        if (!notificationManager.areNotificationsEnabled()) {
            // 无通知权限
//                showNoNotificationAlertDialog()
        }

    }

    fun configNetwork(
        networkId: String,
        routeAll: Boolean,
        dnsConfig: DnsConfig,
        ipv4Address_1: String?,
        ipv4Address_2: String?,
        ipv6Address_1: String?,
        ipv6Address_2: String?,
    ) {
        networkConfig.let {
            it.networkId = networkId
            it.routeAll = routeAll
            it.dnsConfig = dnsConfig
            it.ipv4Address_1 = ipv4Address_1
            it.ipv4Address_2 = ipv4Address_2
            it.ipv6Address_1 = ipv6Address_1
            it.ipv6Address_2 = ipv6Address_2
        }
    }


    /**
     * 启动 ZT 服务连接至指定网络
     *
     * @param networkId 网络号
     */
    private fun startService(networkId: Long) {
        val prepare = VpnService.prepare(activity)
        if (prepare != null) {
            // 等待 VPN 授权后连接网络
            vpnAuthLauncher?.launch(prepare)
            return
        }
        val intent = Intent(
            activity,
            ZeroTierOneService::class.java
        )
        intent.putExtra(ZeroTierOneService.ZT1_NETWORK_ID, networkId)
        bindService()
        activity.startService(intent)
    }

    /**
     * 停止 ZT 服务
     */
    private fun stopService() {
        if (this.boundService != null) {
            this.boundService?.stopZeroTier()
        }
        val intent = Intent(
            activity,
            ZeroTierOneService::class.java
        )
        eventBus.post(StopEvent())
        if (!activity.stopService(intent)) {
            Log.e("${this::class.simpleName}::stopService", "stopService() returned false")
        }
        unbindService()
    }


    fun bindService() {
        if (isBound) {
            if (activity.bindService(
                    Intent(activity, ZeroTierOneService::class.java),
                    this.connection,
                    Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
                )
            ) {
                isBound = true
            }
        }
    }

    fun unbindService() {
        if (isBound) {
            try {
                activity.unbindService(this.connection)
            } catch (e: Exception) {
                Log.e("${this::class.simpleName}::unbindService", "$e")
            } catch (th: Throwable) {
                isBound = false
                throw th
            }
            isBound = false
        }
    }


    private fun getNetworkList(): List<Network> {
        val daoSession = ZerotierFix.getDaoSession()
        daoSession.clear()
        return daoSession.networkDao.queryBuilder().orderAsc(NetworkDao.Properties.NetworkId)
            .build().forCurrentThread().list()
    }

    private fun updateNetworkList() {
        val networkList = getNetworkList()

        // 更新列表
        this.networks.clear()
        this.networks.addAll(networkList)

    }

    /*   fun onNetworkListCheckedChangeEvent(event: NetworkListCheckedChangeEvent) {
           val switchHandle = event.switchHandle
           val checked = event.isChecked
           val selectedNetwork = event.selectedNetwork
           if (checked) {
               // 退出已连接的网络
               val connectedNetworkId: Long = this.viewModel.getConnectNetworkId().getValue()
               if (connectedNetworkId != null) {
                   this.mBoundService.leaveNetwork(connectedNetworkId)
               }
               stopService()
               this.viewModel.doChangeConnectNetwork(null)


               // 启动网络

               val useCellularData = PreferenceManager
                   .getDefaultSharedPreferences(context)
                   .getBoolean(Constants.PREF_NETWORK_USE_CELLULAR_DATA, false)
               val activeNetworkInfo = (context
                   .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                   .activeNetworkInfo
               if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting) {
                   // 设备无网络
                   requireActivity().runOnUiThread(Runnable {
                       Toast.makeText(
                           this@NetworkListFragment.getContext(),
                           R.string.toast_no_network,
                           Toast.LENGTH_SHORT
                       ).show()
                       switchHandle.setChecked(false)
                   })
               } else if (useCellularData || activeNetworkInfo.type != 0) {
                   // 可以连接至网络
                   // 更新 DB 中的网络状态
                   DatabaseUtils.writeLock.lock()
                   try {
                       for (network in this.mNetworks) {
                           network.lastActivated = false
                           network.update()
                       }
                       selectedNetwork.lastActivated = true
                       selectedNetwork.update()
                   } finally {
                       DatabaseUtils.writeLock.unlock()
                   }
                   // 连接目标网络
                   if (!this.isBound()) {
                       sendStartServiceIntent(selectedNetwork.networkId)
                   } else {
                       this.mBoundService.joinNetwork(selectedNetwork.networkId)
                   }
                   this.viewModel.doChangeConnectNetwork(selectedNetwork.networkId)
                   Log.d(NetworkListFragment.TAG, "Joining Network: " + selectedNetwork.networkIdStr)
               } else {
                   // 移动数据且未确认
                   requireActivity().runOnUiThread(Runnable {
                       Toast.makeText(
                           this.getContext(),
                           R.string.toast_mobile_data,
                           Toast.LENGTH_SHORT
                       ).show()
                       switchHandle.setChecked(false)
                   })
               }
           } else {
               // 关闭网络
               Log.d(
                   NetworkListFragment.TAG,
                   "Leaving Leaving Network: " + selectedNetwork.networkIdStr
               )
               if (this.isBound() && this.mBoundService != null) {
                   this.mBoundService.leaveNetwork(selectedNetwork.networkId)
                   this.doUnbindService()
               }
               stopService()
               this.viewModel.doChangeConnectNetwork(null)
           }
       }*/

    /**
     * 根据网络配置，将Network、NetworkConfig插入表中
     */
    fun joinNetwork() {
        try {
            val networkIdStr = this.networkConfig.networkId
            val hexStringToLong = NetworkIdUtils.hexStringToLong(networkIdStr)
            //判断网络是否已存在
            val daoSession = ZerotierFix.getDaoSession()
            val networkDao = daoSession.networkDao
            if (networkDao.queryBuilder().where(
                    NetworkDao.Properties.NetworkId.eq(hexStringToLong),
                    *arrayOfNulls<WhereCondition>(0)
                ).build().forCurrentThread().list().isNotEmpty()
            ) {
                Log.e("${this::class.simpleName}::joinNetwork", "Network already present")
                return
            } else {
                Log.d(
                    "${this::class.simpleName}::joinNetwork",
                    "Joining network " + this.networkConfig.networkId
                )
            }
            //创建Network对象
            val network = Network()
            network.networkId = hexStringToLong
            network.networkIdStr = networkIdStr
            //创建networkConfig对象
            val networkConfig = NetworkConfig()
            networkConfig.id = hexStringToLong
            networkConfig.routeViaZeroTier = this.networkConfig.routeAll
            networkConfig.dnsMode = this.networkConfig.dnsConfig.ordinal
            //配置DNS
            if (this.networkConfig.dnsConfig.ordinal == 2) {
                val dnsServerDao = daoSession.dnsServerDao
                daoSession.queryBuilder(DnsServer::class.java)
                    .where(DnsServerDao.Properties.NetworkId.eq(hexStringToLong), *arrayOfNulls(0))
                    .buildDelete().forCurrentThread().executeDeleteWithoutDetachingEntities()
                daoSession.clear()
                if (this.networkConfig.ipv4Address_1 != null && InetAddressValidator.getInstance()
                        .isValid(this.networkConfig.ipv4Address_1)
                ) {
                    val dnsServer = DnsServer()
                    dnsServer.nameserver = this.networkConfig.ipv4Address_1
                    dnsServer.networkId = hexStringToLong
                    dnsServerDao.insert(dnsServer)
                }
                if (this.networkConfig.ipv4Address_2 != null && InetAddressValidator.getInstance()
                        .isValid(this.networkConfig.ipv4Address_2)
                ) {
                    val dnsServer2 = DnsServer()
                    dnsServer2.nameserver = this.networkConfig.ipv4Address_2
                    dnsServer2.networkId = hexStringToLong
                    dnsServerDao.insert(dnsServer2)
                }
                if (this.networkConfig.ipv6Address_1 != null && InetAddressValidator.getInstance()
                        .isValid(this.networkConfig.ipv6Address_1)
                ) {
                    val dnsServer3 = DnsServer()
                    dnsServer3.nameserver = this.networkConfig.ipv6Address_1
                    dnsServer3.networkId = hexStringToLong
                    dnsServerDao.insert(dnsServer3)
                }
                if (this.networkConfig.ipv6Address_2 != null && InetAddressValidator.getInstance()
                        .isValid(this.networkConfig.ipv6Address_2)
                ) {
                    val dnsServer4 = DnsServer()
                    dnsServer4.nameserver = this.networkConfig.ipv6Address_2
                    dnsServer4.networkId = hexStringToLong
                    dnsServerDao.insert(dnsServer4)
                }
            } else {
                networkConfig.useCustomDNS = false
            }
            //插入网络和网络配置
            daoSession.networkConfigDao.insert(networkConfig)
            network.networkConfigId = hexStringToLong
            networkDao.insert(network)
        } catch (th: Throwable) {
            throw th
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNodeStatus(event: NodeStatusEvent) {
        val status = event.status
        val clientVersion = event.clientVersion
        // 更新在线状态
        onNodeStatus(event)
    }

    fun setOnNodeStatus(lambda: (event: NodeStatusEvent) -> Unit) {
        onNodeStatus = lambda
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNodeDestroyed(event: NodeDestroyedEvent?) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVPNError(event: VPNErrorEvent) {
        val errorMessage = event.message

        updateNetworkList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetworkListReply(event: NetworkListReplyEvent) {
        Log.d("onNetworkListReply", "Got connecting network list")
        // 更新当前连接的网络
        val networks = event.networkList
        // 更新网络列表
        updateNetworkList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVirtualNetworkConfigChanged(event: VirtualNetworkConfigChangedEvent) {
        Log.d("onVirtualNetworkConfigChanged", "Got Network Info")
        val config = event.virtualNetworkConfig

        // Toast 提示网络状态
        val status = NetworkStatus.fromVirtualNetworkStatus(config.status)
        val networkId = com.zerotier.sdk.util.StringUtils.networkIdToString(config.nwid)
        var message: String? = null
        when (status) {
            NetworkStatus.OK -> message =
                activity.getString(R.string.toast_network_status_ok, networkId)

            NetworkStatus.ACCESS_DENIED -> message =
                activity.getString(R.string.toast_network_status_access_denied, networkId)

            NetworkStatus.NOT_FOUND -> message =
                activity.getString(R.string.toast_network_status_not_found, networkId)

            NetworkStatus.PORT_ERROR -> message =
                activity.getString(R.string.toast_network_status_port_error, networkId)

            NetworkStatus.CLIENT_TOO_OLD -> message =
                activity.getString(R.string.toast_network_status_client_too_old, networkId)

            NetworkStatus.AUTHENTICATION_REQUIRED -> message =
                activity.getString(R.string.toast_network_status_authentication_required, networkId)

            NetworkStatus.REQUESTING_CONFIGURATION -> {}
            else -> {}
        }
        Log.i("onVirtualNetworkConfigChanged", "$message")

        // 更新网络列表
        updateNetworkList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNodeID(nodeIDEvent: NodeIDEvent?) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIsServiceRunningReply(event: IsServiceRunningReplyEvent) {
        if (event.isRunning) {
            bindService()
        }
    }

    @Subscribe
    fun onAfterJoinNetworkEvent(event: AfterJoinNetworkEvent?) {
        Log.d("onAfterJoinNetworkEvent", "Event on: AfterJoinNetworkEvent")

    }

    /**
     * Peer 类型转为文本
     */
    fun peerRoleToString(peerRole: PeerRole?): Int {
        return when (peerRole) {
            PeerRole.PEER_ROLE_PLANET -> R.string.peer_role_planet
            PeerRole.PEER_ROLE_LEAF -> R.string.peer_role_leaf
            PeerRole.PEER_ROLE_MOON -> R.string.peer_role_moon
            null -> R.string.peer_role_leaf
        }
    }


    fun refreshPeers() {
        this.eventBus.post(PeerInfoRequestEvent())
        // 超时自动重置刷新状态

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPeerInfoReplyEvent(event: PeerInfoReplyEvent) {
        val peers = event.peers
        if (peers == null) {
            Log.i("onPeerInfoReplyEvent", "peers is empty ")
            return
        }
        // 更新数据列表
        this.peers.clear()
        this.peers.addAll(peers)
    }

}