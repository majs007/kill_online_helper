package kill.online.helper.repository

import kill.online.helper.data.Member
import kill.online.helper.data.ModifyMember
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ZeroTierService {
    @GET("network/{networkID}/member")
    fun getMembers(@Path("networkID") networkID: String): Call<List<Member>>

    @POST(value = "network/{networkID}/member/{memberID}")
    fun modifyMember(
        @Path("networkID") networkID: String,
        @Path("memberID") memberID: String,
        @Body body: ModifyMember
    ): Call<Member>
}

