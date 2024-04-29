package kill.online.helper.utils

import kill.online.helper.zeroTier.util.IPPacketUtils
import kill.online.helper.zeroTier.util.IPPacketUtils.getUDPData


enum class KillPacketType {
    UNKNOWN,
    ROOM_BROADCAST,
    REQUEST_ENTER_ROOM, ENTER_ROOM_SUCCESS,
    REQUEST_QUIT_ROOM, QUIT_ROOM_SUCCESS,
}

const val BROADCAST_ADDRESS = "255.255.255.255"

/**
 * Get the type of game packet
 * @param ipv4 the ipv4 packet data
 * @return the type of game packet
 */
fun getServerPacketType(ipv4: ByteArray): KillPacketType {
    val destAddress = IPPacketUtils.getDestIP(ipv4)
    val udpData = getUDPData(ipv4)

    return when {
        //目的地址为广播地址--->房间广播包
        destAddress?.hostAddress == BROADCAST_ADDRESS -> KillPacketType.ROOM_BROADCAST

        udpData[16] == 0x01.toByte() -> KillPacketType.ENTER_ROOM_SUCCESS

        udpData[16] == 0x03.toByte() -> KillPacketType.QUIT_ROOM_SUCCESS

        else -> KillPacketType.UNKNOWN

    }
}

fun getClientPacketType(ipv4: ByteArray): KillPacketType {
    val udpData = getUDPData(ipv4)
    return when {
        udpData[16] == 0x00.toByte() -> KillPacketType.REQUEST_ENTER_ROOM

        udpData[16] == 0x03.toByte() -> KillPacketType.REQUEST_QUIT_ROOM

        else -> {
            KillPacketType.UNKNOWN
        }
    }
}

