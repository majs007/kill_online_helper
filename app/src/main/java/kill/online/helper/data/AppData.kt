package kill.online.helper.data

data class AppSetting(
    val useCellularData: Boolean = true,
    val disableIpv6: Boolean = false,
)

data class Message(
    val playerName: String,
    val msg: String,
    val type: MsgType = MsgType.TEXT,
    val suffix: MsgSuffix = MsgSuffix.TXT,
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

data class RoomRule(var mode: String, var rule: String, var checked: Boolean = false)
