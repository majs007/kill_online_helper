package kill.online.helper.server

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MessageService : Service() {
    private val TAG = "MessageService"
    private val mBinder: IBinder = MessageBinder()
    val httpServer: HttpServer = HttpServer()
    private lateinit var onStart: () -> Unit
    private lateinit var onStop: () -> Unit

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind: ")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        httpServer.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        httpServer.stop()
        super.onDestroy()
    }


    inner class MessageBinder : Binder() {
        val server: HttpServer
            get() = this@MessageService.httpServer

        fun setCallBack(onStart: () -> Unit, onStop: () -> Unit) {
            this@MessageService.onStart = onStart
            this@MessageService.onStop = onStop
        }

    }
}