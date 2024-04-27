package kill.online.helper.zeroTier.model


data class UserNetwork(
    var networkId: String,
    var networkName: String = "",
    //最近被起用
    var lastActivated: Boolean = false,
)
