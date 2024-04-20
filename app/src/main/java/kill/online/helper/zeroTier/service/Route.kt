package kill.online.helper.zeroTier.service

import kill.online.helper.zeroTier.util.InetAddressUtils
import java.net.InetAddress

/**
 * 路由记录数据类
 */
class Route(
    val address: InetAddress? = null,
    private val prefix: Int = 0,
    var gateway: InetAddress? = null
) {
    fun belongsToRoute(inetAddress: InetAddress): Boolean {
        return address == InetAddressUtils.addressToRoute(inetAddress, prefix)
    }
}
