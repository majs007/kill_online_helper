package kill.online.helper.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.data.RoomRule
import kill.online.helper.server.HttpServer
import kill.online.helper.server.URI_MESSAGE
import kill.online.helper.utils.FileUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppViewModel : ViewModel() {
    private val TAG = "AppViewModel"
    private val httpServer = HttpServer()
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")
    var isAddRule = mutableStateOf(false)

    val roomRule = mutableStateOf(listOf<RoomRule>())

    fun getCheckedRuleIndex(): Int {
        return roomRule.value.indexOfFirst { it.checked }
    }


    fun loadRoomRule(context: Context) {
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
    }

    fun sendMessage(ip: String, msg: Message) {
        NetworkRepository.appClient.sendMessage("http://$ip/$URI_MESSAGE", msg)
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
                    println("request wrong")
                }
            })
    }

    fun startHttpServer() {
        httpServer.onReceivedMessage {

            MessageResponse()
        }
        httpServer.start()
    }

    fun stopHttpServer() {
        httpServer.stop()
    }
}