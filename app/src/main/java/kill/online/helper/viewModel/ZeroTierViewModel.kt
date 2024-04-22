package kill.online.helper.viewModel

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.Member
import kill.online.helper.data.ModifyMember
import kill.online.helper.utils.KillPacketType
import kill.online.helper.utils.getPacketType
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

    val members = mutableStateOf(listOf<Member>())

    fun startZeroTier(context: Context, networkId: Long) {
        val prepare = VpnService.prepare(context)
        if (prepare != null) {
            // 等待 VPN 授权后连接网络
            //vpnAuthLauncher.launch(prepare)
            return
        }
        Log.i(TAG, "Intent is NULL.  Already approved.")
        val intent = Intent(
            context,
            ZeroTierOneService::class.java
        )
        intent.putExtra(ZeroTierOneService.ZT_NETWORK_ID, networkId)
        //绑定服务
        context.bindService(
            intent, this.ztConnection,
            Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        //启动服务
        context.startService(intent)
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