package kill.online.helper.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.data.Room
import kill.online.helper.server.HttpServer
import kill.online.helper.server.HttpServer.Companion.HTTP_PORT_SERVER
import kill.online.helper.server.HttpServer.Companion.URI_MESSAGE
import kill.online.helper.server.MessageService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppViewModel : ViewModel() {
    private val TAG = "AppViewModel"
    val isHTTPRunning = mutableStateOf(false)

    private lateinit var msgServer: HttpServer
    private val msgConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@AppViewModel.msgServer =
                (iBinder as MessageService.MessageBinder).server
            msgServer.setOnReceivedMessage {
                val newMessages = messages.value.toMutableList()
                newMessages.add(it)
                messages.value = newMessages.toList()
                MessageResponse()
            }
            iBinder.setCallBack(onStart = { isHTTPRunning.value = true },
                onStop = { isHTTPRunning.value = false })
            Log.i(TAG, "onServiceConnected: succeed to bind msg Service")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind msg Service")
        }
    }

    var isAddRule = mutableStateOf(false)
    val roomRule = mutableStateOf(listOf<Room.RoomRule>())
    val messages = mutableStateOf(listOf<Message>())


    /*    fun loadRoomRule(context: Context) {
            roomRule.value = FileUtils.read(
                context = context,
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.RoomRule,
                defValue = listOf()
            )
        }

        fun addRoomRule(mode: String, rule: String, context: Context? = null) {
            val newRoomRule = roomRule.value.toMutableList()
            newRoomRule.add(RoomRule(mode, rule))
            roomRule.value = newRoomRule.toList()
            context?.let { saveRoomRule(it) }
        }

        fun updateRoomRule(index: Int, context: Context? = null, also: (it: RoomRule) -> RoomRule) {
            val newRoomRule = roomRule.value.toMutableList()
            newRoomRule[index] = also(newRoomRule[index])
            roomRule.value = newRoomRule.toList()
            context?.let { saveRoomRule(it) }
        }

        fun removeRoomRule(index: Int, context: Context? = null) {
            val newRoomRule = roomRule.value.toMutableList()
            newRoomRule.removeAt(index)
            roomRule.value = newRoomRule.toList()
            context?.let { saveRoomRule(it) }

        }

        private fun saveRoomRule(context: Context) {
            FileUtils.write(
                context = context,
                dataType = FileUtils.DataType.Json,
                itemName = FileUtils.ItemName.RoomRule,
                content = roomRule.value,
            )
        }*/
    fun getCheckedRuleIndex(): Int = roomRule.value.indexOfFirst { it.checked }

    fun sendMessage(ip: String, msg: Message) {
        NetworkRepository.appClient.sendMessage(
            "http://$ip:${HTTP_PORT_SERVER}${URI_MESSAGE}",
            msg
        )
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    val body = response.body()
                    Log.i(TAG, "onResponse: $body")
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    t.printStackTrace()
                    Log.w(TAG, "onFailure: sendMessage failed")
                }
            })
    }

    fun startMsgServer(context: Context) {
        val msgIntent = Intent(context, MessageService::class.java)
        context.bindService(
            msgIntent, this.msgConnection,
            Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        context.startService(msgIntent)

    }

    fun stopMsgServer(context: Context) {
        val msgIntent = Intent(context, MessageService::class.java)
        if (context.stopService(msgIntent)) {
            Log.e(TAG, "stopMsgServer returned false")
        }
        context.unbindService(msgConnection)
    }
}