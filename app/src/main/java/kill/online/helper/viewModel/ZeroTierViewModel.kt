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
import kill.online.helper.utils.getPacketType
import kill.online.helper.zeroTier.model.UserNetwork
import kill.online.helper.zeroTier.model.UserNetworkConfig
import kill.online.helper.zeroTier.service.ZeroTierOneService
import kill.online.helper.zeroTier.util.IPPacketUtils.getDestIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getIPVersion
import kill.online.helper.zeroTier.util.IPPacketUtils.getSourceIP
import kill.online.helper.zeroTier.util.IPPacketUtils.getUDPData
import kill.online.helper.zeroTier.util.IPPacketUtils.handleUDPData
import kill.online.helper.zeroTier.util.InetAddressUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.net.InetSocketAddress

@SuppressLint("StaticFieldLeak")
class ZeroTierViewModel : ViewModel() {
    private val TAG = "ZeroTierViewModel"
    val appViewModel: AppViewModel = AppViewModel()
    val isZTRunning = mutableStateOf(false)
    private lateinit var ztService: ZeroTierOneService
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

    private lateinit var ztNetworkConfig: List<UserNetworkConfig>
    private lateinit var ztNetwork: List<UserNetwork>
    private lateinit var appSetting: AppSetting

    val members = mutableStateOf(listOf<Member>())

    val rooms = mutableStateOf(listOf<Room>())
    val enteredRoom = mutableStateOf(Room())
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


    private fun setGamePacketCallBack() {
        ztService.setOnHandleIPPacket { ipv4 ->
            val hexIpv4 = ipv4.joinToString(separator = ", ") { "0x" + String.format("%02X", it) }
            val udpData = getUDPData(ipv4)
            val hexUdpData =
                udpData.joinToString(separator = ", ") { "0x" + String.format("%02X", it) }
            val ipVersion = getIPVersion(ipv4)
            val sourceAddress = getSourceIP(ipv4)?.hostAddress
            val destAddress = getDestIP(ipv4)?.hostAddress
            val assignedIP = getAssignedIP()
            val networkPrefix =
                InetAddressUtils.addressToNetworkPrefix(assignedIP?.address, assignedIP?.port)
            val gamePacketType = getPacketType(ipv4, networkPrefix)
            Log.i(
                "GamePacketCallBack",
                "hexIpv4: $hexIpv4\nudpData: $hexUdpData\nipVersion: $ipVersion sourceAddress: $sourceAddress destAddress: $destAddress gamePacketType: $gamePacketType"
            )

            when (gamePacketType) {
                KillPacketType.ROOM_BROADCAST -> {
                    onRoomBroadcast(ipv4)
                }

                KillPacketType.REQUEST_ENTER_ROOM -> {
                    onRequestEnterRoom(ipv4)
                }

                KillPacketType.REPLY_ENTER_ROOM -> {
                    onReplyEnterRoom(ipv4)
                }

                else -> {
                    ipv4
                }
            }

        }
    }

