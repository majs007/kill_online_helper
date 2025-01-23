package kill.online.helper.ui.page

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.data.Room
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.theme.appPadding
import kill.online.helper.ui.theme.cardRoundedCorner
import kill.online.helper.ui.theme.floatingButtonPadding
import kill.online.helper.ui.theme.textPadding
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,
    roomListState: LazyListState = rememberLazyListState(),
    ztViewModel: ZeroTierViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val TAG = "HomeContent"
    val context = LocalContext.current
    var ztOldState by remember { mutableStateOf(!ztViewModel.isZTRunning) }
    var isRoomInfoSheetShow by remember { mutableStateOf(false) }
    var room by remember { mutableStateOf(Room()) }
    var isShowAlertDialog by remember { mutableStateOf(false) }
    var isShowCelebration by remember { mutableStateOf(false) }


    val md by remember(0) {
        mutableStateOf(context.assets.open("md/homeContentTips.md").use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        })
    }


    // 根据按钮状态获取对应的按钮颜色
    val runningColor = ButtonDefaults.elevatedButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    )
    val stoppedColor = ButtonDefaults.elevatedButtonColors()
    val btnColors = remember(ztViewModel.isZTRunning) {
        mutableStateOf(if (ztViewModel.isZTRunning) runningColor else stoppedColor)
    }
    val btnText = remember(ztViewModel.isZTRunning) {
        mutableStateOf(if (ztViewModel.isZTRunning) "已启用" else "未启用")
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            ztViewModel.rooms.removeAll { System.currentTimeMillis() - it.timeStamp > 6000 }
            if (System.currentTimeMillis() - ztViewModel.enteredRoom.value.timeStamp > 10000) {
                ztViewModel.enteredRoom.value = Room()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        // 防止快速重复点击，导致闪退
                        if (ztViewModel.isZTRunning == ztOldState) return@ElevatedButton
                        if (!ztViewModel.isZTRunning) {
                            ztViewModel.startZeroTier(context)
                            appViewModel.startMsgServer(context)
                            isShowCelebration = true
                        } else {
                            ztViewModel.stopZeroTier(context)
                            appViewModel.stopMsgServer(context)
                        }
                        ztOldState = !ztOldState
                    } catch (e: Exception) {
                        Log.e(TAG, "ElevatedButton: ${e.message}")
                    }
                },
                shape = RoundedCornerShape(cardRoundedCorner),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Text(
                    text = btnText.value, fontSize = 30.sp, modifier = Modifier
                )
            }
            Spacer(modifier = Modifier.height(30.dp))

            OutlinedCard(
                modifier = Modifier
                    .padding(bottom = floatingButtonPadding)
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
//                    .background(Color.Cyan)

                ) {
                    Text(
                        text = "房间列表", modifier = Modifier
                            .padding(
                                top = textPadding, bottom = textPadding
                            )
                            .align(Alignment.Center)
                    )
                }
                // empty room
                if (ztViewModel.rooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        AssetLottie(
                            name = "emptyGhost.lottie",
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .align(Alignment.Center)
                        )
                    }
                }
                LazyColumn(
                    state = roomListState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    itemsIndexed(ztViewModel.rooms, { index, _ -> index }) { _, it ->
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
                                val roomState = if (it.state == Room.RoomState.WAITING) "👻" else "🔥"
                                val isPrivateRoom = if (it.isPrivateRoom) "🔒" else "🔑"

                                IconButton(
                                    onClick = {
                                        if (it.isPrivateRoom) {
                                            room = it
                                            isShowAlertDialog = true
                                        }
                                    }, modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Text(text = isPrivateRoom)
                                }

                                Text(
                                    text = "$roomState|${it.roomName}",
                                    modifier = Modifier.align(Alignment.Center)
                                )
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

        if (isShowCelebration)
            Box(
                modifier = Modifier
                    .padding(appPadding)
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            ) {
                AssetLottie(
                    name = "celebration.lottie",
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .align(Alignment.Center),
                    iterations = 1,
                    onEnd = {
                        isShowCelebration = false
//                        showToast(context, "播放结束")
                    }
                )
            }
    }


    RoomInfoSheet(isShow = isRoomInfoSheetShow,
        room = room,
        onDismissRequest = { isRoomInfoSheetShow = false })
    if (isShowAlertDialog)
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Key, contentDescription = null
                )
            },
            title = { Text(text = "房间密码") },
            onDismissRequest = { isShowAlertDialog = false },
            confirmButton = {
                ElevatedButton(onClick = {
                    isShowAlertDialog = false
                }) {
                    Text(text = "确定")
                }
            },
            text = {
                OutlinedTextField(value = ztViewModel.roomPassword[room.roomOwnerIp] ?: "",
                    label = { Text(text = "") },
                    maxLines = 1,
                    onValueChange = { newValue ->
                        ztViewModel.roomPassword[room.roomOwnerIp] = newValue
                    })
            }
        )
    if (appViewModel.isShowTips)
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Filled.TipsAndUpdates,
                    contentDescription = null
                )
            },
            title = { Text(text = "宝宝巴适") },
            onDismissRequest = {
                appViewModel.isShowTips = false
            },
            confirmButton = {},
            text = {
                RichText {
                    Markdown(md)
                }
            }
        )
}