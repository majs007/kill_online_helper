package kill.online.helper.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kill.online.helper.data.Member
import kill.online.helper.data.ModifyMember
import kill.online.helper.repository.NetworkRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ZeroTierViewModel : ViewModel() {
    val members = mutableStateOf(listOf<Member>())

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