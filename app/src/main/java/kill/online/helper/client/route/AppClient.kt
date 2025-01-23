package kill.online.helper.client.route

import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface AppClient {
    @POST
    fun sendMessage(@Url url: String, @Body body: Message): Call<MessageResponse>

    @GET
    fun download(@Url url: String): Call<ResponseBody>
}