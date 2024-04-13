package com.online.helper.viewModel

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class GlobalVM : ViewModel() {
    val count = mutableIntStateOf(1)
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")
}