package kill.online.helper


import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kill.online.helper.route.Navigation
import kill.online.helper.ui.theme.killTheme
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.FileUtils
import kill.online.helper.viewModel.SharedViewModel


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化悬浮窗
        FloatingWindowFactory.setApplicationContext(applicationContext)
        FileUtils.applicationContext = applicationContext
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        /* val sharedUri =
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                 intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
             } else {
                 intent.getParcelableExtra(Intent.EXTRA_STREAM)
             }*/

        setContent {
            killTheme {
                SharedViewModel.appViewModel = viewModel()
                SharedViewModel.ztViewModel = viewModel()
                SharedViewModel.ztViewModel.initZTConfig(this)
                SharedViewModel.ztViewModel.loadZTConfig()
                val externalNavController = rememberNavController()
                Navigation(externalNavController, sharedText)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "MainActivity onDestroy")
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            Toast.makeText(this, "Entered PiP mode", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Exited PiP mode", Toast.LENGTH_SHORT).show()
        }
    }
}