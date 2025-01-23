package kill.online.helper.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.data.Message
import kill.online.helper.data.MsgType
import kill.online.helper.ui.theme.FloatingWindowPadding
import kill.online.helper.ui.theme.floatingWindowWidth
import kill.online.helper.ui.theme.messageAlpha
import kill.online.helper.viewModel.AppViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("NewApi", "DefaultLocale")
@Composable
fun MessageCard(
    appViewModel: AppViewModel = viewModel(),
    msg: Message,
) {
    val alignEnd = msg.alignEnd
    Row(
        modifier = Modifier.padding(all = 8.dp),
    ) {
        if (!alignEnd) {
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
//                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
            val text = if (msg.alignEnd) "${
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                    Date(msg.timeStamp)
                )
            } ${msg.playerName}"
            else "${msg.playerName} ${
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                    Date(
                        msg.timeStamp
                    )
                )
            }"
            Text(
                text = text, color = Color.White, style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = messageAlpha),
            ) {
                when (msg.msgType) {
                    MsgType.TEXT -> Text(
                        text = msg.msg,
                        modifier = Modifier.padding(all = 8.dp),
                        maxLines = 10,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    MsgType.EMOJI -> {
                        /*     DrawImage(
                                 name = msg.msg,
                                 contentDescription = "emoji",
                                 modifier = Modifier
                                     .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                     .padding(5.dp)
                             )*/
                        AssetLottie(
                            name = msg.msg,
                            directory = "sticker",
                            modifier = Modifier
                                .size((floatingWindowWidth - 2 * FloatingWindowPadding) / 5)
                                .padding(5.dp),
                        )

                    }

                    MsgType.AUDIO -> Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
//                            .background(Color.Red.copy(alpha = messageAlpha))
                    ) {
                        var isPlaying by remember { mutableStateOf(false) }
                        var audioDuration by remember { mutableFloatStateOf(0f) }
                        var audioWave by remember { mutableStateOf("🔈") }
                        LaunchedEffect(isPlaying) {
                            val audioDelay = 200L
                            while (isPlaying) {
                                if (audioDuration <= 0) {
                                    isPlaying = false
                                    audioWave = "🔈"
                                    break
                                }
                                audioWave = "🔈"
                                delay(audioDelay)
                                audioWave = "🔉"
                                delay(audioDelay)
                                audioWave = "🔊"
                                delay(audioDelay)
                                audioDuration -= audioDelay.toFloat() * 3 / 1000
                            }
                        }
                        IconButton(onClick = {
                            if (isPlaying) {
                                isPlaying = false
                                audioWave = "🔈"
                            } else {
                                isPlaying = true
                                appViewModel.playAudio(msg.msg)
                                audioDuration = msg.audioDuration
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "play audio"
                            )
                        }
                        Text(
                            text = audioWave,
                            fontSize = 28.sp,
                            modifier = Modifier.offset(x = 0.dp, y = (-4).dp)
//                                .background(Color.Green.copy(alpha = messageAlpha))
                        )
                        Text(
                            text = " ${String.format("%.1f", msg.audioDuration)}s   ",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
//                                .background(Color.Blue.copy(alpha = messageAlpha))
                        )
                    }

                    else -> {
                    }
                }
            }
        }
        if (alignEnd) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
//                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )

        }
    }
}