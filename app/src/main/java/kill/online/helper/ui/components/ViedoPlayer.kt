/*
package kill.online.helper.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView

//ExoVideoPlayer
@Composable
fun VideoPlayer(isPlaying: Boolean, url: String) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }
    // 使用AndroidView来嵌入ExoPlayer的PlayerView
    AndroidView(
        factory = {
            StyledPlayerView(context).apply {
                this.player = player
                useController = false
            }
        }
    )
}
*/
