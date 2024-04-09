package com.online.helper


import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.online.helper.data.Message
import com.online.helper.ui.page.HomeBottomSheet
import com.online.helper.ui.page.HomeContent
import com.online.helper.ui.page.PlayerContent
import com.online.helper.ui.page.RuleBottomSheet
import com.online.helper.ui.page.RuleContent
import com.online.helper.ui.page.SettingsContent
import com.online.helper.ui.theme.FloatingWindowPadding
import com.online.helper.ui.theme.Kill联机助手Theme
import com.online.helper.ui.theme.chipPadding
import com.online.helper.ui.theme.floatingBallAlpha
import com.online.helper.ui.theme.floatingWindowBackgroundAlpha
import com.online.helper.ui.theme.floatingWindowBarAlpha
import com.online.helper.ui.theme.floatingWindowCorner
import com.online.helper.ui.theme.floatingWindowHeight
import com.online.helper.ui.theme.floatingWindowItemAlpha
import com.online.helper.ui.theme.floatingWindowWidth
import com.online.helper.ui.theme.messageAlpha
import com.online.helper.ui.theme.topAppBarActionPadding
import com.online.helper.ui.window.ComposeFloatingWindow
import com.online.helper.ui.window.FloatingWindowFactory
import com.online.helper.ui.window.dragFloatingWindow
import com.online.helper.utils.showToast


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
    val navigationItems = listOf("大厅", "玩家", "规则", "设置")
    val topBarTitle = listOf("大厅", "玩家列表", "房间规则", "设置")
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = topBarTitle[selectedItem]) }) },
        bottomBar = {
            BottomAppBar {
                navigationItems.forEachIndexed { index, item ->
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
            val context = LocalContext.current
            val floatingBall =
                FloatingWindowFactory.getFloatingWindow(
                    "floatingBall",
                    content = { fb ->
                        IconButton(
                            onClick = {
                                fb.hide()
                                FloatingWindowFactory.getFloatingWindow("floatingWindow").show()
                            },
                            modifier = Modifier
                                .dragFloatingWindow()
                                .background(
                                    Color.Black.copy(alpha = floatingBallAlpha),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = null
                            )
                        }
                    },
                    config = { fb ->
                        fb.setCallback(
                            onShow = {
                                showToast(context, "显示悬浮窗")
                                Log.i("onShow", "onShow callback")
                            },
                            onHide = {
                                showToast(context, "隐藏悬浮窗")
                                Log.i("onHide", "onHide callback")

                            }
                        ).setLayoutParams { params ->
                            val decorView = fb.decorView
                            val f = Rect().also { decorView.getWindowVisibleDisplayFrame(it) }
                            params.x = (f.width() - fb.contentWidth) / 2
                            params.y = (f.height() - fb.contentHeight) / 2
                        }
                    })

            val floatingWindow =
                FloatingWindowFactory.getFloatingWindow(
                    "floatingWindow",
                    content = { fw -> FloatingWindowScaffold(fw = (fw)) },
                    config = { fw ->
                        fw.setCallback(
                            onShow = {
                                showToast(context, "显示悬浮窗")
                                Log.i("onShow", "onShow callback")
                            },
                            onHide = {
                                showToast(context, "隐藏悬浮窗")
                                Log.i("onHide", "onHide callback")

                            }
                        ).setLayoutParams { params ->
                            val decorView = fw.decorView
                            val f = Rect().also { decorView.getWindowVisibleDisplayFrame(it) }
                            params.x = (f.width() - fw.contentWidth) / 2
                            params.y = (f.height() - fw.contentHeight) / 2
                        }
                    })

            when (selectedItem) {
                0 -> {
                    FloatingActionButton(onClick = {
                        try {
                            if (!floatingBall.isShowing() && !floatingWindow.isShowing()) {
                                floatingBall.show()
                            }


                        } catch (e: Exception) {
                            Log.e("FloatingActionButton", "${e.message}")
                        }


                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        ButtonDefaults.MinWidth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowScaffold(fw: ComposeFloatingWindow) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val navigationItems = listOf("聊天", "成员", "设置")
    val topBarTitle = listOf("房间聊天", "房间成员", "设置")
    Surface(
        color = Color.Black.copy(alpha = floatingWindowBackgroundAlpha),
        shape = RoundedCornerShape(floatingWindowCorner),
        contentColor = Color.White
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(floatingWindowBarAlpha),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = { Text(text = topBarTitle[selectedItem]) },
                    actions = {
                        IconButton(
                            onClick = {
                                fw.hide()
                                FloatingWindowFactory.getFloatingWindow("floatingBall").show()
                            },
                            modifier = Modifier
                                .padding(horizontal = topAppBarActionPadding)
//                                .background(
//                                    Color.White.copy(alpha = floatingWindowItemAlpha),
//                                    shape = CircleShape
//                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloseFullscreen,
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                fw.hide()
                            },
                            modifier = Modifier
                                .padding(horizontal = topAppBarActionPadding)
//                                .background(
//                                    Color.White.copy(alpha = floatingWindowItemAlpha),
//                                    shape = CircleShape
//                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.dragFloatingWindow()
                )
            },
            bottomBar = {
                BottomAppBar(
                    contentColor = Color.White,
                    containerColor = Color.Black.copy(floatingWindowBarAlpha),
                    modifier = Modifier.dragFloatingWindow()
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.White),
                            icon = {
                                when (index) {
                                    0 -> Icon(
                                        Icons.AutoMirrored.Filled.Message,
                                        contentDescription = null
                                    )

                                    1 -> Icon(Icons.Filled.Face, contentDescription = null)
                                    2 -> Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            },
//                        label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index }
                        )
                    }
                }
            },
            modifier = Modifier
                .size(floatingWindowWidth, floatingWindowHeight)
        ) {
            val topAppBarPadding = it.calculateTopPadding()
            val bottomAppBarPadding = it.calculateBottomPadding()
            Column(
                modifier = Modifier
                    .padding(top = topAppBarPadding, bottom = bottomAppBarPadding)
                    .fillMaxSize()
//                .background(Color.Cyan)
            ) {
                when (selectedItem) {
                    0 -> FloatingWindowMessageContent()
                    1 -> FloatingWindowPlayerContent()
                    2 -> FloatingWindowSettingContent()
                }
            }
        }
    }

}

@Composable
fun FloatingWindowSettingContent() {
    Text(text = "FloatingWindowSettingContent")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingWindowPlayerContent() {
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")
    FlowRow(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = FloatingWindowPadding, end = FloatingWindowPadding)
    ) {
        players.forEachIndexed { index, s ->
            AssistChip(
                onClick = { },
                label = { Text(s) },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = Color.White
                ),
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = Color.White,
                    leadingIconContentColor = Color.Cyan
                ),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "Localized description",
//                                Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                modifier = Modifier
                    .padding(start = chipPadding, end = chipPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowMessageContent() {
    val messageList = rememberSaveable {
        listOf(
            Message("海绵宝宝", "hcdakskajcdka"),
            Message("派大星", "hdsjadakjashfdkas"),
            Message("海绵宝宝", "萨达大家积极哦啊就递交哦叫哦耍大刀i就"),
            Message("派大星", "德哈卡都去啊点进去哦ID去哦去哦的放弃哦i"),
            Message("章鱼哥", "别在这里发癫😅😅😅"),
        )
    }
    var inputMessage by rememberSaveable { mutableStateOf("德哈卡都去啊点进去哦ID去哦去哦的放弃哦i") }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
//            .background(Color.Red)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
//                .background(Color.Green)
        ) {
            itemsIndexed(messageList) { index, item ->
                MessageCard(item)
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
//                .background(Color.Yellow)
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.KeyboardVoice, contentDescription = null)
            }
            OutlinedTextField(
                value = inputMessage, onValueChange = { inputMessage = it },
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.White.copy(0.5f),
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedBorderColor = Color.White,
                    cursorColor = Color.Gray,
                ),

                modifier = Modifier.weight(0.9f)
            )
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.EmojiEmotions, contentDescription = null)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}


@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
//                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = msg.playerNickname,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = Color.White.copy(messageAlpha),
                modifier = Modifier
                    .padding(1.dp)
                    .focusable(true)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = 10,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
