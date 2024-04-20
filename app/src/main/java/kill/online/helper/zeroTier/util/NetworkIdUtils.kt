package kill.online.helper.zeroTier.util

import java.math.BigInteger

/**
 * 网络号处理工具类
 */
object NetworkIdUtils {
    fun hexStringToLong(str: String): Long {
        return BigInteger(str, 16).toLong()
    }
}
