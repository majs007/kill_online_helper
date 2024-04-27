package kill.online.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kill.online.helper.data.Room
import kill.online.helper.ui.theme.appPadding
import kill.online.helper.ui.theme.cardRoundedCorner
import kill.online.helper.ui.theme.floatingButtonPadding
import kill.online.helper.ui.theme.textPadding
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,
    roomListState: LazyListState = rememberLazyListState(),
    ztViewModel: ZeroTierViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    var isRoomInfoSheetShow by remember { mutableStateOf(false) }
    var room by remember { mutableStateOf(Room()) }
    // 根据按钮状态获取对应的按钮颜色
    val runningColor = ButtonDefaults.elevatedButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    )
    val stoppedColor = ButtonDefaults.elevatedButtonColors()
    val btnColors = remember(ztViewModel.isZTRunning.value) {
        mutableStateOf(if (ztViewModel.isZTRunning.value) runningColor else stoppedColor)
    }
    val btnText = remember(ztViewModel.isZTRunning.value) {
        mutableStateOf(if (ztViewModel.isZTRunning.value) "已启用" else "未启用")
    }
    LaunchedEffect(null) {
        ztViewModel.initZTConfig(context)
        ztViewModel.loadZTConfig(context)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = appPadding, end = appPadding)
//            .background(Color(23,32,43))
    ) {
        ElevatedButton(
            colors = btnColors.value,
            onClick = {
                try {
                    if (!ztViewModel.isZTRunning.value) {
                        ztViewModel.startZeroTier(context)
                        appViewModel.startMsgServer()

                    } else {
                        ztViewModel.stopZeroTier(context)
                        appViewModel.stopMsgServer()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            shape = RoundedCornerShape(cardRoundedCorner),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text(
                text = btnText.value,
                fontSize = 30.sp,
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        OutlinedCard(
            modifier = Modifier
                .padding(bottom = floatingButtonPadding)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color.Cyan)

            ) {
                Text(
                    text = "房间列表",
                    modifier = Modifier
                        .padding(
                            top = textPadding,
                            bottom = textPadding
                        )
                        .align(Alignment.Center)
                )

            }

            LazyColumn(
                state = roomListState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                items(ztViewModel.rooms.value) {
                    ElevatedButton(
                        onClick = {
                            room = it
                            isRoomInfoSheetShow = true
                        },
                        shape = RoundedCornerShape(cardRoundedCorner),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
//                                .background(Color.Cyan)
                                .fillMaxWidth()
                        ) {
                            val roomState = if (it.state == Room.RoomState.WAITING) "🔥" else "😴"
                            val isPrivateRoom = if (it.isPrivateRoom) "🔒" else "🔓"
                            Text(
                                text = "$isPrivateRoom  $roomState",
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(text = it.roomName, modifier = Modifier.align(Alignment.Center))
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    RoomInfoSheet(
        isShow = isRoomInfoSheetShow,
        room = room,
        onDismissRequest = { isRoomInfoSheetShow = false })

}