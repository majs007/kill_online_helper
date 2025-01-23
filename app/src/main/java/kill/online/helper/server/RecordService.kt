package kill.online.helper.server


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class RecordService : Service() {
    private val TAG = "RecordService"
    private val mBinder: IBinder = RecordBinder()
    private lateinit var notification: Notification
    private lateinit var recorder: MediaRecorder
    private val mediaPlayer = MediaPlayer()
    private var audioFile: File = File.createTempFile("record_audio", "3gp")
    var sendingVoice by mutableStateOf("")
    var audioDuration by mutableFloatStateOf(0f)
    private lateinit var onStop: () -> Unit
    private val CHANNEL_ID = "my_foreground_service_channel"

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind: ")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)  // 将服务提升为前台服务

        // 在录音过程中定时发送更新通知，避免被杀死
        Handler(Looper.getMainLooper()).postDelayed({
            startForeground(1, notification)
        }, 1000)  // 每 10 秒刷新一次前台通知
        return START_STICKY
    }

    override fun onDestroy() {
        recorder.release()
        Log.i(TAG, "onDestroy: stop http server")
        super.onDestroy()
    }

    // 创建通知渠道（适用于 Android 8.0 及以上版本）
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Foreground Service"
            val descriptionText = "Channel for Foreground Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 创建前台服务通知
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true) // 设置通知无法被滑动删除
            .setContentTitle("My Service is Running")
            .setContentText("Tap to interact with the service.")
            .setSmallIcon(android.R.drawable.stat_notify_sync) // 设置通知图标
            .build()
    }


    inner class RecordBinder : Binder() {
        /*  val recorder: MediaRecorder
              get() = this@RecordService.recorder*/
        fun setOnStop(onStop: () -> Unit) {
            this@RecordService.onStop = onStop
        }

        fun initRecorder(recorder: MediaRecorder) {
            this@RecordService.recorder = recorder
        }

        fun startRecord(onStart: () -> Unit = {}) {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                    start()
                } catch (e: Exception) {
                    Log.e(TAG, "startRecord: error\n${e.message}")
                }
            }
            onStart()
            Log.i(TAG, "setCallBack: start recorder")
        }

        fun stopRecord(callback: (String, Float) -> Unit) {
            recorder.apply {
                stop()
                reset()
            }
            sendingVoice = Base64.encodeToString(readAudioFile(), Base64.DEFAULT)
            mediaPlayer.apply {
                reset()
                setDataSource(audioFile.absolutePath)
                prepare()
                audioDuration = duration.toFloat() / 1000
            }
            callback(sendingVoice, audioDuration)
            Log.i(TAG, "setCallBack: stop recorder")
        }

        private fun readAudioFile(): ByteArray {
            val bis = BufferedInputStream(FileInputStream(audioFile))
            val data = ByteArray(audioFile.length().toInt())
            bis.read(data)
            bis.close()
            return data
        }

    }
}