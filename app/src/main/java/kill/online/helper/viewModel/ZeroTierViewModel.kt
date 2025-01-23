package kill.online.helper.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.zerotier.sdk.Peer
import com.zerotier.sdk.PeerRole
import kill.online.helper.data.AppSetting
import kill.online.helper.data.Message
import kill.online.helper.data.MsgType
import kill.online.helper.data.Room
import kill.online.helper.data.Sticker
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.KillPacketType
import kill.online.helper.utils.getClientPacketType
import kill.online.helper.utils.getServerPacketType
import kill.online.helper.zeroTier.model.Moon
import kill.online.helper.zeroTier.model.ZTNetwork
import kill.online.helper.zeroTier.service.ZeroTierOneService
import kill.online.helper.zeroTier.util.IPPacketUtils
import kill.online.helper.zeroTier.util.IPPacketUtils.getDestIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getSourceIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getUDPData
import kill.online.helper.zeroTier.util.IPPacketUtils.handleUDPData
import kill.online.helper.zeroTier.util.InetAddressUtils
import java.math.BigInteger
import java.net.Inet4Address


class ZeroTierViewModel : ViewModel() {
    private val TAG = "ZeroTierViewModel"

    // ----zerotier本地服务----
    @SuppressLint("StaticFieldLeak")
    private lateinit var ztService: ZeroTierOneService
    var isZTRunning by mutableStateOf(false)
    private val ztConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.P)
        @Synchronized
        override fun onServiceConnected(
            componentName: ComponentName, iBinder: IBinder
        ) {
            this@ZeroTierViewModel.ztService =
                (iBinder as ZeroTierOneService.ZeroTierBinder).service
            setGamePacketCallBack()
            ztService.setCallBack(onStartZeroTier = { isZTRunning = true },
                onStopZeroTier = { isZTRunning = false })
            Log.i(TAG, "onServiceConnected: succeed to bind ZeroTierOneService")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind ZeroTierOneService")
        }
    }

    // ----zerotier网络配置----
    var ztNetworks = mutableStateListOf<ZTNetwork>()
    var ztMoons = mutableStateListOf<Moon>()
    var peers = mutableStateListOf<Peer>()

    // peers的zerotier地址到IP地址的映射
    var ztToIP = mutableStateMapOf<String, String>()

    //----app设置----
    private var isLoaded by mutableStateOf(false)
    var appSetting = mutableStateOf(AppSetting())

    // ----游戏相关----
    val rooms = mutableStateListOf<Room>()
    var enteredRoom = mutableStateOf(Room())
    val roomRules = mutableStateListOf<Room.RoomRule>()
    val roomPassword = mutableStateMapOf<String, String>()

    // ----zerotier本地服务----
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun startZeroTier(context: Context) {
        // 检查当前是否已经有通知权限
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                SharedViewModel.appViewModel.permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val networkId: Long = BigInteger(getLastActivatedNetworkId(), 16).toLong()
        //初始化 VPN 授权
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            // 打开系统设置界面
            startActivityForResult(context as Activity, vpnIntent, 0, null)
            Log.i(TAG, "Intent is not NULL.  request to be approved.")
        } else {
            Log.i(TAG, "Intent is NULL.  Already approved.")
        }

        // 启动 ZeroTier 服务
        val ztIntent = Intent(
            context, ZeroTierOneService::class.java
        )
        ztIntent.putExtra(ZeroTierOneService.ZT_NETWORK_ID, networkId)
        //绑定服务
        context.bindService(
            ztIntent, this.ztConnection, Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        //启动服务
        context.startService(ztIntent)
    }

    fun stopZeroTier(context: Context) {
        context.unbindService(ztConnection)
        ztService.shutdown()
    }

    // ----房间相关----

    // deprecated
    private fun roomNameToObject(roomName: String): Room {
        val roomNameList = roomName.split("|")
        if (roomNameList.size < 6) {
            return Room(
                isPrivateRoom = false,
                state = Room.RoomState.WAITING,
                roomOwner = "",
                roomName = roomName,
                roomRule = Room.RoomRule(mode = "", rule = "")
            )
        } else {
            return Room(
                isPrivateRoom = roomNameList[0] == "🔒",
                state = if (roomNameList[1] == "🔥") Room.RoomState.PLAYING else Room.RoomState.WAITING,
                roomOwner = roomNameList[2],
                roomName = roomNameList[3],
                roomRule = Room.RoomRule(mode = roomNameList[4], rule = roomNameList[5])
            )
        }

    }

    private fun roomObjectToName(room: Room): String {
        Log.i(TAG, "roomObjectToName: room: $room")
        val roomState = if (room.state == Room.RoomState.WAITING) "👻" else "🔥"
        val isPrivateRoom = if (room.isPrivateRoom) "🔒" else "🔑"
        val roomOwner =
            if (room.roomOwner.length > 6) room.roomOwner.substring(0, 6) else room.roomOwner
        return "${isPrivateRoom}|${roomState}|${roomOwner}|${room.roomName}|${room.roomRule.mode}|${room.roomRule.rule}"
    }

    //----游戏包处理回调----
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setGamePacketCallBack() {
        ztService.tunTapAdapter.setOnHandleIPPacket { ipv4 ->
            val hexIpv4 = ipv4.joinToString(separator = ", ") { "0x" + String.format("%02X", it) }
            val sourceAddress = getSourceIP(ipv4)?.hostAddress
            val destAddress = getDestIP(ipv4)?.hostAddress
            Log.i(
                TAG,
                "setGamePacketCallBack: hexIpv4: $hexIpv4\nsourceAddress: $sourceAddress destAddress: $destAddress"
            )
            when {
                //房间广播包
                destAddress == InetAddressUtils.GLOBAL_BROADCAST_ADDRESS -> {
                    onRoomBroadcast(ipv4)
                }
                //房主---->成员
                sourceAddress in rooms.map { it.roomOwnerIp } -> {
                    val serverPacketType = getServerPacketType(ipv4)
                    Log.i(TAG, "setGamePacketCallBack: serverPacketType: $serverPacketType")
                    when (serverPacketType) {
                        KillPacketType.ENTER_ROOM_SUCCESS -> {
                            onEnterRoomSuccess(ipv4)
                        }

                        KillPacketType.QUIT_ROOM_SUCCESS -> {
                            onQuitRoomSuccess(ipv4)
                        }

                        KillPacketType.START_GAME -> {
                            onStartGame(ipv4)
                        }

                        KillPacketType.END_GAME -> {
                            onEndGame(ipv4)
                        }

                        else -> {
                            ipv4
                        }
                    }
                }
                //成员---->房主
                destAddress in rooms.map { it.roomOwnerIp } -> {
                    val clientPacketType = getClientPacketType(ipv4)
                    Log.i(TAG, "setGamePacketCallBack: clientPacketType: $clientPacketType")
                    when (clientPacketType) {
                        KillPacketType.TCP_SYNC -> {
                            onTCPSync(ipv4)
                        }

                        KillPacketType.REQUEST_ENTER_ROOM -> {
                            onRequestEnterRoom(ipv4)

                        }

                        KillPacketType.REQUEST_QUIT_ROOM -> {
                            onRequestQuitRoom(ipv4)
                        }

                        else -> {
                            ipv4
                        }
                    }
                }

                else -> {
                    ipv4
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun onRoomBroadcast(ipv4: ByteArray): ByteArray {
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        val udpData = getUDPData(ipv4)
        val utf8Array = udpData.dropLast(1).toByteArray()
        val roomName = String(utf8Array, Charsets.UTF_8)
        Log.i(TAG, "onRoomBroadcast: sourceIP $sourceIP destIP $destIP  roomName: $roomName")
        //发出该包的房间索引
        val roomIndex = rooms.indexOfFirst { room -> room.roomOwnerIp == sourceIP }
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //自己发出的广播
                sourceIP -> {
                    val roomOwner = appSetting.value.playerName
                    //没有找到相同的房间，说明是首次广播
                    if (roomIndex == -1) {
                        Log.i(TAG, "onRoomBroadcast: first broadcast")
                        val newRoom = Room(
                            roomName = if (appSetting.value.roomSetting.isCustomRoomName) appSetting.value.roomSetting.roomName else "${roomOwner}的房间",
                            roomOwner = roomOwner,
                            roomOwnerIp = myIp,
                            isPrivateRoom = appSetting.value.roomSetting.isPrivateRoom,
                            roomPassword = appSetting.value.roomSetting.roomPassword,
                            roomRule = roomRules.firstOrNull { it.checked } ?: Room.RoomRule(),
                            state = Room.RoomState.WAITING,
                            players = listOf(Room.RoomMember(name = roomOwner, ip = myIp)),
                            enableBlackList = appSetting.value.roomSetting.enableBlackList,
                            blackList = appSetting.value.roomSetting.blackList,
                        )
                        //更新 enteredRoom
                        enteredRoom.value = newRoom
                        rooms.add(newRoom)
                    }
                    //更新 enteredRoom 和 房间列表
                    enteredRoom.value =
                        enteredRoom.value.copy(roomName = if (appSetting.value.roomSetting.isCustomRoomName) appSetting.value.roomSetting.roomName else "${roomOwner}的房间",
                            roomOwner = roomOwner,
                            roomOwnerIp = myIp,
                            isPrivateRoom = appSetting.value.roomSetting.isPrivateRoom,
                            roomPassword = appSetting.value.roomSetting.roomPassword,
                            roomRule = roomRules.firstOrNull { it.checked } ?: Room.RoomRule(),
                            enableBlackList = appSetting.value.roomSetting.enableBlackList,
                            blackList = appSetting.value.roomSetting.blackList,
                            timeStamp = System.currentTimeMillis())
                    val index = rooms.indexOfFirst { it.roomOwnerIp == myIp }
                    if (index == -1) rooms.add(enteredRoom.value)
                    else rooms[index] = enteredRoom.value
                    Log.i(TAG, "onRoomBroadcast: broadcast room info")
                    //广播房间信息
                    getPeers()
                    peers.filter { it.role == PeerRole.PEER_ROLE_LEAF }.forEach { peer ->
                        val ztAddress = peer.address.toString(16)
                        Log.i(TAG, "onRoomBroadcast: ztAddress: $ztAddress")
                        ztToIP[ztAddress]?.let { ip ->
                            val roomStr =
                                SharedViewModel.appViewModel.gson.toJson(enteredRoom.value)
                            val configItem = Message.ConfigItem("roomBroadcast", roomStr)
                            val msg = Message(
                                msgType = MsgType.CONFIG,
                                msg = SharedViewModel.appViewModel.gson.toJson(configItem)
                            )
                            SharedViewModel.appViewModel.sendMessage(ip = ip, msg = msg)
                            Log.i(
                                TAG,
                                "onRoomBroadcast: send roomBroadcast ${configItem.value} to $ip"
                            )
                        }
                    }

                    return handleUDPData(ipv4) {
                        Log.i(TAG, "onRoomBroadcast: handleUDPData oldUDP ${udpData.toHexString()}")
                        //修改原来的房间名
                        val newUDP =
                            roomObjectToName(rooms.first { room -> room.roomOwnerIp == sourceIP }).toByteArray() + listOf<Byte>(
                                0x00
                            ).toByteArray()
                        Log.i(TAG, "onRoomBroadcast: handleUDPData newUDP ${newUDP.toHexString()}")
                        newUDP
                    }
                }
                //别人发出的广播
                else -> {
                    //如果房间列表里不存在该房间则，向其发送自己的ip信息
                    if (!rooms.any { room -> room.roomOwnerIp == sourceIP }) {
                        val ztAddress = ztService.node.address().toString(16)
                        val configItem = Message.ConfigItem("ztToIP", "${ztAddress}:${myIp}")
                        val msg = Message(
                            msgType = MsgType.CONFIG,
                            msg = SharedViewModel.appViewModel.gson.toJson(configItem)
                        )
                        SharedViewModel.appViewModel.sendMessage(
                            ip = sourceIP, msg = msg
                        )
                        Log.i(TAG, "onRoomBroadcast: send ztToIP ${configItem.value} to $sourceIP")
                    }
                }
            }
        }
        return ipv4
    }

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.P)
    private fun onTCPSync(ipv4: ByteArray): ByteArray {
        val wrongIpv4 = ByteArray(0)
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //本节点为房主，收到请求包，成员请求加入房间
                destIP -> {
                    //房主收到【请求进入房间】包，房主也要进行判断
                    enteredRoom.value.let { r ->
                        //启用黑名单并且在黑名单里，使数据包无效
                        Log.i(
                            TAG,
                            "onTCPSync: sourceIP $sourceIP blacklist ${appSetting.value.roomSetting.blackList}"
                        )
                        if (r.enableBlackList && sourceIP in appSetting.value.roomSetting.blackList) {
                            Log.i(TAG, "roomOwner find this player:$sourceIP in blacklist")
                            return wrongIpv4
                        }
                        //是否开启了密码房
                        if (r.isPrivateRoom) {
                            if (ipv4.size > r.roomPassword.length) {
                                val originalIpv4 =
                                    ipv4.copyOfRange(0, ipv4.size - r.roomPassword.length)
                                val passwdByteArray =
                                    ipv4.copyOfRange(ipv4.size - r.roomPassword.length, ipv4.size)
                                val passwdStr = String(passwdByteArray)
                                if (r.roomPassword == passwdStr) {
                                    return originalIpv4
                                }
                            }
                            Log.i(TAG, "roomOwner received error passwd")
                            return wrongIpv4
                        }
                    }
                }
                //本节点为成员，向房主发出包，请求加入房间
                sourceIP -> {
                    val room = rooms.firstOrNull { room -> room.roomOwnerIp == destIP }
                    room?.let { r ->
                        //启用黑名单并且在黑名单里，使数据包无效
                        val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                        if (r.enableBlackList && myIp in r.blackList) {
                            SharedViewModel.appViewModel.sysToastText = "你已被房主拉入黑名单"
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                // 在这里执行UI更新操作
                                sysToast.show()
                            }
                            Log.i(TAG, "player find himself in blacklist")
                            return wrongIpv4
                        }
                        //开启了密码房，并且密码错误，使数据包无效
                        if (r.isPrivateRoom && r.roomPassword != (roomPassword[r.roomOwnerIp]
                                ?: "")
                        ) {
                            SharedViewModel.appViewModel.sysToastText = "房间密码错误"
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                // 在这里执行UI更新操作
                                sysToast.show()
                            }
                            Log.i(TAG, "player send wrong passwd")
                            return wrongIpv4
                        }
                        // 开启了密码房，并且密码正确，向房主发送新【请求进入房间】包
                        if (r.isPrivateRoom && r.roomPassword == (roomPassword[r.roomOwnerIp] ?: "")
                        ) {
                            val passwdByteArray = r.roomPassword.toByteArray()
                            val newIpv4 = ipv4 + passwdByteArray
                            Log.i(TAG, "correct passwd, send newIpv4 ${newIpv4.toHexString()}")
                            return newIpv4
                        }
                    }
                    return ipv4
                }

                else -> {}
            }
        }
        return ipv4
    }

    // deprecated
    private fun onEnterRoomSuccess(ipv4: ByteArray): ByteArray {
        return ipv4
    }

    // deprecated
    private fun onQuitRoomSuccess(ipv4: ByteArray): ByteArray {
        return ipv4
    }

    private fun onStartGame(ipv4: ByteArray): ByteArray {
        //【开始游戏】包，更新游戏状态
        Log.i(TAG, "onStartGame: start game")
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //本节点为成员
                destIP -> {}
                //本节点为房主
                sourceIP -> {
                    enteredRoom.value = enteredRoom.value.copy(state = Room.RoomState.PLAYING)
                    return ipv4
                }

                else -> {}
            }
        }
        return ipv4
    }

    private fun onEndGame(ipv4: ByteArray): ByteArray {
        //【结束游戏】包，更新游戏状态
        Log.i(TAG, "onEndGame: end game")
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //本节点为成员
                destIP -> {}
                //本节点为房主
                sourceIP -> {
                    enteredRoom.value = enteredRoom.value.copy(state = Room.RoomState.WAITING)
                    return ipv4
                }

                else -> {}
            }
        }
        return ipv4
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onRequestEnterRoom(ipv4: ByteArray): ByteArray {
        //【请求进入房间】包
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //我发给房主的包
                sourceIP -> {
                    val tcpData = IPPacketUtils.getTCPData(ipv4)
                    var nickName = "未知"
                    try {
                        val nickNameByteList = mutableListOf<Byte>()
                        val len = tcpData[50].toInt()
                        var index = 54
                        repeat(len) {
                            if ((index < tcpData.size) && (tcpData[index].toInt() != 0x00)) {
                                nickNameByteList.add(tcpData[index])
                                index += 1
                            }
                        }
                        nickName = String(nickNameByteList.toByteArray(), Charsets.UTF_8)
                        Log.i(TAG, "onRequestEnterRoom: nickNameList $nickNameByteList")
                        Log.i(TAG, "onRequestEnterRoom: nickName: $nickName")
                    } catch (e: Exception) {
                        Log.e(TAG, "nick name error: ${e.message}")
                    }
                    val member = Room.RoomMember(
                        name = if (nickName == "未知") appSetting.value.playerName else nickName,
                        ip = myIp
                    )
                    val memberStr = SharedViewModel.appViewModel.gson.toJson(member)
                    val configItem = Message.ConfigItem("onRequestEnterRoom", memberStr)
                    val configItemStr = SharedViewModel.appViewModel.gson.toJson(configItem)
                    SharedViewModel.appViewModel.sendMessage(
                        ip = destIP,
                        msg = Message(msgType = MsgType.CONFIG, msg = configItemStr)
                    )
                    Log.i(TAG, "onRequestEnterRoom:$sourceIP request to enter room ")
                }

                else -> {}
            }
        }
        return ipv4
    }

    private fun onRequestQuitRoom(ipv4: ByteArray): ByteArray {
        //【请求退出房间】包
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.let { myIp ->
            when (myIp) {
                //我发给房主的包
                sourceIP -> {
                    val member = Room.RoomMember(name = appSetting.value.playerName, ip = myIp)
                    val memberStr = SharedViewModel.appViewModel.gson.toJson(member)
                    val configItem = Message.ConfigItem("onRequestQuitRoom", memberStr)
                    val configItemStr = SharedViewModel.appViewModel.gson.toJson(configItem)
                    SharedViewModel.appViewModel.sendMessage(
                        ip = destIP,
                        msg = Message(msgType = MsgType.CONFIG, msg = configItemStr)
                    )
                    Log.i(TAG, "onRequestQuitRoom:$sourceIP request to quit room ")
                }

                else -> {}
            }
        }
        return ipv4
    }

    //----zerotier网络配置----
    fun getPeers() {
        if (!isZTRunning) return
        peers.clear()
        ztService.node.peers().forEach {
            peers.add(it)
        }
    }

    fun getAssignedIP(): String? =
        if (!isZTRunning) null else ztService.getVirtualNetworkConfig().assignedAddresses?.first { it.address is Inet4Address }?.hostString

    private fun getLastActivatedNetworkId(): String {
        return ztNetworks.first { it.checked }.networkId
    }

    fun loadZTConfig() {
        if (isLoaded) return
        ztNetworks.clear()
        FileUtils.read<List<ZTNetwork>>(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.ZTNetworks,
            defaultValue = listOf()
        ).forEach { ztNetworks.add(it) }
        ztMoons.clear()
        FileUtils.read<List<Moon>>(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.ZTMoons,
            defaultValue = listOf()
        ).forEach { ztMoons.add(it) }

        FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting,
            defaultValue = AppSetting()
        ).let { appSetting.value = it }
        roomRules.clear()
        FileUtils.read<List<Room.RoomRule>>(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.RoomRules,
            defaultValue = listOf()
        ).forEach { roomRules.add(it) }
        isLoaded = true
        Log.i(
            TAG, "loadZTConfig: succeed to load ztNetworks ztMoons appSetting roomRules blacklist"
        )
    }

    fun initZTConfig(context: Context) {
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.ZTNetworks
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.ZTNetworks,
                content = listOf(ZTNetwork("a09acf02339ffab1", checked = true)),
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.ZTMoons
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.ZTMoons,
                content = listOf<Moon>(),
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.AppSetting
            )
        ) {
            FileUtils.write(dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.AppSetting,
                content = AppSetting().apply {
                    //初始化sticker
                    val stickerNameList = context.assets.list("sticker")?.toList()?.filter {
                        ("qq_" in it) or ("capoo_" in it)
                    }
                    stickerNameList?.let { nameList ->
                        Log.i(TAG, "stickerNameList: $nameList")
                        stickerManage = nameList.map {
                            Sticker(
                                name = it, usageCounter = 0, enable = true
                            )
                        }
                    }

                })
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.RoomRules
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.RoomRules,
                content = listOf<Room.RoomRule>()
            )
        }
        Log.i(
            TAG, "initZTConfig: succeed to init ztNetworks ztMoons appSetting roomRule blacklist"
        )
    }

    fun saveZTConfig(itemName: FileUtils.ItemName) {
        when (itemName) {
            FileUtils.ItemName.ZTNetworks -> FileUtils.write(
                itemName = FileUtils.ItemName.ZTNetworks, content = ztNetworks
            )

            FileUtils.ItemName.ZTMoons -> FileUtils.write(
                itemName = FileUtils.ItemName.ZTMoons, content = ztMoons
            )

            FileUtils.ItemName.AppSetting -> FileUtils.write(
                itemName = FileUtils.ItemName.AppSetting, content = appSetting.value
            )

            FileUtils.ItemName.RoomRules -> FileUtils.write(
                itemName = FileUtils.ItemName.RoomRules, content = roomRules
            )
        }
    }
}