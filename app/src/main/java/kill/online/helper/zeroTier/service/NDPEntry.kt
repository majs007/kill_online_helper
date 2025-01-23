package kill.online.helper.zeroTier.service

import java.net.InetAddress

/**
 * NDP 表项。记录 MAC 与 IPv6 地址的对应关系及记录时间
 */
data class NDPEntry(
    val mac: Long,
    val address: InetAddress
) {
    var time: Long = 0

    init {
        updateTime()
    }

    fun updateTime() {
        time = System.currentTimeMillis()
    }
}
