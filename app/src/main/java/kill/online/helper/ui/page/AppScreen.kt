package kill.online.helper.ui.page

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kill.online.helper.data.Message
import kill.online.helper.route.Route
import kill.online.helper.route.ScaffoldNavigation
import kill.online.helper.route.appBottomBarLabel
import kill.online.helper.route.appNavItem
import kill.online.helper.route.appTopBarTitle
import kill.online.helper.ui.components.MessageCard
import kill.online.helper.ui.theme.floatingBallAlpha
import kill.online.helper.ui.window.ComposeFloatingWindow
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.backToHome
import kill.online.helper.utils.dragFloatingWindow
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    appNavController: NavHostController,
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedPage by rememberSaveable { mutableStateOf(Route.home.value) }

    val scaffoldNavController = rememberNavController()

    appViewModel.initPermissionRequestLauncher(
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    // 权限被授予
                    Toast.makeText(context, "权限被授予", Toast.LENGTH_SHORT).show()
                } else {
                    // 权限被拒绝
                    Toast.makeText(context, "权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            })
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = appTopBarTitle[appNavItem.indexOf(selectedPage)]) },
                actions = {

                    IconButton(onClick = {
                        appViewModel.isShowTips = !appViewModel.isShowTips
                    }) {
                        Icon(Icons.Filled.TipsAndUpdates, contentDescription = "tips")
                    }
                })
        },
        bottomBar = {
            BottomAppBar {
                appNavItem.forEachIndexed { index, item ->
                    NavigationBarItem(icon = {
                        when (index) {
                            0 -> Icon(Icons.Filled.Home, contentDescription = null)
                            1 -> Icon(Icons.Filled.Face, contentDescription = null)
                            2 -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                            3 -> Icon(Icons.Filled.Settings, contentDescription = null)
                        }
                    },
                        label = { Text(appBottomBarLabel[index]) },
                        selected = selectedPage == item,
                        onClick = {
                            if (selectedPage != item) {
                                selectedPage = item
                                scaffoldNavController.popBackStack()
                                //点击item时，清空栈内 popUpTo ID到栈顶之间的所有节点，避免站内节点持续增加
                                scaffoldNavController.navigate(item) {
                                    popUpTo(scaffoldNavController.graph.findStartDestination().id) {
                                        //跳转时保存页面状态
                                        saveState = true
                                    }
                                    //栈顶复用，避免重复点击同一个导航按钮，回退栈中多次创建实例
                                    launchSingleTop = true
                                    //回退时恢复页面状态
                                    restoreState = true
                                    //通过使用 saveState 和 restoreState 标志，当在底部导航项之间切换时，
                                    //系统会正确保存并恢复该项的状态和返回堆栈。
                                }
                            }
                        })
                }
            }
        },
        floatingActionButton = {
            val floatingBall =
                FloatingWindowFactory.getFloatingWindow("floatingBall", content = { fb ->
                    IconButton(
                        onClick = {
                            fb.hide()
                            FloatingWindowFactory.getFloatingWindow("floatingWindow").show()
                        }, modifier = Modifier
                            .dragFloatingWindow { windowOffset, dragArea ->
                                appViewModel.screenWidth = dragArea.x
                                appViewModel.screenHeight = dragArea.y
                            }
                            .background(
                                Color.Black.copy(alpha = floatingBallAlpha), shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }, config = { fb ->
                    fb.setCallback(
                        onShow = {
//                                showToast(context, "显示悬浮窗")
                            Log.i("onShow", "onShow callback")
                        },
                        onHide = {
//                                showToast(context, "隐藏悬浮窗")
                            Log.i("onHide", "onHide callback")
                        },
                        onLayout = { params ->
                            val view = fb.view
                            val f = Rect().also { view.getWindowVisibleDisplayFrame(it) }
                            params.x = (f.width() - view.width) / 2
                            params.y = (f.height() - view.height) / 2
                        }
                    )
                })

            val floatingWindow =
                FloatingWindowFactory.getFloatingWindow("floatingWindow",
                    content = { fw ->
                        FloatingWindowScreen(
                            fw, appViewModel = appViewModel, ztViewModel = ztViewModel
                        )
                    },
                    config = { fw ->
                        fw.setCallback(
                            onShow = {
//                                showToast(context, "显示悬浮窗")
                                Log.i("onShow", "onShow callback")
                            },
                            onHide = {
//                                showToast(context, "隐藏悬浮窗")
                                Log.i("onHide", "onHide callback")
                            },
                            onLayout = { params ->
                                val view = fw.view
                                val f = Rect().also { view.getWindowVisibleDisplayFrame(it) }
                                params.x = (f.width() - view.width) / 2
                                params.y = (f.height() - view.height) / 2
                                view.addOnUnhandledKeyEventListener { _, event ->
                                    Log.i("OnUnhandledKeyEvent", "$event")
                                    if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN ||
                                        event.keyCode == KeyEvent.KEYCODE_SOFT_LEFT ||
                                        event.keyCode == KeyEvent.KEYCODE_SOFT_RIGHT
                                    ) {
                                        // 在这里处理按下返回键的逻辑
                                        Log.i("OnUnhandledKeyEvent", "关闭悬浮窗")
                                        fw.hide()
                                        val fb =
                                            FloatingWindowFactory.getFloatingWindow("floatingBall")
                                        fb.show()
                                        return@addOnUnhandledKeyEventListener true
                                    }
                                    Log.i("OnUnhandledKeyEvent", "不处理")
                                    return@addOnUnhandledKeyEventListener false
                                }
                            }
                        )
                        fw.updateLayoutParams { params ->
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        }
                    }
                )

            when (selectedPage) {
                Route.home.value -> {
                    FloatingActionButton(onClick = {
                        if (!floatingBall.isShowing() && !floatingWindow.isShowing()) {
                            floatingBall.show()
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }

                Route.rule.value -> {
                    FloatingActionButton(onClick = {
                        appViewModel.isAddRule.value = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }
            }


        },
        floatingActionButtonPosition = FabPosition.End,
    ) { it ->
        val topAppBarPadding = it.calculateTopPadding()
        val bottomAppBarPadding = it.calculateBottomPadding()

        val scope = rememberCoroutineScope()
        appViewModel.bulletWindowManager = remember {
            BulletWindowManager(
                appViewModel,
                ztViewModel,
                ztViewModel.appSetting.value.fwRoomSetting.maxBulletMessage
            )
        }
        appViewModel.bulletWindowManager.start(scope)

        FloatingWindowFactory.getFloatingWindow("sysToast",
            content = {
                LaunchedEffect(Unit) {
                    delay(3000)
                    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                    sysToast.hide()
                }
                Box(
                    modifier = Modifier

                        .background(Color.Black.copy(0.3f), shape = RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = appViewModel.sysToastText,
                        color = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }

            },
            config = { fw ->
                fw.setCallback(
                    onLayout = { params ->
                        val view = fw.view
                        val f = Rect().also { view.getWindowVisibleDisplayFrame(it) }
                        params.x = (f.width() - view.width) / 2
                        params.y = (f.height() * 0.8).toInt()
                    }
                )
            }
        )


        BackHandler {
//            showToast(context, "app screen back pressed!!!")
            if (appNavController.currentDestination?.route != appNavController.graph.startDestinationRoute) {
                appNavController.popBackStack()
            } else {
                backToHome(context)
            }
        }
        Column(
            modifier = Modifier
                .padding(top = topAppBarPadding, bottom = bottomAppBarPadding)
                .fillMaxSize()
//                .background(Color.Cyan)
        ) {
            ScaffoldNavigation(appNavController, scaffoldNavController)
        }
    }
}

@SuppressLint("NewApi")
class BulletWindowManager(
    val appViewModel: AppViewModel,
    val ztViewModel: ZeroTierViewModel
) {
    private val TAG = "BulletWindowManager"
    private val bulletWindows = mutableListOf<ComposeFloatingWindow>()
    private val messages = mutableListOf<Message>()
    private val windowFreeStates = mutableListOf<Boolean>()

    @RequiresApi(Build.VERSION_CODES.P)
    constructor(
        appViewModel: AppViewModel,
        ztViewModel: ZeroTierViewModel,
        windowNumber: Int
    ) : this(appViewModel, ztViewModel) {
        repeat(windowNumber) {
            appViewModel.bulletMessages.add(Message())
        }
        repeat(windowNumber) { index ->
            val bulletWindow = FloatingWindowFactory.getFloatingWindow("bulletWindow_$index",
                content = {
                    MessageCard(
                        appViewModel = appViewModel,
                        msg = appViewModel.bulletMessages[index]
//                        msg = Message("qqq", "edwew")
                    )
                },
                config = { fw ->
                    fw.setCallback(
                        onShow = {
//                                showToast(context, "显示悬浮窗")
                            Log.i(TAG, "显示悬浮窗")
                        },
                        onHide = {
//                                showToast(context, "隐藏悬浮窗")
                            Log.i(TAG, "隐藏悬浮窗")
                        }
                    )
                    fw.updateLayoutParams { params ->
                        params.flags = params.flags or FLAG_NOT_TOUCHABLE
                    }
                }
            )
            bulletWindows.add(bulletWindow)
            windowFreeStates.add(true)
        }
    }

    fun playBulletMessage(msg: Message) {
        synchronized(messages) {
            messages.add(msg)
        }
    }


    fun start(scope: CoroutineScope) {
        scope.launch {
            if (ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage) {
                while (true) {
                    delay(100)
                    if (messages.isNotEmpty()) {
                        windowFreeStates.indexOfFirst { it }.let { index ->
                            if (index != -1) {
                                //有空闲窗口，取出一个消息，进行播放
                                val msg: Message
                                synchronized(messages) {
                                    msg = messages.removeAt(0)
                                }
                                Log.i(TAG, "play bullet msg: $msg")

                                appViewModel.bulletMessages[index] = msg
                                scope.launch {
                                    synchronized(windowFreeStates) {
                                        windowFreeStates[index] = false
                                    }
                                    // 重新设置窗口位置
                                    bulletWindows[index].updateLayoutParams { params ->
                                        val view = bulletWindows[index].view
                                        val f =
                                            Rect().also { view.getWindowVisibleDisplayFrame(it) }
                                        params.x = f.width()
                                        params.y = Random.nextInt(f.height() / 4)
                                    }
                                    bulletWindows[index].show()
                                    while (getWindowX(index) > 0) {
                                        delay(10)
                                        moveWindow(index, 5)
                                    }
                                    bulletWindows[index].hide()
                                    synchronized(windowFreeStates) {
                                        windowFreeStates[index] = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveWindow(windowIndex: Int, step: Int) {
        bulletWindows[windowIndex].updateLayoutParams {
            it.x -= step
        }
    }

    private fun getWindowX(windowIndex: Int): Int {
        return bulletWindows[windowIndex].windowParams.x
    }
}