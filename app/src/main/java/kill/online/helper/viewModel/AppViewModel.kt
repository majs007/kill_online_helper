package kill.online.helper.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.repository.NetworkRepository
import kill.online.helper.service.HttpServer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppViewModel : ViewModel() {
    private val httpServer = HttpServer()
    val TAG = "AppViewModel"
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")

    fun sendMessage(ip: String, msg: Message) {
        NetworkRepository.appClient.sendMessage(ip, msg)
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