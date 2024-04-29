package kill.online.helper.ui.page

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.data.Message
import kill.online.helper.data.MsgType
import kill.online.helper.ui.theme.messageAlpha
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FloatingWindowMessageContent(
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {

    var input by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordTime by remember { mutableIntStateOf(0) }
    var isTimerPaused by remember { mutableStateOf(false) }
    var isSelectingEmoji by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (!isTimerPaused) {
                recordTime++
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
//            .background(Color.Red)
    ) {
        //选择emoji
        if (isSelectingEmoji) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        appViewModel.toWindowOffset = coordinates.positionInWindow()
                    }
            ) {
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                    ) {

                        Image(imageVector = Icons.Filled.Face,
                            contentDescription = null,
                            modifier = Modifier
                                .offset(
                                    appViewModel.toOriginOffset.x.dp,
                                    appViewModel.toOriginOffset.y.dp
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            //TODO 图片base64编码输入到 emoji

                                            appViewModel.run {
                                                ztViewModel.enteredRoom.value.players.forEach {
                                                    appViewModel.sendMessage(
                                                        it.ip,
                                                        Message(ztViewModel.getMyName(), emoji)
                                                    )
                                                }
                                            }
                                            appViewModel.toOriginOffset = Offset(0f, 0f)
                                            emoji = ""
                                        }
                                    )
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            appViewModel.toOriginOffset =
                                                appViewModel.toOriginOffset.plus(dragAmount)
                                        },
                                        onDragEnd = { //TODO 图片base64编码输入到 emoji

                                        },
                                    )
                                }
                                .onGloballyPositioned { coordinates ->
                                    appViewModel.toRootOffset = coordinates.positionInRoot()
                                }
                        )
                    }
                }
            }
        }
        //消息列表
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
//                .background(Color.Green)
            ) {
                itemsIndexed(appViewModel.messages.value) { index, item ->
                    MessageCard(item)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
//                .background(Color.Yellow)
        ) {
            IconButton(onClick = {
                isRecording = if (isRecording) {
                    appViewModel.stopRecording()
                    isTimerPaused = true
                    false
                } else {
                    appViewModel.startRecording()
                    isTimerPaused = false
                    true
                }
            }) {
                Icon(imageVector = Icons.Filled.KeyboardVoice, contentDescription = null)
                Text(text = "${recordTime}S")
            }
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                maxLines = 1,
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
                    .weight(0.9f)
                    .focusable()
            )
            IconButton(onClick = {
                isSelectingEmoji = !isSelectingEmoji
            }) {
                Icon(imageVector = Icons.Filled.EmojiEmotions, contentDescription = null)
            }
            IconButton(onClick = {
                if (input.isNotEmpty()) {
                    ztViewModel.enteredRoom.value.players.forEach {
                        appViewModel.sendMessage(it.ip, Message(ztViewModel.getMyName(), input))
                    }
                    input = ""
                }
                if (emoji.isNotEmpty()) {
                    appViewModel.run {
                        val imagePositionRadio = Offset(
                            (toOriginOffset.x + toRootOffset.x + toWindowOffset.x + toScreenOffset.x).dp / screenWidth,
                            (toOriginOffset.y + toRootOffset.y + toWindowOffset.y + toScreenOffset.y).dp / screenHeight
                        )
                        ztViewModel.enteredRoom.value.players.forEach {
                            appViewModel.sendMessage(
                                it.ip,
                                Message(
                                    ztViewModel.getMyName(),
                                    emoji,
                                    isDrag = true,
                                    imagePositionRadio = imagePositionRadio
                                )
                            )
                        }
                    }
                    appViewModel.toOriginOffset = Offset(0f, 0f)
                    emoji = ""
                }
                if (appViewModel.voice.isNotEmpty()) {
                    appViewModel.stopRecording()
                    isRecording = false
                    ztViewModel.enteredRoom.value.players.forEach {
                        appViewModel.sendMessage(
                            it.ip,
                            Message(
                                ztViewModel.getMyName(),
                                appViewModel.voice,
                                audioDuration = recordTime
                            )
                        )
                    }
                    appViewModel.voice = ""
                    appViewModel.clearRecord()
                    recordTime = 0
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}


@Composable
fun MessageCard(
    msg: Message,
    appViewModel: AppViewModel = viewModel()
) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
//                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "${msg.playerName} ${
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                        Date(msg.timeStamp)
                    )
                }",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(messageAlpha),
                modifier = Modifier
                    .padding(1.dp)
//
            ) {
                when (msg.type) {
                    MsgType.TEXT -> Text(
                        text = msg.msg,
                        modifier = Modifier.padding(all = 4.dp),
                        maxLines = 10,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    MsgType.IMAGE -> Image(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )

                    MsgType.AUDIO -> IconButton(onClick = {
                        appViewModel.receivedAudio.writeBytes(
                            Base64.decode(
                                msg.msg,
                                Base64.DEFAULT
                            )
                        )
                        appViewModel.playAudio()
                    }) {
                        Icon(imageVector = Icons.Filled.PlayCircle, contentDescription = null)
                        Text(text = "|ǀ|ǀ|ǀ|ǀ|ǀ  ${msg.audioDuration}S")
                    }
                }
            }
        }
    }
}