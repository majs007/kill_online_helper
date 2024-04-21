package kill.online.helper.data


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
