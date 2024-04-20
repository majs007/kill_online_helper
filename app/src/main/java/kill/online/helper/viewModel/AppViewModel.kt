package kill.online.helper.viewModel

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

/**
 * 全局 viewModel
 */
class AppViewModel : ViewModel() {
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")
    var testState = mutableIntStateOf(0)
}