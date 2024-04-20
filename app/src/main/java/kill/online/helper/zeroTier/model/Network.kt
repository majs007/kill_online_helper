package kill.online.helper.zeroTier.model


data class UserNetwork(
    var networkId: String,
    var networkName: String,
    //网络是否被启用
    var enabled: Boolean = false,
    //最近被起用
    var lastActivated: Boolean = false,
)
