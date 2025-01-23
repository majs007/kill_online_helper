/*
package kill.online.helper.ui.components

import android.content.res.AssetFileDescriptor
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout


@Composable
fun VLCVideoPlayer(
    assFileDes: AssetFileDescriptor, modifier: Modifier = Modifier
) {
    val TAG = "VLCVideoPlayer"
    val context = LocalContext.current
    val options = ArrayList<String>()
    options.add("--vout=android-display")
    options.add("--no-drop-late-frames")
    options.add("--no-skip-frames")
    options.add("--no-interact")
    options.add("--avcodec-hw=1")
    options.add("--file-caching=100")

    val libVLC = LibVLC(context, options)

    val media by remember {
        mutableStateOf(Media(libVLC, assFileDes))
    }
    media.setHWDecoderEnabled(true, true)

    val mediaPlayer = MediaPlayer(libVLC)
    mediaPlayer.media = media

    val scope = rememberCoroutineScope()
    var endTime by remember { mutableLongStateOf(0) }


    // SurfaceView 用于显示 VLC 播放的内容
    AndroidView(
        factory = { _ ->
            VLCVideoLayout(context).apply {
                mediaPlayer.attachViews(this, null, false, false)
                mediaPlayer.setEventListener {
                    when (it.type) {
                        MediaPlayer.Event.Playing -> {
//                            Log.i(TAG, "Event.Playing: time: ${mediaPlayer.time}")
                            scope.launch {
                                val delayTime =
                                    if (endTime == 0L) (media.duration - 500L) else endTime
                                delay(delayTime)
                                Handler(Looper.getMainLooper()).post {
                                    if (mediaPlayer.time >= endTime) {
                                        mediaPlayer.pause()  // 在主线程中暂停播放
                                    }
                                }
                            }
                        }

                        MediaPlayer.Event.Paused -> {
//                            Log.i(TAG, "Event.Paused: time: ${mediaPlayer.time}")
                            mediaPlayer.time = 0
                            mediaPlayer.play()
                        }

                        MediaPlayer.Event.EndReached -> {
                            Log.i(
                                TAG,
                                "Event.EndReached: time: ${media.duration - mediaPlayer.time}"
                            )
                            endTime = mediaPlayer.time
                        }
                    }
                }
                mediaPlayer.play()
            }
        }, modifier = modifier
    )
}

*/
