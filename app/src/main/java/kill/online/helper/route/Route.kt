package kill.online.helper.route

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kill.online.helper.ui.page.AppScreen
import kill.online.helper.ui.page.DeveloperScreen
import kill.online.helper.ui.page.FloatingWindowMemberContent
import kill.online.helper.ui.page.FloatingWindowMessageContent
import kill.online.helper.ui.page.FloatingWindowSettingContent
import kill.online.helper.ui.page.HelpScreen
import kill.online.helper.ui.page.HomeContent
import kill.online.helper.ui.page.MoonSettingScreen
import kill.online.helper.ui.page.OpenSourceScreen
import kill.online.helper.ui.page.PeerContent
import kill.online.helper.ui.page.RuleContent
import kill.online.helper.ui.page.SettingsContent
import kill.online.helper.ui.page.StickerManageScreen
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel

sealed class Route(val value: String) {
    data object app : Route("app")
    data object home : Route("home")
    data object player : Route("player")
    data object rule : Route("rule")
    data object setting : Route("setting")
    data object moonSetting : Route("setting/moonSetting")
    data object stickerManage : Route("setting/stickerManage")
    data object developer : Route("setting/developer")
    data object help : Route("setting/help")
    data object openSource : Route("setting/openSource")
    data object messageFW : Route("messageFW")
    data object memberFW : Route("memberFW")
    data object settingFW : Route("settingFW")
}

val appNavItem = listOf(Route.home.value, Route.player.value, Route.rule.value, Route.setting.value)
val appTopBarTitle = listOf("大厅", "玩家列表", "房间规则", "设置")
val appBottomBarLabel = listOf("大厅", "玩家", "规则", "设置")
val floatingWindowNavItem =
    listOf(Route.messageFW.value, Route.memberFW.value, Route.settingFW.value)
val floatingWindowTopBarTitle = listOf("房间聊天", "房间成员", "设置")


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation(
    appNavController: NavHostController,
    sharedText: String? = null,
    sharedUri: Uri? = null
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    NavHost(
        navController = appNavController,
        startDestination = if (sharedText == null && sharedUri == null) Route.app.value else Route.stickerManage.value,
        popEnterTransition = {
            slideInHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        },
    ) {
        composable(Route.app.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                AppScreen(appNavController)
            }
        }
        composable(Route.moonSetting.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                MoonSettingScreen(appNavController)
            }
        }
        composable(Route.stickerManage.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                StickerManageScreen(
                    appNavController,
                    sharedText = sharedText,
                    sharedUri = sharedUri
                )
            }
        }
        composable(Route.developer.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                DeveloperScreen(appNavController)
            }
        }
        composable(Route.help.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                HelpScreen(appNavController)
            }
        }
        composable(Route.openSource.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                OpenSourceScreen(appNavController)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ScaffoldNavigation(
    appNavController: NavHostController, scaffoldNavController: NavHostController
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    val roomListState = rememberLazyListState()
    val ruleListState = rememberLazyListState()
    val settingListState = rememberLazyListState()
    NavHost(
        navController = scaffoldNavController,
        startDestination = Route.home.value,
        popEnterTransition = {
            slideInHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        }
    ) {

        composable(Route.home.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                HomeContent(appNavController, scaffoldNavController, roomListState)
            }
        }
        composable(Route.player.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                PeerContent(scaffoldNavController)
            }
        }
        composable(Route.rule.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                RuleContent(appNavController, scaffoldNavController, ruleListState)
            }
        }
        composable(Route.setting.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                SettingsContent(appNavController, scaffoldNavController, settingListState)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FWdNavigation(
    floatingWindowNavController: NavHostController,
    appViewModel: AppViewModel,
    ztViewModel: ZeroTierViewModel
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    NavHost(
        navController = floatingWindowNavController,
        startDestination = Route.messageFW.value,
        popEnterTransition = {
            slideInHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(tween(durationMillis = 300, easing = FastOutLinearInEasing))
        }
    ) {
        composable(Route.messageFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowMessageContent(appViewModel = appViewModel, ztViewModel = ztViewModel)
            }
        }
        composable(Route.memberFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowMemberContent(appViewModel = appViewModel, ztViewModel = ztViewModel)
            }
        }
        composable(Route.settingFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowSettingContent(appViewModel = appViewModel, ztViewModel = ztViewModel)
            }
        }
    }
}