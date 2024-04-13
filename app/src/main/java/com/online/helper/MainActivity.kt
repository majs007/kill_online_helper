package com.online.helper


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.online.helper.route.Navigation
import com.online.helper.ui.theme.Kill联机助手Theme
import com.online.helper.ui.window.FloatingWindowFactory


class MainActivity : ComponentActivity() {


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
}