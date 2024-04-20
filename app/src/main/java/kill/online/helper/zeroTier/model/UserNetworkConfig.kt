package kill.online.helper.zeroTier.model

import kill.online.helper.zeroTier.model.type.DNSMode

data class UserNetworkConfig(
    var networkId: String,
    var routeViaZeroTier: Boolean,
    var dnsMode: DNSMode,
    var dnsServers: List<String>,
)
