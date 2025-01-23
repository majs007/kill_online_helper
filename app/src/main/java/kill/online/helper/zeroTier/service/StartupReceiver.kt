package kill.online.helper.zeroTier.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kill.online.helper.zeroTier.util.Constants


class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received: " + intent.action + " Starting ZeroTier One service.")
        val pref = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
        if (pref.getBoolean(Constants.PREF_GENERAL_START_ZEROTIER_ON_BOOT, true)) {
            Log.i(TAG, "Preferences set to start ZeroTier on boot")
            // TODO 一般不开机自启
        } else {
            Log.i(TAG, "Preferences set to not start ZeroTier on boot")
        }
    }

    companion object {
        private const val TAG = "StartupReceiver"
    }
}
