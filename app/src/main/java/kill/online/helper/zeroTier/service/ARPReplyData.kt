package kill.online.helper.zeroTier.service

import java.net.InetAddress

/**
 * ARP 应答报文的所需数据。由于报文内容总是当前节点的 IP 与 MAC，因此仅记录应答报文目标的信息。
 */
data class ARPReplyData(
    val destMac: Long = 0,
    val destAddress: InetAddress? = null
)
