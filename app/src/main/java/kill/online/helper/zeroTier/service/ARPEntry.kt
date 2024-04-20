package kill.online.helper.zeroTier.service

import java.net.InetAddress

/**
 * ARP 表项。记录 MAC 与 IPv4 地址的对应关系及记录时间
 */
data class ARPEntry(val mac: Long, val address: InetAddress) {
    var time: Long = 0

    init {
        updateTime()
    }

    fun updateTime() {
        time = System.currentTimeMillis()
    }
}
