package com.online.helper


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.online.helper.route.Navigation
import com.online.helper.ui.theme.Kill联机助手Theme
import com.online.helper.ui.window.FloatingWindowFactory


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化悬浮窗
        FloatingWindowFactory.setApplicationContext(applicationContext)
        setContent {
            Kill联机助手Theme {
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