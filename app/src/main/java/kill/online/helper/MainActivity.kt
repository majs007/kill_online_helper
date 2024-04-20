package kill.online.helper


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import kill.online.helper.route.Navigation
import kill.online.helper.ui.theme.killTheme
import kill.online.helper.ui.window.FloatingWindowFactory


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化悬浮窗
        FloatingWindowFactory.setApplicationContext(applicationContext)

        setContent {
            killTheme {
                val externalNavController = rememberNavController()
                Navigation(externalNavController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "MainActivity onDestroy")
    }
}