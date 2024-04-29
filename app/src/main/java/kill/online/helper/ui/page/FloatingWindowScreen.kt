package kill.online.helper.ui.page

import android.graphics.Rect
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import kill.online.helper.route.FWdNavigation
import kill.online.helper.route.Route
import kill.online.helper.route.floatingWindowNavItem
import kill.online.helper.route.floatingWindowTopBarTitle
import kill.online.helper.ui.theme.emojiSize
import kill.online.helper.ui.theme.floatingWindowBackgroundAlpha
import kill.online.helper.ui.theme.floatingWindowBarAlpha
import kill.online.helper.ui.theme.floatingWindowCorner
import kill.online.helper.ui.theme.floatingWindowHeight
import kill.online.helper.ui.theme.floatingWindowWidth
import kill.online.helper.ui.theme.topAppBarActionPadding
import kill.online.helper.ui.window.ComposeFloatingWindow
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.dragFloatingWindow
import kill.online.helper.utils.toPainter
import kill.online.helper.viewModel.AppViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FloatingWindowScreen(
    fw: ComposeFloatingWindow,
    onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner?,
    appViewModel: AppViewModel = viewModel(),
) {
    val floatingWindowNavController = rememberNavController()
    var selectedItem by remember { mutableStateOf(Route.messageFW.value) }
    val context = LocalContext.current

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
                    title = {
                        Text(
                            text = floatingWindowTopBarTitle[floatingWindowNavItem.indexOf(
                                selectedItem
                            )]
                        )
                    },
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
                    modifier = Modifier.dragFloatingWindow { windowOffset ->
                        appViewModel.toScreenOffset = windowOffset
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    contentColor = Color.White,
                    containerColor = Color.Black.copy(floatingWindowBarAlpha),
                    modifier = Modifier.dragFloatingWindow { windowOffset ->
                        appViewModel.toScreenOffset = windowOffset
                    }
                ) {
                    floatingWindowNavItem.forEachIndexed { index, item ->
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
                            selected = selectedItem == item,
                            onClick = {
                                if (selectedItem != item) {
                                    selectedItem = item
                                    floatingWindowNavController.popBackStack()
                                    //点击item时，清空栈内 popUpTo ID到栈顶之间的所有节点，避免站内节点持续增加
                                    floatingWindowNavController.navigate(item) {
                                        popUpTo(floatingWindowNavController.graph.findStartDestination().id) {
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
                            }
                        )
                    }
                }
            },
            modifier = Modifier.size(floatingWindowWidth, floatingWindowHeight)
        ) {
            val topAppBarPadding = it.calculateTopPadding()
            val bottomAppBarPadding = it.calculateBottomPadding()
            val floatingImg =
                FloatingWindowFactory.getFloatingWindow(
                    "floatingImg",
                    content = { fb ->
                        LaunchedEffect(appViewModel.receivedImg.timeStamp) {
                            delay(2000)
                            fb.hide()
                        }
                        val img = Base64.decode(
                            appViewModel.receivedImg.msg,
                            Base64.DEFAULT
                        )
                        Box(
                            modifier = Modifier
                                .size(emojiSize)
                                .clipToBounds()
                        ) {
                            Image(img.toPainter(), contentDescription = null)
                        }
                    },
                    config = { fb ->
                        fb.setCallback(
                            onShow = {
//                                showToast(context, "显示悬浮窗")
                                Log.i("onShow", "floatingImg")
                            },
                            onHide = {
//                                showToast(context, "隐藏悬浮窗")
                                Log.i("onHide", "floatingImg")

                            }
                        )
                            .setLayoutParams { params ->
                                val decorView = fb.decorView
                                val f = Rect().also { rect ->
                                    decorView.getWindowVisibleDisplayFrame(rect)
                                }
                                if (appViewModel.receivedImg.isDrag) {
                                    params.x =
                                        (f.width() * appViewModel.receivedImg.imagePositionRadio.x).toInt()
                                    params.y =
                                        (f.height() * appViewModel.receivedImg.imagePositionRadio.y).toInt()
                                } else {
                                    params.x = (f.width() - fb.contentWidth) / 2
                                    params.y = (f.height() - fb.contentHeight) / 2
                                }

                            }
                    })
            LaunchedEffect(key1 = appViewModel.receivedImg.timeStamp) { floatingImg.show() }
            Column(
                modifier = Modifier
                    .padding(top = topAppBarPadding, bottom = bottomAppBarPadding)
                    .fillMaxSize()
//                .background(Color.Cyan)
            ) {
                onBackPressedDispatcherOwner?.let { owner ->
                    CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides owner) {
                        FWdNavigation(floatingWindowNavController)
                    }
                }
            }
        }
    }

}