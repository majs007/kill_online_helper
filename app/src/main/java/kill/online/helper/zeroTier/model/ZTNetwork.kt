package kill.online.helper.zeroTier.model

import kill.online.helper.zeroTier.model.type.DNSMode


data class ZTNetwork(
    var networkId: String = "",
    var networkName: String = "",
    //被选用
    var checked: Boolean = false,
    var config: ZTNetworkConfig = ZTNetworkConfig()
) {
    data class ZTNetworkConfig(
        var routeViaZeroTier: Boolean = false,
        var dnsMode: DNSMode = DNSMode.NO_DNS,
        var dnsServers: List<String> = listOf(),
    )
}
