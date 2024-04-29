package kill.online.helper.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.AppSetting
import kill.online.helper.data.Member
import kill.online.helper.data.ModifyMember
import kill.online.helper.data.Room
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.KillPacketType
import kill.online.helper.utils.StateUtils
import kill.online.helper.utils.StateUtils.update
import kill.online.helper.utils.getServerPacketType
import kill.online.helper.zeroTier.model.Moon
import kill.online.helper.zeroTier.model.UserNetwork
import kill.online.helper.zeroTier.model.UserNetworkConfig
import kill.online.helper.zeroTier.service.ZeroTierOneService
import kill.online.helper.zeroTier.util.IPPacketUtils.getDestIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getSourceIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getUDPData
import kill.online.helper.zeroTier.util.IPPacketUtils.handleUDPData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.net.InetSocketAddress


object ZeroTierViewModel : ViewModel() {
    private val TAG = "ZeroTierViewModel"

    @SuppressLint("StaticFieldLeak")
    private lateinit var ztService: ZeroTierOneService
    val isZTRunning = mutableStateOf(false)
    private val ztConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@ZeroTierViewModel.ztService =
                (iBinder as ZeroTierOneService.ZeroTierBinder).service
            setGamePacketCallBack()
            ztService.setCallBack(onStartZeroTier = { isZTRunning.value = true },
                onStopZeroTier = { isZTRunning.value = false })
            Log.i(TAG, "onServiceConnected: succeed to bind ZeroTierOneService")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind ZeroTierOneService")
        }
    }

    var ztNetworkConfig = mutableStateOf(listOf<UserNetworkConfig>())
    var ztNetwork = mutableStateOf(listOf<UserNetwork>())
    var ztMoon = mutableStateOf(listOf<Moon>())
    var appSetting = mutableStateOf(AppSetting())

    val members = mutableStateOf(listOf<Member>())
    val rooms = mutableStateOf(listOf<Room>())
    val enteredRoom = mutableStateOf(Room())
    val roomRule = mutableStateOf(listOf<Room.RoomRule>())
    val blacklist = mutableStateOf(listOf<Room.Member>())


    fun startZeroTier(context: Context) {
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
        ztService.stopZeroTier()
        val intent = Intent(context, ZeroTierOneService::class.java)
        if (context.stopService(intent)) {
            Log.e(TAG, "stopService returned false")
        }
        context.unbindService(ztConnection)
    }

    private fun roomNameToObject(roomName: String): Room {
        val roomNameList = roomName.split("|")
        return Room(
            isPrivateRoom = roomNameList[0] == "🔒",
            state = if (roomNameList[1] == "🔥") Room.RoomState.PLAYING else Room.RoomState.WAITING,
            roomOwner = roomNameList[2],
            roomName = roomNameList[3],
            roomRule = Room.RoomRule(mode = roomNameList[4], rule = roomNameList[5])
        )
    }

    private fun roomObjectToName(room: Room): String {
        val roomState = if (room.state == Room.RoomState.WAITING) "🔥" else "😴"
        val isPrivateRoom = if (room.isPrivateRoom) "🔒" else "🔓"
        val roomOwner =
            if (room.roomOwner.length > 6) room.roomOwner.substring(0, 6) else room.roomOwner
        return "${isPrivateRoom}|${roomState}|${roomOwner}|${room.roomName}|${room.roomRule.mode}|${room.roomRule.rule}"
    }

    private fun getAssignedIP(): InetSocketAddress? =
        ztService.getVirtualNetworkConfig().assignedAddresses?.first()

    fun getMyName(): String {
        return members.value.firstOrNull {
            it.config.ipAssignments.first() == getAssignedIP()?.address?.hostAddress
        }?.name
            ?: ""
    }

    fun getCheckedRuleIndex(): Int = roomRule.value.indexOfFirst { it.checked }

    private fun setGamePacketCallBack() {
        ztService.setOnHandleIPPacket { ipv4 ->
            val hexIpv4 = ipv4.joinToString(separator = ", ") { "0x" + String.format("%02X", it) }
            val udpData = getUDPData(ipv4)
            val hexUdpData =
                udpData.joinToString(separator = ", ") { "0x" + String.format("%02X", it) }
            val sourceAddress = getSourceIP(ipv4)?.hostAddress
            val destAddress = getDestIP(ipv4)?.hostAddress
            Log.i(
                TAG,
                "setGamePacketCallBack: hexIpv4: $hexIpv4\nudpData: $hexUdpData\n sourceAddress: $sourceAddress destAddress: $destAddress"
            )
            when {
                //房主发出的包，发给本节点或房间广播
                sourceAddress in rooms.value.map { it.roomOwnerIp } -> {
                    val serverPacketType = getServerPacketType(ipv4)
                    Log.i(TAG, "setGamePacketCallBack: serverPacketType: $serverPacketType")
                    when (serverPacketType) {
                        KillPacketType.ROOM_BROADCAST -> {
                            onRoomBroadcast(ipv4)
                        }

                        KillPacketType.ENTER_ROOM_SUCCESS -> {
                            onEnterRoomSuccess(ipv4)
                        }

                        KillPacketType.QUIT_ROOM_SUCCESS -> {
                            onQuitRoomSuccess(ipv4)
                        }

                        else -> {
                            ipv4
                        }
                    }
                }
                //成员发出的包，而且是本节点发出的
                destAddress in rooms.value.map { it.roomOwnerIp } -> {
                    val clientPacketType = getServerPacketType(ipv4)
                    Log.i(TAG, "setGamePacketCallBack: clientPacketType: $clientPacketType")
                    when (clientPacketType) {
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

    private fun onRoomBroadcast(ipv4: ByteArray): ByteArray {

        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        val udpData = getUDPData(ipv4)
        val roomName = String(udpData.dropLast(1).toByteArray(), Charsets.UTF_8)
        val roomOwner = members.value.first {
            it.config.ipAssignments.first() == sourceIP
        }.name
        //发出该包的房间索引
        val roomIndex = rooms.value.indexOfFirst { room -> room.roomOwnerIp == sourceIP }
        getAssignedIP()?.hostName.let { myIp ->
            when (myIp) {
                //自己发出的广播
                sourceIP -> {
                    //没有找到相同的房间，说明是首次广播
                    if (roomIndex == -1) {
                        val newRoom = Room(
                            roomName = if (appSetting.value.roomSetting.customRoomName) appSetting.value.roomSetting.roomName else "${roomOwner}的房间",
                            roomOwner = roomOwner,
                            roomOwnerIp = sourceIP ?: "",
                            isPrivateRoom = appSetting.value.roomSetting.isPrivateRoom,
                            roomPassword = appSetting.value.roomSetting.roomPassword,
                            roomRule = roomRule.value.first { it.checked },
                            state = Room.RoomState.WAITING,
                            players = listOf(Room.Member(name = roomOwner, ip = sourceIP ?: ""))
                        )
                        //更新 enteredRoom
                        enteredRoom.value = newRoom
                        StateUtils.add(
                            itemName = FileUtils.ItemName.Room,
                            state = rooms,
                            newItem = newRoom
                        )
                    }
                    //找到相同的房间，说明是第二次发出广播，更新房间信息
                    else {
                        update(
                            itemName = FileUtils.ItemName.Room,
                            state = rooms,
                            index = roomIndex
                        ) {
                            it.roomName =
                                if (appSetting.value.roomSetting.customRoomName) appSetting.value.roomSetting.roomName else "${roomOwner}的房间"
                            it.roomOwner = roomOwner
                            it.roomRule = roomRule.value.first { it.checked }
                            it.isPrivateRoom = appSetting.value.roomSetting.isPrivateRoom
                            it.copy()
                        }
                        //更新 enteredRoom
                        enteredRoom.value = rooms.value[roomIndex]
                    }
                    return handleUDPData(ipv4) {
                        roomObjectToName(rooms.value.first { room -> room.roomOwnerIp == sourceIP }).toByteArray() + listOf<Byte>(
                            0x00
                        ).toByteArray()
                    }
                }
                //别人发出的广播
                destIP -> {
                    val newRoom = roomNameToObject(roomName)
                    //找到相同的房间，说明是第一次收到别人发出的广播
                    if (roomIndex == -1) {
                        newRoom.roomOwnerIp = sourceIP ?: ""
                        newRoom.players = listOf(Room.Member(name = roomOwner, ip = sourceIP ?: ""))
                        StateUtils.add(
                            itemName = FileUtils.ItemName.Room,
                            state = rooms,
                            newItem = newRoom
                        )

                    }
                    //找到相同的房间，说明是第二次收到别人发出的广播，更新房间信息
                    else {
                        update(
                            itemName = FileUtils.ItemName.Room,
                            state = rooms,
                            index = roomIndex
                        ) {
                            it.roomName = newRoom.roomName
                            it.roomOwner = newRoom.roomOwner
                            it.roomRule = newRoom.roomRule
                            it.state = newRoom.state
                            it.isPrivateRoom = newRoom.isPrivateRoom
                            it.copy()
                        }
                    }
                }

                else -> {}
            }
        }
        return ipv4
    }

    //TODO 哪位成员加入房间的内容可能在游戏数据部分需要解析
    private fun onEnterRoomSuccess(ipv4: ByteArray): ByteArray {
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        val playerName =
            members.value.first { member -> member.config.ipAssignments.first() == destIP }.name
        enteredRoom.value.players += Room.Member(name = playerName, ip = destIP)
        return ipv4
    }

    //TODO 哪位成员退出房间的内容可能在游戏数据部分需要解析
    private fun onQuitRoomSuccess(ipv4: ByteArray): ByteArray {
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        val player = enteredRoom.value.players.first { player -> player.ip == destIP }
        enteredRoom.value.players -= player
        return ipv4
    }

    private fun onRequestEnterRoom(ipv4: ByteArray): ByteArray {
        val wrongIpv4 = ipv4.copyOf()
        listOf<Byte>(-1, -1, -1, -1).toByteArray().copyInto(wrongIpv4, 12)
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.hostName.let { myIp ->
            when (myIp) {
                //本节点为房主，收到请求包，成员请求加入房间
                destIP -> {

                    //启用黑名单并且在黑名单里，使数据包无效
                    if (appSetting.value.roomSetting.enableBlackList &&
                        sourceIP in blacklist.value.map { it.ip }
                    ) {
                        return wrongIpv4
                    }
                    //是否开启了密码房
                    if (appSetting.value.roomSetting.isPrivateRoom) {
                        val password =
                            ipv4.drop(ipv4.size - appSetting.value.roomSetting.roomPassword.length)
                        //密码错误，使数据包无效
                        return if (!password.toByteArray()
                                .contentEquals(appSetting.value.roomSetting.roomPassword.toByteArray())
                        ) wrongIpv4
                        //密码正确，使数据包有效
                        else ipv4.dropLast(appSetting.value.roomSetting.roomPassword.length)
                            .toByteArray()
                    }
                    //未开启密码房，使数据包有效--->不处理
                    else {
                    }
                }
                //本节点为成员，向房主发出包，请求加入房间
                sourceIP -> {
                    val room = rooms.value.firstOrNull { room -> room.roomOwnerIp == sourceIP }
                    room?.let {
                        return handleUDPData(ipv4) { udp -> udp + it.roomPassword.toByteArray() }
                    }
                }

                else -> {}
            }
        }
        return ipv4
    }

    private fun onRequestQuitRoom(ipv4: ByteArray): ByteArray {
        return ipv4
    }

    fun getLastActivatedNetworkId(): String {
        return ztNetwork.value.first {
            it.lastActivated
        }.networkId
    }

    fun loadZTConfig() {
        ztNetworkConfig.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.NetworkConfig,
            defValue = listOf()
        )
        ztNetwork.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Network,
            defValue = listOf()
        )
        ztMoon.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Moon,
            defValue = listOf()
        )
        appSetting.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting,
            defValue = AppSetting()
        )
        roomRule.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.RoomRule,
            defValue = listOf()
        )
        blacklist.value = FileUtils.read(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Blacklist,
            defValue = listOf()
        )

        Log.i(
            TAG,
            "loadZTConfig: succeed to load ztNetworkConfig ztNetwork ztMoon appSetting roomRule blacklist"
        )
    }

    fun initZTConfig() {
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.NetworkConfig
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.NetworkConfig,
                content = listOf(UserNetworkConfig("a09acf02339ffab1")),
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.Network
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.Network,
                content = listOf(UserNetwork("a09acf02339ffab1", lastActivated = true)),
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.Moon
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.Moon,
                content = listOf<Moon>(),
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.AppSetting
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.AppSetting,
                content = AppSetting()
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.RoomRule
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.RoomRule,
                content = listOf(
                    Room.RoomRule("标准", "素将局"),
                    Room.RoomRule("标准", "阴间局")
                )
            )
        }
        if (!FileUtils.isExist(
                itemName = FileUtils.ItemName.Blacklist
            )
        ) {
            FileUtils.write(
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.Blacklist,
                content = listOf<Room.Member>()
            )
        }

        Log.i(
            TAG,
            "initZTConfig: succeed to init ztNetworkConfig ztNetwork ztMoon appSetting roomRule blacklist"
        )
    }

    private fun saveZTConfig() {
        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.NetworkConfig,
            content = ztNetworkConfig.value
        )
        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Network,
            content = ztNetwork.value
        )
        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Moon,
            content = ztMoon.value
        )

        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting,
            content = appSetting.value
        )
        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.RoomRule,
            content = roomRule.value
        )
        FileUtils.write(
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Blacklist,
            content = blacklist.value
        )
        Log.i(
            TAG,
            "saveZTConfig: succeed to save ztNetworkConfig ztNetwork ztMoon appSetting roomRule blacklist"
        )
    }

    private fun updateNetworkStatus(
        context: Context,
        networkId: String,
        networkName: String? = null,
        lastActivated: Boolean? = null,
    ) {
        ztNetwork.value.first {
            it.networkId == networkId
        }.let { net ->
            networkName?.run { net.networkName = networkName }
            lastActivated?.run { net.lastActivated = lastActivated }

        }
        Log.i(TAG, "updateNetworkStatus: start to saveZTConfig")
        saveZTConfig()
    }

    fun getMembers(networkID: String) {
        try {
            NetworkRepository.ztClient.getMembers(networkID)
                .enqueue(object : Callback<List<Member>> {
                    override fun onResponse(
                        call: Call<List<Member>>, response: Response<List<Member>>
                    ) {
                        try {
                            response.body()?.let { it ->
                                members.value = it.filter {
                                    it.config.ipAssignments.isNotEmpty()
                                }.sortedBy { -it.lastSeen }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                        t.printStackTrace()
                        Log.i(TAG, "onFailure: request wrong")
                    }
                })
        } catch (e: Throwable) {
            e.printStackTrace()
        }


    }

    fun modifyMember(networkID: String, memberID: String, name: String, description: String) {
        val modifyMember = ModifyMember(name = name, description = description)
        NetworkRepository.ztClient.modifyMember(networkID, memberID, modifyMember)
            .enqueue(object : Callback<Member> {
                override fun onResponse(call: Call<Member>, response: Response<Member>) {
                    response.body()?.let {
                        val result = it.name == name && it.description == description
                        Log.i("modifyMember", "result: $result")
                        members.value.first { members ->
                            members.id == memberID
                        }.let { member ->
                            member.name = it.name
                            member.description = it.description
                        }
                    }
                }

                override fun onFailure(call: Call<Member>, t: Throwable) {
                    t.printStackTrace()
                    println("request wrong")
                }
            })
    }

}