package kill.online.helper.data

import androidx.compose.ui.geometry.Offset

data class AppSetting(
    var useCellularData: Boolean = true,
    var disableIpv6: Boolean = false,
    var playerName: String = "未知",
    val roomSetting: RoomSetting = RoomSetting(),
    var stickerManage: List<Sticker> = listOf(),
    val fwRoomSetting: FWRoomSetting = FWRoomSetting(),
    var isUserAgreed: Boolean = false,
) {
    data class RoomSetting(
        var isCustomRoomName: Boolean = true,
        var roomName: String = "",
        var isPrivateRoom: Boolean = false,
        var roomPassword: String = "",
        var enableBlackList: Boolean = false,
        var blackList: List<String> = listOf(),
    )

    //FloatingWindow
    data class FWRoomSetting(
        var autoPlayAudio: Boolean = true,
        var enableBulletMessage: Boolean = true,
        var maxBulletMessage: Int = 4,
    )
}

data class Sticker(
    val name: String = "",
    val type: StickerType = StickerType.LOCAL,
    var usageCounter: Int = 0,
    val enable: Boolean = true,
) {
    enum class StickerType {
        LOCAL, IMPORT, ONLINE
    }
}


data class Message(
    val playerName: String = "",
    val msg: String = "",
    val msgType: MsgType = MsgType.TEXT,
    val stickerState: StickerState = StickerState.SEND,
    val imagePositionRadio: Offset = Offset(0f, 0f),
    val audioDuration: Float = 0f,
    var alignEnd: Boolean = true,
    val timeStamp: Long = System.currentTimeMillis(),
) {
    data class ConfigItem(
        val key: String = "",
        val value: String = "",
    )
}

data class MessageResponse(val result: Result = Result.OK)
enum class MsgType {
    TEXT, EMOJI, STICKER, AUDIO, CONFIG
}

enum class Result {
    OK, FAILED
}

enum class StickerState {
    SEND, RECEIVE
}


data class Room(
    // 房间名
    var roomName: String = "",
    // 是否是私人房间
    var isPrivateRoom: Boolean = false,
    // 房间密码
    val roomPassword: String = "",
    // 房主
    var roomOwner: String = "",
    // 房主ip
    var roomOwnerIp: String = "",
    // 房间玩的游戏模式
    var roomRule: RoomRule = RoomRule(),
    // 当前房间游戏状态
    var state: RoomState = RoomState.WAITING,
    // 房间内的玩家
    var players: List<RoomMember> = listOf(),
    //禁言表
    var banSendMsgList: List<String> = listOf(),
    //禁贴纸表
    var banSendStickerList: List<String> = listOf(),
    //全员禁言
    var banAllMessage: Boolean = false,
    //全员禁贴纸
    var banAllSticker: Boolean = false,
    //启用黑名单
    var enableBlackList: Boolean = false,
    // 房间黑名单
    var blackList: List<String> = listOf(),
    // 时间戳
    var timeStamp: Long = System.currentTimeMillis(),
) {

    data class RoomMember(
        // 玩家名
        val name: String = "",
        // 玩家虚拟局域网ip
        val ip: String = "",
    )

    data class RoomRule(
        var mode: String = "",
        var rule: String = "",
        var checked: Boolean = false
    )

    enum class RoomState {
        WAITING, PLAYING
    }
}


data object AppSettingItem {
    const val ADVANCED_NETWORK_SETTING = "高级网络设置"
    const val USE_CELLULAR_DATA = "使用移动数据"
    const val DISABLE_IPV6 = "禁用Ipv6"
    const val ZT_NETWORK_SETTING = "ZT网络设置"
    const val ZT_MOON_SETTING = "ZT Moon设置"
    const val MOON_WORLD_ID = "输入 Moon ID"

    const val ROOM_SETTING = "房间设置"
    const val CUSTOM_ROOM_NAME = "自定义房间名"
    const val ROOM_PASSWORD = "房间密码"
    const val BLACK_LIST = "黑名单"
    const val PLAYER_NAME = "玩家名"

    const val FW_ROOM_SETTING = "悬浮窗设置"
    const val BULLET_MESSAGE = "弹幕消息"
    const val AUTO_PLAY_AUDIO = "自动播放音频"
    const val MANAGE_STICKER = "管理表情包"
    const val MAX_BULLET_MESSAGE = "最多弹幕数"
    const val BAN_ALL_MESSAGE = "全员禁言"
    const val BAN_ALL_STICKER = "全员禁贴纸"
    const val STICKER_INTERVAL = "贴纸发送间隔"
    const val MESSAGE_INTERVAL = "消息发送间隔"

    const val OTHER = "其他"
    const val DEVELOPER = "开发者"
    const val HELP = "帮助"
    const val OPEN_SOURCE = "开源许可"
    const val CHECK_UPDATE = "检查更新"
}

