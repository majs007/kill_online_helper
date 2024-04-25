package kill.online.helper.zeroTier.model

import kill.online.helper.zeroTier.model.type.DNSMode

data class UserNetworkConfig(
    var networkId: String,
    var routeViaZeroTier: Boolean = true,
    var dnsMode: DNSMode = DNSMode.NO_DNS,
    var dnsServers: List<String> = listOf(),
)
