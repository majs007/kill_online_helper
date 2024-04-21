package kill.online.helper.repository

import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.service.URI_MESSAGE
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AppClient {
    @POST(value = "{ip}$URI_MESSAGE")
    fun sendMessage(@Path("ip") ip: String, @Body body: Message): Call<MessageResponse>
}