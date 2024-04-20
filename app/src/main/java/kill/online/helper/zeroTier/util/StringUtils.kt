package kill.online.helper.zeroTier.util

import com.zerotier.sdk.Peer
import com.zerotier.sdk.Version
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.util.Locale

/**
 * 字符串处理工具类
 */
object StringUtils {
    private const val VERSION_FORMAT = "%d.%d.%d"

    /**
     * 将版本号转为可读字符串
     *
     * @param version 版本号
     * @return 可读字符串
     */
    fun toString(version: Version): String {
        return String.format(
            Locale.ROOT, VERSION_FORMAT,
            version.major, version.minor, version.revision
        )
    }

    /**
     * 获得结点版本的可读字符串
     *
     * @param peer 结点
     * @return 可读字符串
     */
    fun peerVersionString(peer: Peer): String {
        return String.format(
            Locale.ROOT, VERSION_FORMAT,
            peer.versionMajor, peer.versionMinor, peer.versionRev
        )
    }

    /**
     * 将 16 进制字符串转换为字符数组
     *
     * @param hex 16 进制字符串
     * @return 字符数组
     */
    fun hexStringToBytes(hex: String): ByteArray {
        val length = hex.length
        if (length % 2 != 0) {
            throw RuntimeException("String length must be even")
        }
        val result = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            val highDigit = hex[i].digitToIntOrNull(16) ?: -1
            val lowDigit = hex[i + 1].digitToIntOrNull(16) ?: -1
            result[i / 2] = ((highDigit shl 4) + lowDigit).toByte()
            i += 2
        }
        return result
    }

    /**
     * 将 InetSocketAddress 转为 IP:Port (IPv6 则是 [IP]:Port) 格式的字符串
     */
    fun toString(address: InetSocketAddress): String {
        val inetAddress = address.address
        val port = address.port

        // 将 IP 地址转为字符串
        var ipString = inetAddress.hostAddress
        if (inetAddress is Inet6Address) {
            ipString = "[$ipString]"
        }
        return "$ipString:$port"
    }
}
