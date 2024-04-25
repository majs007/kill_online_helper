package kill.online.helper.ui.page


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.king.ultraswiperefresh.NestedScrollMode
import com.king.ultraswiperefresh.UltraSwipeRefresh
import com.king.ultraswiperefresh.rememberUltraSwipeRefreshState
import kill.online.helper.ui.components.BasicItemContainer
import kill.online.helper.viewModel.ZeroTierViewModel
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayerContent(
    navController: NavHostController,
    playerListState: LazyListState = rememberLazyListState()
) {
    val ztViewModel: ZeroTierViewModel = viewModel()
    val state = rememberUltraSwipeRefreshState()
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }


    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            ztViewModel.getMembers(ztViewModel.getLastActivatedNetworkId())
            state.isRefreshing = false
        }
    }
    LaunchedEffect(null) {
        ztViewModel.getMembers(ztViewModel.getLastActivatedNetworkId())
    }
    UltraSwipeRefresh(
        state = state,
        onRefresh = { state.isRefreshing = true },
        onLoadMore = { state.isLoading = true },
        modifier = Modifier,
        headerScrollMode = NestedScrollMode.Translate,
        footerScrollMode = NestedScrollMode.Translate,
    ) {
        LazyColumn(state = playerListState) {
            itemsIndexed(ztViewModel.members.value) { index, item ->
                val milliseconds = currentTime.longValue - item.lastSeen
                val dayNumber =
                    TimeUnit.MILLISECONDS.toDays(currentTime.longValue - item.lastSeen).toInt()

                when {
                    dayNumber == 0 && milliseconds < 60 * 1000 -> {
                        BasicItemContainer(
                            icon = "🥳",
                            text = { item.name },
                            subText = { "状态：在线" })
                    }

                    dayNumber == 0 -> {
                        BasicItemContainer(
                            icon = "🥰",
                            text = { item.name },
                            subText = { "状态：${dayNumber + 1}天内" })
                    }

                    dayNumber == 1 -> {
                        BasicItemContainer(
                            icon = "😎",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 2 -> {
                        BasicItemContainer(
                            icon = "😶",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 3 -> {
                        BasicItemContainer(
                            icon = "😐",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 4 -> {
                        BasicItemContainer(
                            icon = "🤔",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 5 -> {
                        BasicItemContainer(
                            icon = "😕",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 6 -> {
                        BasicItemContainer(
                            icon = "😥",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber == 7 -> {
                        BasicItemContainer(
                            icon = "😖",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }

                    dayNumber > 30 -> {

                    }

                    else -> {
                        BasicItemContainer(
                            icon = "😭",
                            text = { item.name },
                            subText = { "状态：${dayNumber}天前" })
                    }
                }

            }

        }
    }

}
