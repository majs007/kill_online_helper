package kill.online.helper.zeroTier.model

data class Moon(
    //Moon 地址
    var moonWorldId: String,
    // Moon 种子
    var moonSeed: String = "",
    // Moon 状态
    var state: MoonState = MoonState.DERAILMENT,
    // Moon 是否选中
    var checked: Boolean = false,
) {
    enum class MoonState {
        ORBIT, DERAILMENT
    }
}