package kill.online.helper.data

import androidx.compose.ui.geometry.Offset

data class AppSetting(
    var useCellularData: Boolean = true,
    var disableIpv6: Boolean = false,
    val roomSetting: RoomSetting = RoomSetting(),
    val fwRoomSetting: FWRoomSetting = FWRoomSetting(),
) {
    data class RoomSetting(
        var customRoomName: Boolean = true,
        var roomName: String = "🏠",
        var isPrivateRoom: Boolean = false,
        var roomPassword: String = "",
        var enableBlackList: Boolean = false,
    )

    //FloatingWindow
    data class FWRoomSetting(
        var autoPlayAudio: Boolean = true,
    )
}

data class Message(
    val playerName: String = "",
    val msg: String = "",
    val type: MsgType = MsgType.TEXT,
    //编码格式
    val suffix: MsgSuffix = MsgSuffix.TXT,
    val isDrag: Boolean = false,
    val imagePositionRadio: Offset = Offset(0f, 0f),
    val audioDuration: Int = 0,
    val timeStamp: Long = System.currentTimeMillis(),
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


data object AppSettingItem {
    const val ADVANCED_NETWORK_SETTING = "高级网络设置"
    const val USE_CELLULAR_DATA = "使用移动数据"
    const val DISABLE_IPV6 = "禁用Ipv6"

    const val ROOM_SETTING = "房间设置"
    const val CUSTOM_ROOM_NAME = "自定义房间名"
    const val ROOM_PASSWORD = "房间密码"
    const val BLACK_LIST = "黑名单"

    const val FW_ROOM_SETTING = "悬浮窗设置"
    const val AUTO_PLAY_AUDIO = "自动播放音频"
    const val MANAGE_EMOJI = "管理表情包"

    const val ABOUT = "关于"
    const val CHECK_UPDATE = "检查更新"
}

