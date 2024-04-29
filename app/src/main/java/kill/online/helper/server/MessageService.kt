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
        Log.d(TAG, "onStartCommand: ")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        httpServer.stop()
        onStop()
        Log.i(TAG, "onDestroy: stop http server")
        super.onDestroy()
    }


    inner class MessageBinder : Binder() {
        val server: HttpServer
            get() = this@MessageService.httpServer

        fun startMsgServer(onStart: () -> Unit, onStop: () -> Unit) {
            this@MessageService.onStop = onStop
            httpServer.start()
            onStart()
            Log.i(TAG, "setCallBack: start http server")
        }

    }
}