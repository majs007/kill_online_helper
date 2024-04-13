package com.online.helper.ui.page

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.online.helper.route.Route
import com.online.helper.route.ScaffoldNavigation
import com.online.helper.route.appBottomBarLabel
import com.online.helper.route.appNavItem
import com.online.helper.route.appTopBarTitle
import com.online.helper.ui.theme.floatingBallAlpha
import com.online.helper.ui.window.FloatingWindowFactory
import com.online.helper.ui.window.dragFloatingWindow
import com.online.helper.utils.showToast
import com.online.helper.utils.simulateHome


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppScreen(appNavController: NavHostController) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    var selectedItem by rememberSaveable { mutableStateOf(Route.home.value) }

    val scaffoldNavController = rememberNavController()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = appTopBarTitle[appNavItem.indexOf(selectedItem)]) }) },
        bottomBar = {
            BottomAppBar {
                appNavItem.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.Home, contentDescription = null)
                                1 -> Icon(Icons.Filled.Face, contentDescription = null)
                                2 -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                3 -> Icon(Icons.Filled.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(appBottomBarLabel[index]) },
                        selected = selectedItem == item,
                        onClick = {
                            selectedItem = item
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
                    )
                }
            }
        },
        floatingActionButton = {

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
                        }.setOnBackHandle {

                            Log.i("setOnBackHandle Ball", "back pressed!!!")
                            if (appNavController.currentDestination?.route != appNavController.graph.startDestinationRoute) {
                                appNavController.popBackStack()
                            } else {
                                simulateHome(context)
                            }
                        }
                    })

            val floatingWindow =
                FloatingWindowFactory.getFloatingWindow(
                    "floatingWindow",
                    content = { fw -> FloatingWindowScreen(fw,onBackPressedDispatcherOwner) },
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
                        }.setOnBackHandle {

                            Log.i("setOnBackHandle Window", "back pressed!!!")
                            if (appNavController.currentDestination?.route != appNavController.graph.startDestinationRoute) {
                                appNavController.popBackStack()
                            } else {
                                simulateHome(context)
                            }
                        }
                    })

            when (selectedItem) {
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
            ScaffoldNavigation(appNavController, scaffoldNavController)
        }
    }
}