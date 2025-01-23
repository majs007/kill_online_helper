package kill.online.helper.utils

import android.util.Log
import kill.online.helper.server.HttpServer
import kill.online.helper.zeroTier.util.IPPacketUtils
import kill.online.helper.zeroTier.util.IPPacketUtils.isTCP


enum class KillPacketType {
    UNKNOWN,
    TCP_SYNC, TCP_RST,
    APP_CLIENT, APP_SERVER,
    ROOM_BROADCAST,
    START_GAME, END_GAME,
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
    val TAG = "getServerPacketType"
    var tcpData = ByteArray(0)
    val destAddress = IPPacketUtils.getDestIP(ipv4)
    var tcpSourcePort = 0
    var tcpDestPort = 0
    var isTCPRst = false
    if (isTCP(ipv4)) {
        tcpSourcePort = IPPacketUtils.getTCPSourcePort(ipv4)
        tcpDestPort = IPPacketUtils.getTCPDestPort(ipv4)
        tcpData = IPPacketUtils.getTCPData(ipv4)
        isTCPRst = IPPacketUtils.isRST(ipv4)
        var msgType = -1
        if (tcpData.size > 17) {
            msgType = tcpData[16].toInt()
        }
        Log.i(
            TAG,
            "msgType: $msgType isTCPRst:$isTCPRst tcpSourcePort:$tcpSourcePort tcpDestPort:$tcpDestPort tcpData:${tcpData.toHexString()}"
        )
    }

    return when {
        //目的地址为广播地址--->房间广播包
        destAddress?.hostAddress == BROADCAST_ADDRESS -> KillPacketType.ROOM_BROADCAST

        tcpSourcePort == HttpServer.HTTP_PORT_SERVER -> KillPacketType.APP_SERVER

        tcpDestPort == HttpServer.HTTP_PORT_SERVER -> KillPacketType.APP_CLIENT

        tcpSourcePort == 7625 && isTCPRst -> KillPacketType.TCP_RST

        tcpData.size > 17 && tcpData[16].toInt() == 0x01 -> KillPacketType.ENTER_ROOM_SUCCESS

        tcpData.size > 17 && tcpData[16].toInt() == 0x03 -> KillPacketType.QUIT_ROOM_SUCCESS

        tcpData.size > 500 && tcpData[16].toInt() == 0x00 -> KillPacketType.START_GAME

        tcpData.size > 17 && tcpData[16].toInt() == 0x63 -> KillPacketType.END_GAME

        else -> KillPacketType.UNKNOWN
    }
}

fun getClientPacketType(ipv4: ByteArray): KillPacketType {
    val TAG = "getClientPacketType"
    var tcpData = ByteArray(0)
    var tcpSourcePort = 0
    var tcpDestPort = 0
    var isTCPSync = false
    if (isTCP(ipv4)) {
        tcpSourcePort = IPPacketUtils.getTCPSourcePort(ipv4)
        tcpDestPort = IPPacketUtils.getTCPDestPort(ipv4)
        tcpData = IPPacketUtils.getTCPData(ipv4)
        isTCPSync = IPPacketUtils.isSYN(ipv4)
        var msgType = -1
        if (tcpData.size > 17) {
            msgType = tcpData[16].toInt()
        }
        Log.i(
            TAG,
            "msgType: $msgType isTCPSync:$isTCPSync tcpSourcePort:$tcpSourcePort tcpDestPort:$tcpDestPort tcpData:${tcpData.toHexString()}"
        )
    }

    return when {
        tcpSourcePort == HttpServer.HTTP_PORT_SERVER -> KillPacketType.APP_SERVER

        tcpDestPort == HttpServer.HTTP_PORT_SERVER -> KillPacketType.APP_CLIENT

        tcpDestPort == 7625 && isTCPSync -> KillPacketType.TCP_SYNC

        tcpData.size > 17 && tcpData[16].toInt() == 0x00 -> KillPacketType.REQUEST_ENTER_ROOM

        tcpData.size > 17 && tcpData[16].toInt() == 0x03 -> KillPacketType.REQUEST_QUIT_ROOM

        else -> KillPacketType.UNKNOWN

    }
}

