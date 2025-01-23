package kill.online.helper.ui.page

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.data.StickerState
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.components.ExternalImage
import kill.online.helper.ui.theme.cardRoundedCorner
import kill.online.helper.ui.theme.floatingWindowBackgroundAlpha
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.FileUtils.writeBytesToFile
import kill.online.helper.utils.dragFloatingWindow
import kill.online.helper.utils.showToast
import kill.online.helper.utils.toMD5
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.delay
import java.io.File

@SuppressLint("NewApi")
@Composable
fun FloatingWindowSticker(
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel(),
    stickerState: StickerState,
    stickerName: String,
    stickerSize: Dp = 100.dp,
) {
    val TAG = "FloatingWindowSticker"
    val context = LocalContext.current
    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (stickerState == StickerState.RECEIVE) {
            delay(5000)
            val receiveFW = FloatingWindowFactory.getFloatingWindow("receivedSticker")
            receiveFW.hide()
        }
    }
    Box(modifier = Modifier
        .size(stickerSize)
        .background(
            color = if (stickerState == StickerState.SEND) Color.White.copy(
                floatingWindowBackgroundAlpha
            ) else Color.Transparent,
            shape = RoundedCornerShape(cardRoundedCorner),
        )
        .dragFloatingWindow(stickerState == StickerState.SEND) { windowOffset, dragArea ->
            appViewModel.toScreenOffset = windowOffset

        }) {
        // 第一个元素：表情图片，位于中央
        Box(
            modifier = Modifier
                .size(stickerSize - 10.dp)
//                        .background(color = Color.DarkGray)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            val receiveFW =
                                FloatingWindowFactory.getFloatingWindow("receivedSticker")
                            receiveFW.hide()
                        }
                    )
                }
        ) {
//            DrawImage(stickerName, "sticker", Modifier.fillMaxSize())
            if (stickerName.endsWith(".lottie")) {
                AssetLottie(
                    name = stickerName,
                    directory = "sticker",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val dir = context.getExternalFilesDir("online_sticker")
                val stickerFile = File(dir, stickerName.toMD5())
                if (!stickerFile.exists()) {
                    LaunchedEffect(Unit) {
                        appViewModel.download(stickerName, onSuccess = { body ->
                            writeBytesToFile(stickerFile, body.bytes())
                            isDownloaded = true
                        }, onFailure = {
                            showToast(context, "表情下载失败，${it.message}")
                        })
                    }
                } else {
                    isDownloaded = true
                }

                if (isDownloaded)
                    ExternalImage(
                        name = stickerName.toMD5(),
                        directory = "online_sticker",
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }
        // 第二个元素：玩家ID，位于左上角
        Text(
            ztViewModel.appSetting.value.playerName,
            color = Color.Blue,
            fontSize = 15.sp,
            modifier = Modifier
                .offset(y = (-5).dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )
        if (stickerState == StickerState.SEND) {
            // 第三个元素：一个取消按钮，位于右上角
            IconButton(
                onClick = {
                    val fw = FloatingWindowFactory.getFloatingWindow("editSticker")
                    fw.hide()
                }, modifier = Modifier
                    .offset(10.dp, (-10).dp)
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Localized description",
                    tint = Color.Blue
                )
            }
            // 第四个元素：一个发送按钮，位于右下角
            IconButton(
                onClick = {
                    try {
                        if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                            appViewModel.sysToastText =
                                "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                            sysToast.show()
                            return@IconButton
                        } else {
                            appViewModel.lastSendTime =
                                System.currentTimeMillis()
                        }

                        val offsetX = appViewModel.toScreenOffset.x / appViewModel.screenWidth
                        val offsetY = appViewModel.toScreenOffset.y / appViewModel.screenHeight
                        appViewModel.receivedSticker = appViewModel.editingSticker.copy(
                            stickerState = StickerState.RECEIVE,
                            imagePositionRadio = Offset(offsetX, offsetY),
                            timeStamp = System.currentTimeMillis()
                        )
                        Log.i(
                            TAG,
                            "imagePositionRadioX: ${appViewModel.receivedSticker.imagePositionRadio.x} imagePositionRadioY: ${appViewModel.receivedSticker.imagePositionRadio.y}"
                        )
                        Log.i(
                            TAG,
                            "screenWidth: ${appViewModel.screenWidth} screenHeight: ${appViewModel.screenHeight}"
                        )
                        val receiveFW = FloatingWindowFactory.getFloatingWindow("receivedSticker")
                        receiveFW.windowParams.x =
                            (appViewModel.screenWidth * appViewModel.receivedSticker.imagePositionRadio.x).toInt()
                        receiveFW.windowParams.y =
                            (appViewModel.screenHeight * appViewModel.receivedSticker.imagePositionRadio.y).toInt()
                        receiveFW.show()
                        val myIp = ztViewModel.getAssignedIP()
                        ztViewModel.enteredRoom.value.players.forEach {
                            if (it.ip != myIp)
                                appViewModel.sendMessage(it.ip, appViewModel.receivedSticker)
                        }
                        // 更新贴纸使用次数
                        ztViewModel.appSetting.value =
                            ztViewModel.appSetting.value.copy(
                                stickerManage = ztViewModel.appSetting.value.stickerManage.toMutableList()
                                    .map {
                                        if (it.name == stickerName) {
                                            it.copy(usageCounter = it.usageCounter + 1)
                                        } else {
                                            it
                                        }
                                    }
                            )
                        ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        //关闭editSticker窗口
                        val editFW = FloatingWindowFactory.getFloatingWindow("editSticker")
                        editFW.hide()
                    } catch (e: Exception) {
                        Log.e(TAG, "FloatingWindowSticker: ${e.message}")
                    }


                }, modifier = Modifier
                    .offset(10.dp, 10.dp)
                    .align(Alignment.BottomEnd)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Localized description",
                    tint = Color.Blue
                )
            }
        }
    }
}