package kill.online.helper.zeroTier.util

import android.util.Log
import kill.online.helper.utils.toByteArray
import kill.online.helper.utils.toHexString
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import kotlin.random.Random


object IPPacketUtils {
    private const val TAG = "IPPacketUtils"
    fun getIPVersion(bArr: ByteArray): Byte {
        return (bArr[0].toInt() shr 4).toByte()
    }

    fun getSourceIP(bArr: ByteArray): InetAddress? {
        val iPVersion = getIPVersion(bArr)
        return if (iPVersion.toInt() == 4) {
            val bArr2 = ByteArray(4)
            System.arraycopy(bArr, 12, bArr2, 0, 4)
            try {
                InetAddress.getByAddress(bArr2)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else if (iPVersion.toInt() == 6) {
            val bArr3 = ByteArray(16)
            System.arraycopy(bArr, 8, bArr3, 0, 16)
            try {
                InetAddress.getByAddress(bArr3)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else {
            Log.e(TAG, "Unknown IP version")
            null
        }
    }

    fun getDestIP(bArr: ByteArray): InetAddress? {
        val iPVersion = getIPVersion(bArr)
        return if (iPVersion.toInt() == 4) {
            val bArr2 = ByteArray(4)
            System.arraycopy(bArr, 16, bArr2, 0, 4)
            try {
                InetAddress.getByAddress(bArr2)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else if (iPVersion.toInt() == 6) {
            val bArr3 = ByteArray(16)
            System.arraycopy(bArr, 24, bArr3, 0, 16)
            try {
                InetAddress.getByAddress(bArr3)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Error creating InetAddress", e)
                null
            }
        } else {
            Log.e(TAG, "Unknown IP version")
            null
        }
    }

    fun getTCPSourcePort(ipv4: ByteArray): Int {
        // 确保输入数据包的长度足够
        if (ipv4.size < 20) {
            throw IllegalArgumentException("Invalid IPv4 packet")
        }

        // IPv4头部长度（第1字节的4个低位）
        val ipv4HeaderLength = (ipv4[0].toInt() and 0x0F) * 4

        // 确保TCP部分开始的位置
        if (ipv4.size < ipv4HeaderLength + 20) {
            throw IllegalArgumentException("Invalid IPv4 packet, TCP segment is too short")
        }

        // TCP源端口位于IPv4头部后的第13、14字节（即第21、22字节）
        val tcpSourcePort = byteArrayToInt(
            listOf<Byte>(
                0x00,
                0x00
            ).toByteArray() +
                    ipv4.slice(ipv4HeaderLength + 0 until ipv4HeaderLength + 2).toByteArray()
        )

        return tcpSourcePort
    }

    fun getTCPDestPort(ipv4: ByteArray): Int {
        // 确保输入数据包的长度足够
        if (ipv4.size < 20) {
            throw IllegalArgumentException("Invalid IPv4 packet")
        }

        // IPv4头部长度（第1字节的4个低位）
        val ipv4HeaderLength = (ipv4[0].toInt() and 0x0F) * 4

        // 确保TCP部分开始的位置
        if (ipv4.size < ipv4HeaderLength + 20) {
            throw IllegalArgumentException("Invalid IPv4 packet, TCP segment is too short")
        }

        // TCP目标端口位于IPv4头部后的第15、16字节（即第23、24字节）
        val tcpDestPort = byteArrayToInt(
            listOf<Byte>(
                0x00,
                0x00
            ).toByteArray() + ipv4.slice(ipv4HeaderLength + 2 until ipv4HeaderLength + 4)
                .toByteArray()
        )

        return tcpDestPort
    }

    fun getUDPData(ipv4: ByteArray): ByteArray {
        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val udpHeaderLength = 8
        return ipv4.slice(ipHeaderLength + udpHeaderLength until ipv4.size).toByteArray()
    }

    fun getTCPData(ipv4: ByteArray): ByteArray {
        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val tcpHeaderLength = (ipv4[ipHeaderLength + 12].toInt() and 0xF0) shr 2
        return ipv4.slice(ipHeaderLength + tcpHeaderLength until ipv4.size).toByteArray()
    }

    fun calculateChecksum(bArr: ByteArray, j: Long, i: Int, i2: Int): Long {
        var j = j
        var i = i
        var i3 = i2 - i
        while (i3 > 1) {
            j += (65280 and (bArr[i].toInt() shl 8) or (bArr[i + 1].toInt() and 255)).toLong()
            if (-65536L and j > 0) {
                j = (j and 65535L) + 1
            }
            i += 2
            i3 -= 2
        }
        if (i3 > 0) {
            j += (bArr[i].toInt() shl 8 and 0xff00)
            if (j and -65536L > 0) {
                j = (j and 65535L) + 1
            }
        }
        return j.inv() and 65535L
    }

    private fun calculateChecksum(bytes: ByteArray): UShort {
        var sum: Long = 0

        // 将每两个字节的数据组合起来，并求和
        var i = 0
        while (i < bytes.size - 1) {
            sum += (bytes[i].toInt() and 0xFF) shl 8 or (bytes[i + 1].toInt() and 0xFF)
            i += 2
        }

        // 如果数据包长度为奇数，则将最后一个字节的值加到和中
        if (bytes.size % 2 != 0) {
            sum += (bytes[bytes.size - 1].toInt() and 0xFF) shl 8
        }

        // 将和的高位字节加到低位字节中
        sum = (sum and 0xFFFF) + (sum shr 16)

        // 取反得到校验和
        return (sum.inv() and 0xFFFF).toUShort()
    }

    private fun setIPV4CheckSum(ipv4: ByteArray) {
        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        println("ipHeaderLength: $ipHeaderLength")
        val ipv4Header = ipv4.slice(0 until ipHeaderLength).toByteArray()
        ipv4Header[10] = 0
        ipv4Header[11] = 0
        val checkSum = calculateChecksum(ipv4Header).toInt()
        ipv4[10] = (checkSum shr 8).toByte()
        ipv4[11] = checkSum.toByte()
        println("ipv4 check sum: ${ipv4[10]},${ipv4[11]}")
        ipv4Header[10] = (checkSum shr 8).toByte()
        ipv4Header[11] = checkSum.toByte()
        println("ipv4 check sum: ${ipv4[10]},${ipv4[11]}")

    }

    private fun setTCPCheckSum(ipv4: ByteArray) {
        val fakeHeader = ByteArray(12)
        val sourceAddress = ipv4.slice(12..15).toByteArray()
        val destinationAddress = ipv4.slice(16..19).toByteArray()

        sourceAddress.copyInto(fakeHeader, 0)
        destinationAddress.copyInto(fakeHeader, 4)
        fakeHeader[8] = 0
        fakeHeader[9] = 6
        fakeHeader[10] = ((ipv4.size - 20) shr 8).toByte()
        fakeHeader[11] = (ipv4.size - 20).toByte()

        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val tcp = ipv4.drop(ipHeaderLength).toByteArray()
        tcp[16] = 0
        tcp[17] = 0
        val checkSum = calculateChecksum(fakeHeader + tcp).toInt()
        ipv4[ipHeaderLength + 16] = (checkSum shr 8).toByte()
        ipv4[ipHeaderLength + 17] = checkSum.toByte()
    }

    private fun setUDPCheckSum(ipv4: ByteArray) {
        val fakeHeader = ByteArray(12)
        val sourceAddress = ipv4.slice(12..15).toByteArray()
        val destinationAddress = ipv4.slice(16..19).toByteArray()

        sourceAddress.copyInto(fakeHeader, 0)
        destinationAddress.copyInto(fakeHeader, 4)
        fakeHeader[8] = 0
        fakeHeader[9] = 17
        fakeHeader[10] = ((ipv4.size - 20) shr 8).toByte()
        fakeHeader[11] = (ipv4.size - 20).toByte()

        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val udp = ipv4.drop(ipHeaderLength).toByteArray()
        udp[6] = 0
        udp[7] = 0
        val checkSum = calculateChecksum(fakeHeader + udp).toInt()
        println("ipv4 check sum: ${ipv4[ipHeaderLength + 6]},${ipv4[ipHeaderLength + 7]}")
        ipv4[ipHeaderLength + 6] = (checkSum shr 8).toByte()
        ipv4[ipHeaderLength + 7] = checkSum.toByte()
        println("ipv4 check sum: ${ipv4[ipHeaderLength + 6]},${ipv4[ipHeaderLength + 7]}")
    }

    fun handleUDPData(ipv4: ByteArray, lambda: (udpData: ByteArray) -> ByteArray): ByteArray {
        val oldUdpData = getUDPData(ipv4)
        val newUdpData = lambda(oldUdpData)
        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val udpHeaderLength = 8
        val header = ipv4.slice(0 until ipHeaderLength + udpHeaderLength).toByteArray()

        val newIpv4 = header + newUdpData
        // 修改ipv4长度字段（字节2和字节3）
        val newTotalLengthBytes = newIpv4.size.toByteArray()
        // 修改Total Length字段（字节 2 和 3）
        newIpv4[2] = newTotalLengthBytes[2]  // 最高字节
        newIpv4[3] = newTotalLengthBytes[3]  // 最低字节
        // 修改UDP长度字段（字节4和字节5）
        val udpLengthBytes = (newUdpData.size + 8).toByteArray()
        newIpv4[24] = udpLengthBytes[2]  // 高字节
        newIpv4[25] = udpLengthBytes[3]  // 低字节
        setUDPCheckSum(newIpv4)
        setIPV4CheckSum(newIpv4)
        Log.i(TAG, "handleUDPData: newIpv4 ${newIpv4.toHexString()}")
        return newIpv4
    }

    fun handleTCPDate(ipv4: ByteArray, lambda: (update: ByteArray) -> ByteArray): ByteArray {
        val oldTcpData = getTCPData(ipv4)
        val newTcpData = lambda(oldTcpData)
        val ipHeaderLength = (ipv4[0].toInt() and 0x0F) * 4
        val tcpHeaderLength = (ipv4[ipHeaderLength + 12].toInt() and 0xF0) shr 2

        val header = ipv4.slice(0 until ipHeaderLength + tcpHeaderLength).toByteArray()

        val newIpv4 = header + newTcpData
        val ipTotalLength = newIpv4.size
        //设置ip total length
        newIpv4[2] = (ipTotalLength shr 8).toByte()
        newIpv4[3] = ipTotalLength.toByte()

        setTCPCheckSum(newIpv4)
        setIPV4CheckSum(newIpv4)
        return newIpv4
    }

    // deprecated 垃圾游戏，RST也处理不了，照样闪退
    fun createRST(ipv4: ByteArray): ByteArray {
        // 确保输入数据包的长度足够
        if (ipv4.size < 20) {
            throw IllegalArgumentException("Invalid IPv4 packet")
        }

        // IPv4头部长度（第1字节的4个低位）
        val ipv4HeaderLength = (ipv4[0].toInt() and 0x0F) * 4

        val rst = listOf<Byte>(
            0x45, 0x00, 0x00, 0x28,
            0x00, 0x00, 0x40, 0x00,
            0x40, 0x06, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,

            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x50, 0x14, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
        ).toByteArray()
        // 设置标识
        val identifier = Random.nextInt(0, 65535).toByteArray()
        rst[4] = identifier[0]
        rst[5] = identifier[1]
        // 设置源IP地址
        repeat(4) { i -> rst[i + 12] = ipv4[i + 16] }
        // 设置目标IP地址
        repeat(4) { i -> rst[i + 16] = ipv4[i + 12] }
        // 设置源端口
        repeat(2) { i -> rst[i + 20] = ipv4[i + ipv4HeaderLength + 2] }
        // 设置目标端口
        repeat(2) { i -> rst[i + 22] = ipv4[i + ipv4HeaderLength] }
        // 设置ACK
        val ackByteArray = ipv4.slice(ipv4HeaderLength + 4 until ipv4HeaderLength + 8).toByteArray()
        val ackInt = byteArrayToInt(ackByteArray)
        val ack = (ackInt + 1).toByteArray()
        5.toByteArray()
        repeat(4) { i -> rst[i + 28] = ack[i] }
        setTCPCheckSum(rst)
        setIPV4CheckSum(rst)
        Log.i(TAG, "createRST: ${rst.toHexString()}")
        return rst
    }

    private fun byteArrayToInt(byteArray: ByteArray): Int {
        return ByteBuffer.wrap(byteArray).int
    }

    fun byteArrayToIntBigEndian(bytes: ByteArray): Int {
        require(bytes.size == 4) { "Byte array must be of length 4" }
        return (bytes[0].toInt() and 0xFF shl 24) or (bytes[1].toInt() and 0xFF shl 16) or (bytes[2].toInt() and 0xFF shl 8) or (bytes[3].toInt() and 0xFF)
    }

    fun byteArrayToIntLittleEndian(bytes: ByteArray): Int {
        require(bytes.size == 4) { "Byte array must be of length 4" }
        return (bytes[3].toInt() and 0xFF shl 24) or (bytes[2].toInt() and 0xFF shl 16) or (bytes[1].toInt() and 0xFF shl 8) or (bytes[0].toInt() and 0xFF)
    }

    fun isTCP(ipv4: ByteArray): Boolean {
        // 确保输入数据包的长度足够
        if (ipv4.size < 20) {
            throw IllegalArgumentException("Invalid IPv4 packet")
        }
        // 获取 IPv4 协议字段
        val protocol = ipv4[9].toInt()
        // 如果协议字段为 6，则表示是 TCP 协议
        return protocol == 6
    }

    fun isUDP(ipv4: ByteArray): Boolean {
        // 确保输入数据包的长度足够
        if (ipv4.size < 20) {
            throw IllegalArgumentException("Invalid IPv4 packet")
        }
        // 获取 IPv4 协议字段（位于第9个字节）
        val protocol = ipv4[9].toInt()
        // 如果协议字段为 17，则表示是 UDP 协议
        return protocol == 17
    }

    fun isSYN(ipv4Packet: ByteArray): Boolean {
        // 检查 IPv4 包的长度，确保它至少包含 IP 头部和 TCP 头部（20 字节 + 至少 20 字节的 TCP 头部）
        if (ipv4Packet.size < 40) {
            return false
        }

        // 提取 IP 头部长度 (IPv4 header 的 4 高位表示头部长度)
        val ipHeaderLength = (ipv4Packet[0].toInt() and 0x0F) * 4 // IP 头部长度（单位：字节）

        // 提取 TCP 头部（从 IP 数据部分开始，IP 头部之后）
        val tcpHeader = ipv4Packet.copyOfRange(ipHeaderLength, ipHeaderLength + 20)

        // 确保至少有 20 字节的 TCP 头部
        if (tcpHeader.size < 20) {
            return false
        }

        // 获取 TCP 标志位（13 字节是标志字段）
        val flags = tcpHeader[13].toInt()

        // 提取 SYN 和 ACK 标志位
        val synFlag = (flags and 0x02) != 0  // SYN 位
        val ackFlag = (flags and 0x10) != 0  // ACK 位

        // 如果 SYN 为 1 且 ACK 为 0，则是客户端发出的连接请求包
        return synFlag && !ackFlag
    }

    fun isRST(ipv4Packet: ByteArray): Boolean {
        // 检查 IPv4 包的长度，确保它至少包含 IP 头部和 TCP 头部（20 字节 + 至少 20 字节的 TCP 头部）
        if (ipv4Packet.size < 40) {
            return false
        }

        // 提取 IP 头部长度 (IPv4 header 的 4 高位表示头部长度)
        val ipHeaderLength = (ipv4Packet[0].toInt() and 0x0F) * 4 // IP 头部长度（单位：字节）

        // 提取 TCP 头部（从 IP 数据部分开始，IP 头部之后）
        val tcpHeader = ipv4Packet.copyOfRange(ipHeaderLength, ipHeaderLength + 20)

        // 确保至少有 20 字节的 TCP 头部
        if (tcpHeader.size < 20) {
            return false
        }

        // 获取 TCP 标志位（13 字节是标志字段）
        val flags = tcpHeader[13].toInt()

        // 提取 RST 和 ACK 标志位
        val rstFlag = (flags and 0x04) != 0  // RST 位
        val ackFlag = (flags and 0x10) != 0  // ACK 位

        // 如果 RST 为 1 且 ACK 为 1，则是RST
        return rstFlag && ackFlag
    }

    fun isInNetwork(sourceAddress: InetAddress?, networkPrefix: InetAddress?): Boolean {
        val sourceAddressBytes = sourceAddress?.address
        val networkPrefixBytes = networkPrefix?.address
        if (sourceAddressBytes != null && networkPrefixBytes != null) {
            for (i in networkPrefixBytes.indices) {
                if (sourceAddressBytes[i] != networkPrefixBytes[i] && networkPrefixBytes[i].toInt() != 0) {
                    return false
                }
            }
            return true
        }
        return false

    }

}
