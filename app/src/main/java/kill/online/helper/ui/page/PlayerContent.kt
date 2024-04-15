package kill.online.helper.ui.page


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import kill.online.helper.ui.components.BasicItemContainer

@Composable
fun PlayerContent(
    navController: NavHostController,
    playerListState: LazyListState = rememberLazyListState()
) {
    data class PlayerInfo(val name: String, val lastSeen: Int)

    val players = remember {
        listOf(
            PlayerInfo("章鱼哥", 0), PlayerInfo("派大星", 1),
            PlayerInfo("海绵宝宝", 2), PlayerInfo("小蜗", 3),
            PlayerInfo("蟹老板", 4), PlayerInfo("神秘奇男子AAA", 5),
            PlayerInfo("章鱼大哥", 6), PlayerInfo("大蜗", 7),
            PlayerInfo("海绵宝宝", 8), PlayerInfo("小蜗", 9),
            PlayerInfo("蟹老板", 10), PlayerInfo("神秘奇男子AAA", 11),
        )
    }
    LazyColumn(state = playerListState) {
        itemsIndexed(players) { index, item ->
            when (item.lastSeen) {
                0 -> {
                    BasicItemContainer(icon = "🥳", text = { item.name }, subText = { "状态：在线" })
                }

                1 -> {
                    BasicItemContainer(
                        icon = "🥰",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen}天内" })
                }

                2 -> {
                    BasicItemContainer(
                        icon = "😎",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                3 -> {
                    BasicItemContainer(
                        icon = "😶",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                4 -> {
                    BasicItemContainer(
                        icon = "😐",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                5 -> {
                    BasicItemContainer(
                        icon = "🤔",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                6 -> {
                    BasicItemContainer(
                        icon = "😕",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                7 -> {
                    BasicItemContainer(
                        icon = "😥",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                8 -> {
                    BasicItemContainer(
                        icon = "😖",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                else -> {
                    BasicItemContainer(
                        icon = "😭",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}多天前" })
                }
            }

        }

    }
}
