package kill.online.helper.ui.page

import android.os.Build
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import kill.online.helper.route.FWdNavigation
import kill.online.helper.route.Route
import kill.online.helper.route.floatingWindowNavItem
import kill.online.helper.route.floatingWindowTopBarTitle
import kill.online.helper.ui.theme.floatingWindowBackgroundAlpha
import kill.online.helper.ui.theme.floatingWindowBarAlpha
import kill.online.helper.ui.theme.floatingWindowCorner
import kill.online.helper.ui.theme.floatingWindowHeight
import kill.online.helper.ui.theme.floatingWindowWidth
import kill.online.helper.ui.theme.topAppBarActionPadding
import kill.online.helper.ui.window.ComposeFloatingWindow
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.ui.window.dragFloatingWindow

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowScreen(
    fw: ComposeFloatingWindow,
    onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner?
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
                    modifier = Modifier.dragFloatingWindow()
                )
            },
            bottomBar = {
                BottomAppBar(
                    contentColor = Color.White,
                    containerColor = Color.Black.copy(floatingWindowBarAlpha),
                    modifier = Modifier.dragFloatingWindow()
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