    private fun onRoomBroadcast(ipv4: ByteArray): ByteArray {
        val assignedIP = getAssignedIP()?.address?.hostAddress ?: ""
        val sourceIP = getSourceIP(ipv4)?.hostAddress
        val udpData = getUDPData(ipv4)
        val roomName = String(udpData.dropLast(1).toByteArray(), Charsets.UTF_8)
        val roomOwner = members.value.first {
            it.config.ipAssignments.first() == sourceIP
        }.name
        //查找是否有相同的房间
        val roomIndex = rooms.value.indexOfFirst { room -> room.roomOwnerIp == sourceIP }
        //自己发出的广播
        if (assignedIP == sourceIP) {
            //没有找到相同的房间，说明是首次广播
            if (roomIndex == -1) {
                val newRoom = Room(
                    roomName = if (appSetting.roomSetting.customRoomName) appSetting.roomSetting.roomName else "${roomOwner}的房间",
                    roomOwner = roomOwner,
                    roomOwnerIp = sourceIP ?: "",
                    isPrivateRoom = appSetting.roomSetting.isPrivateRoom,
                    roomPassword = appSetting.roomSetting.roomPassword,
                    roomRule = appViewModel.roomRule.value.first { it.checked },
                    state = Room.RoomState.WAITING,
                    players = listOf(Room.Member(name = roomOwner, ip = sourceIP ?: ""))
                )
                //更新 enteredRoom
                enteredRoom.value = newRoom
                StateUtils.add(itemName = FileUtils.ItemName.Room, state = rooms, newItem = newRoom)
            }
            //找到相同的房间，说明是第二次发出广播，更新房间信息
            else {
                update(itemName = FileUtils.ItemName.Room, state = rooms, index = roomIndex) {
                    it.roomName =
                        if (appSetting.roomSetting.customRoomName) appSetting.roomSetting.roomName else "${roomOwner}的房间"
                    it.roomOwner = roomOwner
                    it.roomRule = appViewModel.roomRule.value.first { it.checked }
                    it.isPrivateRoom = appSetting.roomSetting.isPrivateRoom
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
        else {
            val newRoom = roomNameToObject(roomName)
            //找到相同的房间，说明是第一次收到别人发出的广播
            if (roomIndex == -1) {
                newRoom.roomOwnerIp = sourceIP ?: ""
                newRoom.players = listOf(Room.Member(name = roomOwner, ip = sourceIP ?: ""))
                StateUtils.add(itemName = FileUtils.ItemName.Room, state = rooms, newItem = newRoom)

            }
            //找到相同的房间，说明是第二次收到别人发出的广播，更新房间信息
            else {
                update(itemName = FileUtils.ItemName.Room, state = rooms, index = roomIndex) {
                    it.roomName = newRoom.roomName
                    it.roomOwner = newRoom.roomOwner
                    it.roomRule = newRoom.roomRule
                    it.state = newRoom.state
                    it.isPrivateRoom = newRoom.isPrivateRoom
                    it.copy()
                }
            }
            return ipv4
        }
    }

    private fun onRequestEnterRoom(ipv4: ByteArray): ByteArray {
        val wrongIpv4 = ipv4.copyOf()
        listOf<Byte>(-1, -1, -1, -1).toByteArray().copyInto(wrongIpv4, 12)
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        getAssignedIP()?.hostName.let { it ->
            //如果是自己发出的请求，自己请求加入别人的房间
            if (it == sourceIP) {
                val room = rooms.value.firstOrNull { room -> room.roomOwnerIp == sourceIP }
                room?.let {
                    return handleUDPData(ipv4) { udp -> udp + it.roomPassword.toByteArray() }
                }
            }
            //如果是别人发出的请求，别人请求加入自己的房间
            else {
                //在黑名单里，使数据包无效
                if (sourceIP in blacklist.value.map { it.ip }) {
                    return wrongIpv4
                }
                //是否开启了密码房
                if (appSetting.roomSetting.isPrivateRoom) {
                    val password = ipv4.drop(ipv4.size - appSetting.roomSetting.roomPassword.length)
                    //密码错误，使数据包无效
                    return if (!password.toByteArray()
                            .contentEquals(appSetting.roomSetting.roomPassword.toByteArray())
                    ) wrongIpv4
                    //密码正确，使数据包有效
                    else ipv4.dropLast(appSetting.roomSetting.roomPassword.length).toByteArray()
                } else {
                }
            }
        }
        return ipv4
    }

    private fun onReplyEnterRoom(ipv4: ByteArray): ByteArray {
        val sourceIP = getSourceIP(ipv4)?.hostAddress ?: ""
        val destIP = getDestIP(ipv4)?.hostAddress ?: ""
        //查找是否有相同的房间
        val room = rooms.value.first { room -> room.roomOwnerIp == sourceIP }
        getAssignedIP()?.hostName.let { it ->
            //如果是别人发出的请求，别人同意自己加入其房间，更新 enteredRoom
            if (it == destIP) enteredRoom.value = room
        }
        return ipv4
    }


    fun loadZTConfig(context: Context) {
        ztNetworkConfig = FileUtils.read(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.NetworkConfig,
            defValue = listOf()
        )
        ztNetwork = FileUtils.read(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Network,
            defValue = listOf()
        )
        appSetting = FileUtils.read(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting,
            defValue = AppSetting()
        )
        Log.i(TAG, "loadZTConfig: succeed to load ztNetworkConfig ztNetwork appSetting")
    }

    fun getLastActivatedNetworkId(): String {
        return ztNetwork.first {
            it.lastActivated
        }.networkId
    }

    private fun saveZTConfig(context: Context) {
        FileUtils.write(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.NetworkConfig,
            content = ztNetworkConfig
        )
        FileUtils.write(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.Network,
            content = ztNetwork
        )
        FileUtils.write(
            context = context,
            dataType = FileUtils.DataType.Json,
            itemName = FileUtils.ItemName.AppSetting,
            content = appSetting
        )
        Log.i(TAG, "saveZTConfig: succeed to save ztNetworkConfig ztNetwork appSetting")
    }

    fun initZTConfig(context: Context) {
        if (!FileUtils.isExist(
                context = context, itemName = FileUtils.ItemName.NetworkConfig
            )
        ) {
            FileUtils.write(
                context = context,
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.NetworkConfig,
                content = listOf(UserNetworkConfig("a09acf02339ffab1")),
            )
        }
        if (!FileUtils.isExist(
                context = context, itemName = FileUtils.ItemName.Network
            )
        ) {
            FileUtils.write(
                context = context,
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.Network,
                content = listOf(UserNetwork("a09acf02339ffab1", lastActivated = true)),
            )
        }
        if (!FileUtils.isExist(
                context = context, itemName = FileUtils.ItemName.AppSetting
            )
        ) {
            FileUtils.write(
                context = context,
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.AppSetting,
                content = AppSetting()
            )
        }

        Log.i(TAG, "initZTConfig: succeed to init ztNetworkConfig ztNetwork appSetting")
    }

    private fun updateNetworkStatus(
        context: Context,
        networkId: String,
        networkName: String? = null,
        lastActivated: Boolean? = null,
    ) {
        ztNetwork.first {
            it.networkId == networkId
        }.let { net ->
            networkName?.run { net.networkName = networkName }
            lastActivated?.run { net.lastActivated = lastActivated }

        }
        Log.i(TAG, "updateNetworkStatus: start to saveZTConfig")
        saveZTConfig(context)
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