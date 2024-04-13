package com.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.online.helper.ui.components.Title

@Composable
fun SettingsContent( appNavController: NavHostController,scaffoldNavController: NavHostController,) {
    val settings = remember { listOf("高级网络设置", "悬浮球设置", "房间设置", "关于") }
    val settingsIcon = remember {
        listOf(
            Icons.Filled.Wifi, Icons.Filled.ChildCare,
            Icons.Outlined.Home, Icons.Outlined.Info
        )
    }
    LazyColumn(modifier = Modifier) {
        item {
            Title(text = { settings[0] })

        }
        item {
            Title(text = { settings[1] })

        }
        item {
            Title(text = { settings[2] })

        }
        item {
            Title(text = { settings[3] })

        }
    }
}