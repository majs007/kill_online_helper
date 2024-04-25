package kill.online.helper.data


data class ModifyMember(
    val hidden: Boolean? = null,
    val name: String,
    val description: String,
    val config: Config? = null
) {
    data class Config(
        val activeBridge: Boolean? = null,
        val authorized: Boolean? = null,
        val capabilities: List<Int>? = null,
        val ipAssignments: List<String>? = null,
        val noAutoAssignIps: Boolean? = null,
        val tags: List<List<Int>>? = null
    )
}

data class Member(
    val id: String,
    val clock: Long,
    val networkId: String,
    val nodeId: String,
    val controllerId: String,
    val hidden: Boolean,
    var name: String,
    var description: String,
    val config: Config,
    val lastOnline: Long,
    val lastSeen: Long,
    val physicalAddress: String,
    val clientVersion: String,
    val protocolVersion: Int,
    val supportsRulesEngine: Boolean
) {
    data class Config(
        val activeBridge: Boolean,
        val authorized: Boolean,
        val capabilities: List<Int>,
        val creationTime: Long,
        val id: String,
        val identity: String,
        val ipAssignments: List<String>,
        val lastAuthorizedTime: Long,
        val lastDeauthorizedTime: Long,
        val noAutoAssignIps: Boolean,
        val revision: Int,
        val tags: List<List<Int>>,
        val vMajor: Int,
        val vMinor: Int,
        val vRev: Int,
        val vProto: Int
    )
}







