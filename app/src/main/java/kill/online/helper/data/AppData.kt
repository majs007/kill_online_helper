package kill.online.helper.data

data class AppSetting(
    val useCellularData: Boolean = true,
    val disableIpv6: Boolean = false,
    val roomSetting: RoomSetting = RoomSetting(),
) {
    data class RoomSetting(
        val customRoomName: Boolean = true,
        val roomName: String = "hello🥳",
        val isPrivateRoom: Boolean = false,
        val roomPassword: String = ""
    )
}

data class Message(
    val playerName: String,
    val msg: String,
    val type: MsgType = MsgType.TEXT,
    val suffix: MsgSuffix = MsgSuffix.TXT,
    val time: Long = System.currentTimeMillis(),
)

data class MessageResponse(val result: Result = Result.OK)
enum class MsgType {
    TEXT, IMAGE, AUDIO
}

enum class MsgSuffix {
    TXT,
    JPEG, JPG, PNG, GIF,
    MP3,
}

enum class Result {
    OK, FAILED
}


data class Room(
    var roomName: String = "",
    val roomPassword: String = "",
    var isPrivateRoom: Boolean = false,
    var roomOwner: String = "",
    var roomOwnerIp: String = "",
    var roomRule: RoomRule = RoomRule(),
    var state: RoomState = RoomState.WAITING,
    var players: List<Member> = listOf()
) {
    data class Member(
        val name: String = "",
        val ip: String = "",
    )

    data class RoomRule(var mode: String = "", var rule: String = "", var checked: Boolean = false)
    enum class RoomState {
        WAITING, PLAYING
    }
}

