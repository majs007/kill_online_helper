package kill.online.helper.zeroTier

data class ZeroTierConfig(
    var networkId: String,
    var routeAll: Boolean,
    var dnsConfig: DnsConfig,
    var ipv4Address_1: String?,
    var ipv4Address_2: String?,
    var ipv6Address_1: String?,
    var ipv6Address_2: String?,
)

enum class DnsConfig { NoDNS, NetworkDNS, CustomDNS }