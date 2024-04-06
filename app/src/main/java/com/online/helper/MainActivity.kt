package com.online.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.online.helper.ui.window.ComposeFloatingWindow
import com.online.helper.ui.window.dragFloatingWindow
import com.online.helper.ui.components.BasicItemContainer
import com.online.helper.ui.page.HomeBottomSheet
import com.online.helper.ui.page.HomeContent
import com.online.helper.ui.page.RuleBottomSheet
import com.online.helper.ui.page.RuleContent
import com.online.helper.ui.page.SettingsContent
import com.online.helper.ui.theme.Kill联机助手Theme
import com.online.helper.ui.window.FloatingWindowFactory


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化悬浮窗
        FloatingWindowFactory.setApplicationContext(applicationContext)
        setContent {
            Kill联机助手Theme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun AppScaffold() {
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val homeSheetState = rememberModalBottomSheetState()
    val showHomeBottomSheet = rememberSaveable { mutableStateOf(false) }
    val ruleSheetState = rememberModalBottomSheetState()
    val showRuleBottomSheet = rememberSaveable { mutableStateOf(false) }
    val items = listOf("大厅", "玩家", "规则", "设置")
    val topBarTitle = listOf("大厅", "玩家列表", "房间规则", "设置")
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = topBarTitle[selectedItem]) }) },
        bottomBar = {
            BottomAppBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.Home, contentDescription = null)
                                1 -> Icon(Icons.Filled.Face, contentDescription = null)
                                2 -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                3 -> Icon(Icons.Filled.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        },
        floatingActionButton = {
            val applicationContext = LocalContext.current.applicationContext
            val context = LocalContext.current

            when (selectedItem) {
                0 -> {
                    FloatingActionButton(onClick = {

                        FloatingWindowFactory.getFloatingWindow("tag1") {
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.dragFloatingWindow()
                            ) {
                                Text(text = "悬浮按钮")
                            }
                        }.show()


                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }

                2 -> {
                    FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End

    ) { it ->
        val topAppBarPadding = it.calculateTopPadding()
        val bottomAppBarPadding = it.calculateBottomPadding()
        Column(
            modifier = Modifier
                .padding(top = topAppBarPadding, bottom = bottomAppBarPadding)
                .fillMaxSize()
//                .background(Color.Cyan)
        ) {
            HomeBottomSheet(players, showHomeBottomSheet, homeSheetState)
            RuleBottomSheet(showRuleBottomSheet, ruleSheetState)


            when (selectedItem) {
                0 -> HomeContent { showHomeBottomSheet.value = true }
                1 -> PlayerContent()
                2 -> RuleContent(showRuleBottomSheet)
                3 -> SettingsContent()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerContent() {
    data class PlayerInfo(val name: String, val lastSeen: Int)

    val players = rememberSaveable {
        listOf(
            PlayerInfo("章鱼哥", 0), PlayerInfo("派大星", 1),
            PlayerInfo("海绵宝宝", 2), PlayerInfo("小蜗", 3),
            PlayerInfo("蟹老板", 4), PlayerInfo("神秘奇男子AAA", 5),
            PlayerInfo("章鱼大哥", 6), PlayerInfo("大蜗", 7),
            PlayerInfo("海绵宝宝", 8), PlayerInfo("小蜗", 9),
            PlayerInfo("蟹老板", 10), PlayerInfo("神秘奇男子AAA", 11),
        )
    }
    LazyColumn {
        itemsIndexed(players) { index, item ->
            when (item.lastSeen) {
                0 -> {
                    BasicItemContainer(icon = "🥳", text = { item.name }, subText = { "状态：在线" })
                }

                1 -> {
                    BasicItemContainer(
                        icon = "🥰",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen}天内" })
                }

                2 -> {
                    BasicItemContainer(
                        icon = "😎",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                3 -> {
                    BasicItemContainer(
                        icon = "😶",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                4 -> {
                    BasicItemContainer(
                        icon = "😐",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                5 -> {
                    BasicItemContainer(
                        icon = "🤔",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                6 -> {
                    BasicItemContainer(
                        icon = "😕",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                7 -> {
                    BasicItemContainer(
                        icon = "😥",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                8 -> {
                    BasicItemContainer(
                        icon = "😖",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}天多前" })
                }

                else -> {
                    BasicItemContainer(
                        icon = "😭",
                        text = { item.name },
                        subText = { "状态：${item.lastSeen - 1}多天前" })
                }
            }

        }

    }
}

@Composable
fun FloatingContent() {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "Button")
    }
    Text(text = "hello")
}

