package kill.online.helper.utils

import kill.online.helper.zeroTier.util.IPPacketUtils
import kill.online.helper.zeroTier.util.IPPacketUtils.getUDPData
import java.net.InetAddress


enum class KillPacketType {
    UNKNOWN, NO_IP_ASSIGNED,
    ROOM_BROADCAST, REQUEST_ENTER_ROOM, REPLY_ENTER_ROOM, REQUEST_QUIT_ROOM, REPLY_QUIT_ROOM,
}

const val BROADCAST_ADDRESS = "255.255.255.255"

fun getPacketType(ipv4: ByteArray, networkPrefix: InetAddress?): KillPacketType {
    val ipVersion = IPPacketUtils.getIPVersion(ipv4)
    val sourceAddress = IPPacketUtils.getSourceIP(ipv4)?.hostAddress
    val destAddress = IPPacketUtils.getDestIP(ipv4)?.hostAddress
    val udpData = getUDPData(ipv4)

    return when {
        networkPrefix == null -> KillPacketType.NO_IP_ASSIGNED

        destAddress == BROADCAST_ADDRESS -> KillPacketType.ROOM_BROADCAST

        else -> KillPacketType.UNKNOWN

    }


}

