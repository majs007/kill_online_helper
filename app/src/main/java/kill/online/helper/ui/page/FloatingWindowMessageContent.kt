package kill.online.helper.ui.page


import android.annotation.SuppressLint
import android.graphics.Rect
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.data.Message
import kill.online.helper.data.MsgType
import kill.online.helper.data.Sticker
import kill.online.helper.data.StickerState
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.components.ExternalImage
import kill.online.helper.ui.components.MessageCard
import kill.online.helper.ui.components.OutlinedTextField
import kill.online.helper.ui.theme.FloatingWindowPadding
import kill.online.helper.ui.theme.floatingWindowWidth
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.showToast
import kill.online.helper.utils.toMD5
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("NewApi")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingWindowMessageContent(
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {
    val TAG = "FWMessageContent"
    val context = LocalContext.current
    val myIp by remember { mutableStateOf(ztViewModel.getAssignedIP() ?: "") }
    var input by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordTime by remember { mutableIntStateOf(0) }
    var isSelectingEmoji by remember { mutableStateOf(false) }

    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
    val editSticker = FloatingWindowFactory.getFloatingWindow("editSticker",
        content = { fw ->
            FloatingWindowSticker(
                stickerState = appViewModel.editingSticker.stickerState,
                stickerName = appViewModel.editingSticker.msg,
                appViewModel = appViewModel,
                ztViewModel = ztViewModel
            )
        },
        config = { fw ->
            fw.setCallback(
                onShow = {
//                                showToast(context, "显示悬浮窗")
                    Log.i("onShow", "floatingEditSticker")
                },
                onHide = {
//                                showToast(context, "隐藏悬浮窗")
                    Log.i("onHide", "floatingEditSticker")

                },
                onLayout = { params ->
                    val view = fw.view
                    val f = Rect().also { view.getWindowVisibleDisplayFrame(it) }
                    params.x = (f.width() - view.width) / 2
                    params.y = (f.height() - view.height) / 2
                }
            )
        }
    )
    // onReceiveMessage 处调用
    val receivedSticker = FloatingWindowFactory.getFloatingWindow("receivedSticker",
        content = { _ ->
            FloatingWindowSticker(
                stickerState = appViewModel.receivedSticker.stickerState,
                stickerName = appViewModel.receivedSticker.msg,
                appViewModel = appViewModel,
                ztViewModel = ztViewModel
            )
        },
        config = { fw ->
            fw.setCallback(
                onShow = {
//                                showToast(context, "显示悬浮窗")
                    Log.i("onShow", "floatingReceivedSticker")
                },
                onHide = {
//                                showToast(context, "隐藏悬浮窗")
                    Log.i("onHide", "floatingReceivedSticker")

                },
            )
        }
    )

    appViewModel.initRecorder(MediaRecorder(context))


    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000L)
            if (recordTime == 9) {
                isRecording = false
                appViewModel.stopRecording()
                showToast(context, "录音时间已达10s上限")
                break
            }
            recordTime++
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()
//            .background(Color.Red)
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // 自动滚动到底部
        LaunchedEffect(appViewModel.messages.size) {
            coroutineScope.launch {
                // 自动滚动到最后一项
                if (appViewModel.messages.isNotEmpty()) {
                    listState.animateScrollToItem(appViewModel.messages.size - 1)
                }
            }
        }
        //选择emoji
        if (isSelectingEmoji) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 常用表情
                item(0) {
                    LazyRow {
                        val popularStickers =
                            ztViewModel.appSetting.value.stickerManage.sortedByDescending {
                                it.usageCounter
                            }.take(10)
                        itemsIndexed(popularStickers, { index, _ -> index }) { _, item ->
                            if (item.type == Sticker.StickerType.LOCAL) {
                                AssetLottie(
                                    name = item.name,
                                    directory = "sticker",
                                    modifier = Modifier
                                        .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                                                    appViewModel.sysToastText = "全员禁言中"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                                                    appViewModel.sysToastText =
                                                        "您已被禁言"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                                                    appViewModel.sysToastText =
                                                        "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                } else {
                                                    appViewModel.lastSendTime =
                                                        System.currentTimeMillis()
                                                }
                                                val sendingMsg = Message(
                                                    ztViewModel.appSetting.value.playerName,
                                                    item.name,
                                                    msgType = MsgType.EMOJI
                                                )
                                                appViewModel.messages.add(sendingMsg)
                                                ztViewModel.enteredRoom.value.players.forEach {
                                                    if (it.ip != myIp)
                                                        appViewModel.sendMessage(
                                                            it.ip,
                                                            sendingMsg
                                                        )
                                                }
                                                if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                                                    appViewModel.isInitialized("bulletWindowManager")
                                                )
                                                    appViewModel.bulletWindowManager.playBulletMessage(
                                                        sendingMsg
                                                    )
                                                isSelectingEmoji = false
                                            },
                                                onLongPress = {
                                                    if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllSticker) {
                                                        appViewModel.sysToastText =
                                                            "全员禁贴纸中"
                                                        sysToast.show()
                                                        return@detectTapGestures
                                                    }
                                                    if (myIp in ztViewModel.enteredRoom.value.banSendStickerList) {
                                                        appViewModel.sysToastText =
                                                            "您已被禁止发送贴纸"
                                                        sysToast.show()
                                                        return@detectTapGestures
                                                    }
                                                    val sendingMsg = Message(
                                                        ztViewModel.appSetting.value.playerName,
                                                        item.name,
                                                        msgType = MsgType.STICKER,
                                                        stickerState = StickerState.SEND,
                                                    )
                                                    appViewModel.editingSticker = sendingMsg
                                                    editSticker.show()
                                                    isSelectingEmoji = false
                                                })
                                        }
                                )
                            } else {
                                ExternalImage(name = item.name.toMD5(),
                                    contentDescription = "sticker",
                                    modifier = Modifier
                                        .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                                                    appViewModel.sysToastText = "全员禁言中"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                                                    appViewModel.sysToastText =
                                                        "您已被禁言"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                                                    appViewModel.sysToastText =
                                                        "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                } else {
                                                    appViewModel.lastSendTime =
                                                        System.currentTimeMillis()
                                                }
                                                val sendingMsg = Message(
                                                    ztViewModel.appSetting.value.playerName,
                                                    item.name,
                                                    msgType = MsgType.EMOJI
                                                )
                                                appViewModel.messages.add(sendingMsg)
                                                ztViewModel.enteredRoom.value.players.forEach {
                                                    if (it.ip != myIp)
                                                        appViewModel.sendMessage(
                                                            it.ip,
                                                            sendingMsg
                                                        )
                                                }
                                                if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                                                    appViewModel.isInitialized("bulletWindowManager")
                                                )
                                                    appViewModel.bulletWindowManager.playBulletMessage(
                                                        sendingMsg
                                                    )
                                                isSelectingEmoji = false
                                            }, onLongPress = {
                                                if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllSticker) {
                                                    appViewModel.sysToastText =
                                                        "全员禁贴纸中"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                if (myIp in ztViewModel.enteredRoom.value.banSendStickerList) {
                                                    appViewModel.sysToastText =
                                                        "您已被禁止发送贴纸"
                                                    sysToast.show()
                                                    return@detectTapGestures
                                                }
                                                val sendingMsg = Message(
                                                    ztViewModel.appSetting.value.playerName,
                                                    item.name,
                                                    msgType = MsgType.STICKER,
                                                    stickerState = StickerState.SEND,
                                                )
                                                appViewModel.editingSticker = sendingMsg
                                                editSticker.show()
                                                isSelectingEmoji = false
                                            })
                                        }
                                )
                            }
                        }
                    }
                }
                // Tab与表情包
                item(1) {
                    var selectedTabIndex by remember { mutableIntStateOf(0) }
                    val tabTitles = listOf("QQ", "咖波", "自定义")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            edgePadding = 5.dp,  // 左右边距
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 动态生成多个 Tab
                            for (i in tabTitles.indices) {
                                Tab(selected = selectedTabIndex == i,
                                    onClick = { selectedTabIndex = i },
                                    text = { Text(tabTitles[i]) })
                            }
                        }
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        // 根据选中的 Tab 显示不同内容
                        when (selectedTabIndex) {
                            0 -> {
                                //QQ sticker
                                val qqStickers = ztViewModel.appSetting.value.stickerManage.filter {
                                    it.name.startsWith("qq_")
                                }
                                val qqStickerChunk = qqStickers.chunked(10)
                                val lazyQQStickers = remember { mutableStateListOf<Sticker>() }
                                LaunchedEffect(Unit) {
                                    qqStickerChunk.forEachIndexed { index, chunk ->
                                        lazyQQStickers.addAll(chunk)
                                        delay(1000)
                                    }
                                }
                                lazyQQStickers.forEachIndexed { _, sticker ->

                                    val isEnable = sticker.enable
                                    if (isEnable) {
                                        AssetLottie(
                                            name = sticker.name,
                                            directory = "sticker",
                                            modifier = Modifier
                                                .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(onTap = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                                                            appViewModel.sysToastText =
                                                                "全员禁言中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁言"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                                                            appViewModel.sysToastText =
                                                                "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        } else {
                                                            appViewModel.lastSendTime =
                                                                System.currentTimeMillis()
                                                        }
                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.EMOJI
                                                        )
                                                        appViewModel.messages.add(sendingMsg)
                                                        ztViewModel.enteredRoom.value.players.forEach {
                                                            if (it.ip != myIp)
                                                                appViewModel.sendMessage(
                                                                    it.ip,
                                                                    sendingMsg
                                                                )
                                                        }

                                                        if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                                                            appViewModel.isInitialized("bulletWindowManager")
                                                        ) appViewModel.bulletWindowManager.playBulletMessage(
                                                            sendingMsg
                                                        )
                                                        // 更新贴纸使用次数
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.copy(
                                                                stickerManage = ztViewModel.appSetting.value.stickerManage.toMutableList()
                                                                    .map {
                                                                        if (it.name == sticker.name) {
                                                                            it.copy(usageCounter = it.usageCounter + 1)
                                                                        } else {
                                                                            it
                                                                        }
                                                                    }
                                                            )
                                                        ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                                        isSelectingEmoji = false
                                                    }, onLongPress = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllSticker) {
                                                            appViewModel.sysToastText =
                                                                "全员禁贴纸中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendStickerList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁止发送贴纸"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.STICKER,
                                                            stickerState = StickerState.SEND,
                                                        )
                                                        appViewModel.editingSticker = sendingMsg
                                                        editSticker.show()
                                                        isSelectingEmoji = false
                                                    })
                                                }
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // capoo sticker
                                val capooStickers =
                                    ztViewModel.appSetting.value.stickerManage.filter {
                                        it.name.startsWith("capoo_")
                                    }
                                val capooStickerChunk = capooStickers.chunked(10)
                                val lazyCapooStickers = remember { mutableStateListOf<Sticker>() }
                                LaunchedEffect(Unit) {
                                    capooStickerChunk.forEachIndexed { index, chunk ->
                                        lazyCapooStickers.addAll(chunk)
                                        delay(1000)
                                    }
                                }
                                lazyCapooStickers.forEachIndexed { _, sticker ->
                                    val isEnable = sticker.enable
                                    if (isEnable) {
                                        AssetLottie(
                                            name = sticker.name,
                                            directory = "sticker",
                                            modifier = Modifier
                                                .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(onTap = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                                                            appViewModel.sysToastText =
                                                                "全员禁言中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁言"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                                                            appViewModel.sysToastText =
                                                                "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        } else {
                                                            appViewModel.lastSendTime =
                                                                System.currentTimeMillis()
                                                        }
                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.EMOJI
                                                        )

                                                        appViewModel.messages.add(sendingMsg)
                                                        ztViewModel.enteredRoom.value.players.forEach {
                                                            if (it.ip != myIp)
                                                                appViewModel.sendMessage(
                                                                    it.ip,
                                                                    sendingMsg
                                                                )
                                                        }
                                                        if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                                                            appViewModel.isInitialized("bulletWindowManager")
                                                        )
                                                            appViewModel.bulletWindowManager.playBulletMessage(
                                                                sendingMsg
                                                            )
                                                        // 更新贴纸使用次数
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.copy(
                                                                stickerManage = ztViewModel.appSetting.value.stickerManage.toMutableList()
                                                                    .map {
                                                                        if (it.name == sticker.name) {
                                                                            it.copy(usageCounter = it.usageCounter + 1)
                                                                        } else {
                                                                            it
                                                                        }
                                                                    }
                                                            )
                                                        ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                                        isSelectingEmoji = false
                                                    }, onLongPress = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllSticker) {
                                                            appViewModel.sysToastText =
                                                                "全员禁贴纸中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendStickerList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁止发送贴纸"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }

                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.STICKER,
                                                            stickerState = StickerState.SEND,
                                                        )
                                                        appViewModel.editingSticker = sendingMsg
                                                        editSticker.show()
                                                        isSelectingEmoji = false
                                                    })
                                                }
                                        )
                                    }
                                }
                            }

                            2 -> {
                                val onlineSticker =
                                    ztViewModel.appSetting.value.stickerManage.filter { it.type == Sticker.StickerType.ONLINE }

                                onlineSticker.forEachIndexed { _, sticker ->
                                    val dir = context.getExternalFilesDir("online_sticker")
                                    val stickerFile = File(dir, sticker.name.toMD5())
                                    val isExist = stickerFile.exists()
                                    val isEnable = sticker.enable
                                    if (isEnable && isExist) {
                                        ExternalImage(name = sticker.name.toMD5(),
                                            contentDescription = "sticker",
                                            modifier = Modifier
                                                .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(onTap = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                                                            appViewModel.sysToastText =
                                                                "全员禁言中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁言"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                                                            appViewModel.sysToastText =
                                                                "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        } else {
                                                            appViewModel.lastSendTime =
                                                                System.currentTimeMillis()
                                                        }
                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.EMOJI
                                                        )
                                                        appViewModel.messages.add(sendingMsg)
                                                        ztViewModel.enteredRoom.value.players.forEach {
                                                            if (it.ip != myIp)
                                                                appViewModel.sendMessage(
                                                                    it.ip,
                                                                    sendingMsg
                                                                )
                                                        }

                                                        if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                                                            appViewModel.isInitialized("bulletWindowManager")
                                                        ) appViewModel.bulletWindowManager.playBulletMessage(
                                                            sendingMsg
                                                        )
                                                        // 更新贴纸使用次数
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.copy(
                                                                stickerManage = ztViewModel.appSetting.value.stickerManage.toMutableList()
                                                                    .map {
                                                                        if (it.name == sticker.name) {
                                                                            it.copy(usageCounter = it.usageCounter + 1)
                                                                        } else {
                                                                            it
                                                                        }
                                                                    }
                                                            )
                                                        ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                                        isSelectingEmoji = false
                                                    }, onLongPress = {
                                                        if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllSticker) {
                                                            appViewModel.sysToastText =
                                                                "全员禁贴纸中"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        if (myIp in ztViewModel.enteredRoom.value.banSendStickerList) {
                                                            appViewModel.sysToastText =
                                                                "您已被禁止发送贴纸"
                                                            sysToast.show()
                                                            return@detectTapGestures
                                                        }
                                                        val sendingMsg = Message(
                                                            ztViewModel.appSetting.value.playerName,
                                                            sticker.name,
                                                            msgType = MsgType.STICKER,
                                                            stickerState = StickerState.SEND,
                                                        )
                                                        appViewModel.editingSticker = sendingMsg
                                                        editSticker.show()
                                                        isSelectingEmoji = false
                                                    })
                                                }
                                        )
                                    }
                                }
                            }

                            else -> Text("Content for Tab ${selectedTabIndex + 1}")
                        }
                    }
                }
            }
        }
        //消息列表
        else {
            if (appViewModel.messages.isEmpty())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AssetLottie(
                        name = "message.lottie",
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .align(Alignment.Center)
                    )
                }
            else
                LazyColumn(
                    state = listState, modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
//                .background(Color.Green)
                ) {
                    itemsIndexed(appViewModel.messages, { index, _ -> index }) { _, item ->
                        Row(
                            horizontalArrangement = if (item.alignEnd) {
                                Arrangement.End
                            } else {
                                Arrangement.Start
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MessageCard(appViewModel, msg = item)
                        }
                    }
                }
        }
        // 语音、输入框、表情、发送
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
//                .background(Color.Yellow)
        ) {
            //record button
            IconButton(
                onClick = {
                    if (isRecording) {
                        //停止录音
                        isRecording = false
                        appViewModel.stopRecording()
                        isPaused = true
                    } else {
                        if (isPaused) {
                            //取消暂停状态
                            isPaused = false
                        } else {
                            //开始录音
                            recordTime = 0
                            isRecording = appViewModel.startRecording(context)
                        }
                    }
                },
            ) {
                if (isRecording) {
                    Row {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = null)
                        Text(text = "${recordTime}S", fontSize = 10.sp)
                    }
                } else {
                    if (isPaused) {
                        Row {
                            Icon(
                                imageVector = Icons.Filled.AudioFile,
                                contentDescription = null
                            )
                            Text(text = "${recordTime}S", fontSize = 10.sp)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.KeyboardVoice,
                            contentDescription = null
                        )
                    }
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.White.copy(0.5f),
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedBorderColor = Color.White,
                    cursorColor = Color.Gray,
                ),
                modifier = Modifier
                    .weight(0.8f)
                    .focusable()
                    .height(32.dp)
            )
            //emoji button
            IconButton(onClick = {
                isSelectingEmoji = !isSelectingEmoji
            }) {
                Icon(imageVector = Icons.Filled.EmojiEmotions, contentDescription = null)
            }
            //send button
            IconButton(onClick = {
                if (myIp != ztViewModel.enteredRoom.value.roomOwnerIp && ztViewModel.enteredRoom.value.banAllMessage) {
                    appViewModel.sysToastText = "全员禁言中"
                    sysToast.show()
                    return@IconButton
                }
                if (myIp in ztViewModel.enteredRoom.value.banSendMsgList) {
                    appViewModel.sysToastText =
                        "您已被禁言"
                    sysToast.show()
                    return@IconButton
                }
                if (System.currentTimeMillis() - appViewModel.lastSendTime < appViewModel.msgInterval * 1000) {
                    appViewModel.sysToastText =
                        "过于频繁，等待${(appViewModel.msgInterval * 1000 + appViewModel.lastSendTime - System.currentTimeMillis()) / 1000}s"
                    sysToast.show()
                    return@IconButton
                } else {
                    appViewModel.lastSendTime = System.currentTimeMillis()
                }
                if (input.isNotEmpty()) {
                    //将要发送的文本消息
                    val sendingMsg = Message(ztViewModel.appSetting.value.playerName, input)
                    appViewModel.messages.add(sendingMsg)
//                    showToast(context, "send message")
                    ztViewModel.enteredRoom.value.players.forEach {
                        if (it.ip != myIp)
                            appViewModel.sendMessage(it.ip, sendingMsg)
                    }
                    if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                        appViewModel.isInitialized("bulletWindowManager")
                    )
                        appViewModel.bulletWindowManager.playBulletMessage(sendingMsg)
                    input = ""
                }
                if (appViewModel.sendingVoice.isNotEmpty()) {
                    //将要发送的语音消息
                    if (isPaused) {
                        isPaused = false
                        isRecording = false
                        val sendingMsg = Message(
                            ztViewModel.appSetting.value.playerName,
                            appViewModel.sendingVoice,
                            msgType = MsgType.AUDIO,
                            audioDuration = appViewModel.audioDuration
                        )
                        appViewModel.messages.add(sendingMsg)
                        ztViewModel.enteredRoom.value.players.forEach {
                            if (it.ip != myIp)
                                appViewModel.sendMessage(
                                    it.ip, sendingMsg
                                )
                        }
                    }
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}