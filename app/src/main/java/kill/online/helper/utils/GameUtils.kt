package kill.online.helper.utils


enum class KillPacketType {
    ROOM_BROADCAST, REQUEST_ENTER_ROOM, REPLY_ENTER_ROOM, REQUEST_QUIT_ROOM, REPLY_QUIT_ROOM,

}

fun getPacketType(tcpData: ByteArray): KillPacketType {
    return KillPacketType.ROOM_BROADCAST
}

