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
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.KillPacketType
import kill.online.helper.utils.getPacketType
import kill.online.helper.zeroTier.model.UserNetwork
import kill.online.helper.zeroTier.model.UserNetworkConfig
import kill.online.helper.zeroTier.service.ZeroTierOneService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("StaticFieldLeak")
class ZeroTierViewModel : ViewModel() {
    private val TAG = "ZeroTierViewModel"
    private lateinit var ztService: ZeroTierOneService
    private var isBound: Boolean = false
    private val ztConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@ZeroTierViewModel.ztService =
                (iBinder as ZeroTierOneService.ZeroTierBinder).service
            isBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            this@ZeroTierViewModel.isBound = false
        }
    }

    private lateinit var ztNetworkConfig: List<UserNetworkConfig>
    private lateinit var ztNetwork: List<UserNetwork>
    private lateinit var appSetting: AppSetting

    val members = mutableStateOf(listOf<Member>())
    val isZTRunning = mutableStateOf(false)

    fun startZeroTier(context: Context, networkId: Long = getLastActivatedNetworkId().toLong()) {
        //加载配置
        loadZTConfig(context)
        //初始化 VPN 授权
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            // 打开系统设置界面
            startActivityForResult(context as Activity, vpnIntent, 0, null)
        }
        Log.i(TAG, "Intent is NULL.  Already approved.")
        val ztIntent = Intent(
            context,
            ZeroTierOneService::class.java
        )
        ztIntent.putExtra(ZeroTierOneService.ZT_NETWORK_ID, networkId)
        //绑定服务
        context.bindService(
            ztIntent, this.ztConnection,
            Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        //启动服务
        context.startService(ztIntent)
        setGamePacketCallBack()
    }

    fun stopZeroTier(context: Context) {
        ztService.stopZeroTier()
        val intent = Intent(context, ZeroTierOneService::class.java)
        if (context.stopService(intent)) {
            Log.e(TAG, "stopService returned false")
        }
        context.unbindService(ztConnection)
    }

    private fun setGamePacketCallBack() {
        ztService.setOnHandleIPPacket { ipv4 ->
            val gamePacketType = getPacketType(ipv4.drop(40).toByteArray())
            when (gamePacketType) {
                KillPacketType.ROOM_BROADCAST -> {
                    onRoomBroadcast(ipv4)
                }

                KillPacketType.REQUEST_ENTER_ROOM -> {
                    onRequestEnterRoom(ipv4)
                }

                KillPacketType.REQUEST_QUIT_ROOM -> {
                    onRequestQuitRoom(ipv4)
                }

                else -> {}
            }
            ipv4
        }
    }

    private fun onRoomBroadcast(ipv4: ByteArray) {

    }

    private fun onRequestEnterRoom(ipv4: ByteArray) {

    }

    private fun onRequestQuitRoom(ipv4: ByteArray) {

    }

    private fun loadZTConfig(context: Context) {
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

    fun updateNetworkStatus(
        context: Context,
        networkId: String,
        networkName: String? = null,
        enabled: Boolean? = null,
        lastActivated: Boolean? = null,
    ) {
        ztNetwork.first {
            it.networkId == networkId
        }.let { net ->
            networkName?.run { net.networkName = networkName }
            enabled?.run { net.enabled = enabled }
            lastActivated?.run { net.lastActivated = lastActivated }

        }
        saveZTConfig(context)
    }

    fun getMembers(networkID: String) {
        NetworkRepository.zeroTier.getMembers(networkID).enqueue(object : Callback<List<Member>> {
            override fun onResponse(call: Call<List<Member>>, response: Response<List<Member>>) {
                response.body()?.let { it ->
                    members.value = it.toMutableList()
                }
            }

            override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                t.printStackTrace()
                println("request wrong")
            }
        })
    }

    fun modifyMember(networkID: String, memberID: String, name: String, description: String) {
        val modifyMember = ModifyMember(name = name, description = description)
        NetworkRepository.zeroTier.modifyMember(networkID, memberID, modifyMember)
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