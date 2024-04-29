package kill.online.helper.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.data.MsgType
import kill.online.helper.server.HttpServer
import kill.online.helper.server.HttpServer.Companion.HTTP_PORT_SERVER
import kill.online.helper.server.HttpServer.Companion.URI_MESSAGE
import kill.online.helper.server.MessageService
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.StateUtils.add
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter


object AppViewModel : ViewModel() {
    private val TAG = "AppViewModel"
    val isHTTPRunning = mutableStateOf(false)

    private lateinit var msgServer: HttpServer
    private val msgConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@AppViewModel.msgServer =
                (iBinder as MessageService.MessageBinder).server
            msgServer.setListener(onReceiveMessage = ::onReceivedMessage)

            iBinder.startMsgServer(onStart = { isHTTPRunning.value = true },
                onStop = { isHTTPRunning.value = false })
            Log.i(TAG, "onServiceConnected: succeed to bind msg Service")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind msg Service")
        }
    }

    var isAddRule = mutableStateOf(false)

    val messages = mutableStateOf(listOf<Message>())
    private var recorder: MediaRecorder = MediaRecorder()
    private val retriever = MediaMetadataRetriever()
    private val mediaPlayer = MediaPlayer()
    private var audioFile: File = File.createTempFile("temp_record", "3gp")
    var voice by mutableStateOf("")

    var toScreenOffset by mutableStateOf(Offset(0f, 0f))
    var toWindowOffset by mutableStateOf(Offset(0f, 0f))
    var toRootOffset by mutableStateOf(Offset(0f, 0f))
    var toOriginOffset by mutableStateOf(Offset(0f, 0f))

    var showReceivedImg by mutableStateOf(false)
    var receivedImg by mutableStateOf(Message())
    var receivedAudio: File = File.createTempFile("received_audio", "3gp")


    fun sendMessage(ip: String, msg: Message) {
        NetworkRepository.appClient.sendMessage(
            "http://$ip:${HTTP_PORT_SERVER}${URI_MESSAGE}",
            msg
        )
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    val body = response.body()
                    Log.i(TAG, "onResponse: $body")
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    t.printStackTrace()
                    Log.w(TAG, "onFailure: sendMessage failed")
                }
            })
    }

    fun startMsgServer(context: Context) {
        val msgIntent = Intent(context, MessageService::class.java)
        context.bindService(
            msgIntent, this.msgConnection,
            Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        context.startService(msgIntent)
    }

    fun stopMsgServer(context: Context) {
        val msgIntent = Intent(context, MessageService::class.java)
        if (context.stopService(msgIntent)) {
            Log.e(TAG, "stopMsgServer returned false")
        }
        context.unbindService(msgConnection)
    }

    fun startRecording() {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording() {
        recorder.apply {
            stop()
            release()
        }
        voice = Base64.encodeToString(readAudioFile(), Base64.DEFAULT)
    }

    fun clearRecord() = BufferedWriter(FileWriter(audioFile)).use { it.write("") }

    private fun readAudioFile(): ByteArray {
        val bis = BufferedInputStream(FileInputStream(audioFile))
        val data = ByteArray(audioFile.length().toInt())
        bis.read(data)
        bis.close()
        return data
    }

    fun getAudioDuration(): Long {
        retriever.setDataSource(audioFile.absolutePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return durationStr?.toLongOrNull() ?: 0
    }

    private fun onReceivedMessage(newMsg: Message): MessageResponse {
        when (newMsg.type) {
            MsgType.TEXT -> {
                add(FileUtils.ItemName.Message, messages, newMsg, autoSave = false) { msgList ->
                    msgList.sortedBy { it.timeStamp }
                }
            }

            MsgType.IMAGE -> {
                receivedImg = newMsg
                showReceivedImg = true
            }

            MsgType.AUDIO -> {
                add(FileUtils.ItemName.Message, messages, newMsg, autoSave = false) { msgList ->
                    msgList.sortedBy { it.timeStamp }
                }
                receivedAudio.writeBytes(Base64.decode(newMsg.msg, Base64.DEFAULT))
                if (ZeroTierViewModel.appSetting.value.fwRoomSetting.autoPlayAudio)
                    playAudio()

            }
        }
        return MessageResponse()
    }

    fun playAudio() {
        mediaPlayer.apply {
            reset()
            setDataSource(receivedAudio.absolutePath)
            prepare()
            start()
        }
    }

    override fun onCleared() {
        mediaPlayer.release()
        super.onCleared()
    }
}