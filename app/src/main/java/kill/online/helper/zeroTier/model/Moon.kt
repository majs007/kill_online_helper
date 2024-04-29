package kill.online.helper.zeroTier.model

data class Moon(
    //Moon 地址
    var moonWorldId: String,
    // Moon 种子
    var moonSeed: String,
    var state: MoonState = MoonState.DERAILMENT,
) {
    enum class MoonState {
        ORBIT, DERAILMENT
    }
}