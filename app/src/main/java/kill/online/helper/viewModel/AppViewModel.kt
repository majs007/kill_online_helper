package kill.online.helper.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kill.online.helper.client.NetworkRepository
import kill.online.helper.data.Message
import kill.online.helper.data.MessageResponse
import kill.online.helper.data.MsgType
import kill.online.helper.data.Room
import kill.online.helper.server.HttpServer
import kill.online.helper.server.HttpServer.Companion.HTTP_PORT_SERVER
import kill.online.helper.server.HttpServer.Companion.URI_MESSAGE
import kill.online.helper.server.MessageService
import kill.online.helper.server.RecordService
import kill.online.helper.ui.page.BulletWindowManager
import kill.online.helper.ui.window.FloatingWindowFactory
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


//android 相关
class AppViewModel : ViewModel() {
    private val TAG = "AppViewModel"
    val gson = Gson()
    var isHTTPRunning by mutableStateOf(false)

    private lateinit var msgServer: HttpServer
    private val msgConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@AppViewModel.msgServer = (iBinder as MessageService.MessageBinder).server
            msgServer.setListener(onReceiveMessage = ::onReceivedMessage)

            iBinder.startMsgServer(onStart = { isHTTPRunning = true },
                onStop = { isHTTPRunning = false })
            Log.i(TAG, "onServiceConnected: succeed to bind msg Service")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind msg Service")
        }
    }

    private lateinit var recordBinder: RecordService.RecordBinder
    private val recordConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@AppViewModel.recordBinder = (iBinder as RecordService.RecordBinder)
            recordBinder.initRecorder(recorder)
            recordBinder.setOnStop { isInitRecord = false }
            isInitRecord = true
            Log.i(TAG, "onServiceConnected: succeed to bind record Service")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: succeed to unbind record Service")
        }
    }

    var isAddRule = mutableStateOf(false)
    var isShowTips by mutableStateOf(false)

    val messages = mutableStateListOf<Message>()

    lateinit var permissionRequestLauncher: ManagedActivityResultLauncher<String, Boolean>

    var isInitRecord by mutableStateOf(false)
    private lateinit var recorder: MediaRecorder
    private val mediaPlayer = MediaPlayer()
    private var audioFile: File = File.createTempFile("record_audio", "3gp")
    var audioDuration by mutableFloatStateOf(0f)
    var sendingVoice by mutableStateOf("")

    var toScreenOffset by mutableStateOf(Offset(0f, 0f))
    var screenWidth by mutableFloatStateOf(2230f)
    var screenHeight by mutableFloatStateOf(1080f)

    //系统Toast
    var sysToastText by mutableStateOf("")

    // 上次发送消息的时间
    var lastSendTime by mutableLongStateOf(0L)

    // 消息发送间隔
    var msgInterval by mutableIntStateOf(10)
    var editingSticker by mutableStateOf(Message())
    var receivedSticker by mutableStateOf(Message())
    private var receivedAudio by mutableStateOf("")

    lateinit var bulletWindowManager: BulletWindowManager
    var bulletMessages = mutableStateListOf<Message>()


    fun sendMessage(ip: String, msg: Message) {
        NetworkRepository.appClient.sendMessage(
            "http://$ip:${HTTP_PORT_SERVER}${URI_MESSAGE}", msg
        ).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(
                call: Call<MessageResponse>, response: Response<MessageResponse>
            ) {
                val body = response.body()
                Log.i(TAG, "onResponse: $body")
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e(TAG, "onFailure: sendMessage failed")
            }
        })
    }

    fun download(
        url: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        NetworkRepository.appClient.download(
            url
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val contentLength = response.headers()["Content-Length"]?.toLong() ?: -1L
                Log.i(TAG, "onResponse: contentLength = $contentLength")
                if (contentLength > 5 * 1024 * 1024) {
                    onFailure(Throwable("文件过大"))
                    return
                }
                response.body()?.let {
                    onSuccess(it)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        }
        )
    }

    fun startMsgServer(context: Context) {
        val msgIntent = Intent(context, MessageService::class.java)
        context.bindService(
            msgIntent, this.msgConnection, Context.BIND_NOT_FOREGROUND or Context.BIND_DEBUG_UNBIND
        )
        context.startService(msgIntent)
    }

    fun stopMsgServer(context: Context) {
        context.unbindService(msgConnection)
        val msgIntent = Intent(context, MessageService::class.java)
        if (context.stopService(msgIntent)) {
            Log.e(TAG, "stopMsgServer returned false")
        }
    }

    fun initRecorder(recorder: MediaRecorder) {
        if (this::recorder.isInitialized) return
        this.recorder = recorder
    }

    fun initPermissionRequestLauncher(permissionRequestLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        if (this::permissionRequestLauncher.isInitialized) return
        this.permissionRequestLauncher = permissionRequestLauncher
    }

    fun startRecording(context: Context): Boolean {
        // 检查当前是否已经有录音权限
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            // 如果已有权限，提示用户
//            Toast.makeText(context, "已授予权限", Toast.LENGTH_SHORT)
//                .show()
            /*    if (!isInitRecord) {
                    val recordIntent = Intent(context, RecordService::class.java)
                    context.bindService(recordIntent, this.recordConnection, Context.BIND_DEBUG_UNBIND)
                    startForegroundService(context, recordIntent)
                }
                if (this::recordBinder.isInitialized)
                    recordBinder.startRecord()
                else return false*/
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
            return true
        } else {
            // 如果没有权限，请求权限
            permissionRequestLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
    }

    fun stopRecording() {
        /*  recordBinder.stopRecord { audio, duration ->
              sendingVoice = audio
              audioDuration = duration
          }*/
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
    }

    private fun readAudioFile(): ByteArray {
        val bis = BufferedInputStream(FileInputStream(audioFile))
        val data = ByteArray(audioFile.length().toInt())
        bis.read(data)
        bis.close()
        return data
    }

    @SuppressLint("NewApi")
    private fun onReceivedMessage(newMsg: Message): MessageResponse {
        newMsg.apply { alignEnd = false }
        when (newMsg.msgType) {
            MsgType.TEXT -> {
                messages.add(newMsg)
                if (SharedViewModel.ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                    this::bulletWindowManager.isInitialized
                )
                    bulletWindowManager.playBulletMessage(newMsg)
            }

            MsgType.EMOJI -> {
                messages.add(newMsg)
                if (SharedViewModel.ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage and
                    this::bulletWindowManager.isInitialized
                )
                    bulletWindowManager.playBulletMessage(newMsg)
            }

            MsgType.AUDIO -> {
                messages.add(newMsg)
                receivedAudio = newMsg.msg
                if (SharedViewModel.ztViewModel.appSetting.value.fwRoomSetting.autoPlayAudio)
                    playAudio(receivedAudio)
            }

            MsgType.STICKER -> {
                receivedSticker = newMsg
                Handler(Looper.getMainLooper()).post {
                    val receiveFW = FloatingWindowFactory.getFloatingWindow("receivedSticker")
                    receiveFW.windowParams.x =
                        (screenWidth * newMsg.imagePositionRadio.x).toInt()
                    receiveFW.windowParams.y =
                        (screenHeight * newMsg.imagePositionRadio.y).toInt()
                    receiveFW.show()
                }
                Log.i(
                    TAG,
                    "onReceivedMessage:screenWidth:$screenWidth screenHeight:$screenHeight imagePositionRadio ${receivedSticker.imagePositionRadio}"
                )
            }

            MsgType.CONFIG -> {
                val configItem = gson.fromJson(newMsg.msg, Message.ConfigItem::class.java)
                when (configItem.key) {
                    //成员处理
                    "roomBroadcast" -> {
                        val value = configItem.value
                        val room = gson.fromJson(value, Room::class.java)
                        val index =
                            SharedViewModel.ztViewModel.rooms.indexOfFirst { it.roomOwnerIp == room.roomOwnerIp }
                        if (index == -1) SharedViewModel.ztViewModel.rooms.add(room)
                        else SharedViewModel.ztViewModel.rooms[index] = room
                        val myIp = SharedViewModel.ztViewModel.getAssignedIP()
                        if (myIp in room.players.map { it.ip }) {
                            SharedViewModel.ztViewModel.enteredRoom.value = room
                        }
                        Log.i(TAG, "onReceivedMessage: room $value")
                    }

                    "ztToIP" -> {
                        val value = configItem.value.split(':')
                        SharedViewModel.ztViewModel.ztToIP[value[0]] = value[1]
                        Log.i(TAG, "onReceivedMessage: ztToIP: $value")
                    }
                    //房主处理
                    "onRequestEnterRoom" -> {
                        val value = configItem.value
                        val member = gson.fromJson(value, Room.RoomMember::class.java)
                        val players = SharedViewModel.ztViewModel.enteredRoom.value.players
                        val index =
                            SharedViewModel.ztViewModel.enteredRoom.value.players.indexOfFirst { it.ip == member.ip }
                        if (index == -1) SharedViewModel.ztViewModel.enteredRoom.value =
                            SharedViewModel.ztViewModel.enteredRoom.value.copy(
                                players = players.toMutableList().apply { add(member) }.toList()
                            )
                        else SharedViewModel.ztViewModel.enteredRoom.value =
                            SharedViewModel.ztViewModel.enteredRoom.value.copy(
                                players = players.toMutableList().apply { set(index, member) }
                            )
                        Log.i(TAG, "onRequestEnterRoom: member $value")
                    }
                    //房主处理
                    "onRequestQuitRoom" -> {
                        val value = configItem.value
                        val member = gson.fromJson(value, Room.RoomMember::class.java)
                        val players = SharedViewModel.ztViewModel.enteredRoom.value.players
                        val index =
                            SharedViewModel.ztViewModel.enteredRoom.value.players.indexOfFirst { it.ip == member.ip }
                        if (index != -1) SharedViewModel.ztViewModel.enteredRoom.value =
                            SharedViewModel.ztViewModel.enteredRoom.value.copy(
                                players = players.toMutableList().apply { removeAt(index) }.toList()
                            )
                        Log.i(TAG, "onRequestQuitRoom: member $value")
                    }
                }
            }
        }
        return MessageResponse()
    }

    fun playAudio(base64Audio: String = "") {
        val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
        // 创建临时文件
        val tempFile = File.createTempFile("playing_audio", ".3gp")
        // 确保程序结束时删除临时文件
        tempFile.deleteOnExit()
        tempFile.writeBytes(audioBytes)
        mediaPlayer.apply {
            reset()
            setDataSource(tempFile.absolutePath)
            prepare()
            start()
        }
    }

    fun isInitialized(filed: String): Boolean {
        when (filed) {
            "msgServer" -> return this::msgServer.isInitialized
            "recorder" -> return this::recorder.isInitialized
            "permissionRequestLauncher" -> return this::permissionRequestLauncher.isInitialized
            "bulletWindowManager" -> return this::bulletWindowManager.isInitialized
        }
        return false
    }

    override fun onCleared() {
        mediaPlayer.release()
        super.onCleared()
    }
}