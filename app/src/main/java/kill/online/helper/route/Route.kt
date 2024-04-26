package kill.online.helper.route

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kill.online.helper.ui.page.AboutScreen
import kill.online.helper.ui.page.AppScreen
import kill.online.helper.ui.page.FloatingWindowMemberContent
import kill.online.helper.ui.page.FloatingWindowMessageContent
import kill.online.helper.ui.page.FloatingWindowSettingContent
import kill.online.helper.ui.page.HomeContent
import kill.online.helper.ui.page.PlayerContent
import kill.online.helper.ui.page.RuleContent
import kill.online.helper.ui.page.SettingsContent

sealed class Route(val value: String) {
    data object app : Route("app")
    data object home : Route("home")
    data object player : Route("player")
    data object rule : Route("rule")
    data object setting : Route("setting")
    data object about : Route("setting/about")
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


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun Navigation(appNavController: NavHostController) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    NavHost(
        navController = appNavController,
        startDestination = Route.app.value,
        popEnterTransition = {
            expandHorizontally(
                animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                clip = false
            )
        },
        popExitTransition = {
            shrinkHorizontally(
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
                clip = false
            )
        },
    ) {
        composable(Route.app.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                AppScreen(appNavController)
            }
        }
        composable(Route.about.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                AboutScreen(appNavController)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScaffoldNavigation(
    appNavController: NavHostController, scaffoldNavController: NavHostController
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    val roomListState = rememberLazyListState()
    val playerListState = rememberLazyListState()
    val ruleListState = rememberLazyListState()
    val settingListState = rememberLazyListState()
    NavHost(
        navController = scaffoldNavController,
        startDestination = Route.home.value,
        popEnterTransition = {
            expandHorizontally(
                animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                clip = false
            )
        },
        popExitTransition = {
            shrinkHorizontally(
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
                clip = false
            )
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
                PlayerContent(scaffoldNavController, playerListState)
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


@Composable
fun FWdNavigation(
    floatingWindowNavController: NavHostController
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    NavHost(
        navController = floatingWindowNavController,
        startDestination = Route.messageFW.value,
        popEnterTransition = {
            expandHorizontally(
                animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                clip = false
            )
        },
        popExitTransition = {
            shrinkHorizontally(
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
                clip = false
            )
        }
    ) {
        composable(Route.messageFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowMessageContent()
            }
        }
        composable(Route.memberFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowMemberContent()
            }
        }
        composable(Route.settingFW.value) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                FloatingWindowSettingContent()
            }
        }
    }
}