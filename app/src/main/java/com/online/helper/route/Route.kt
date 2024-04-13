package com.online.helper.route

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.online.helper.ui.page.AboutScreen
import com.online.helper.ui.page.AppScreen
import com.online.helper.ui.page.FloatingWindowMemberContent
import com.online.helper.ui.page.FloatingWindowMessageContent
import com.online.helper.ui.page.FloatingWindowSettingContent
import com.online.helper.ui.page.HomeContent
import com.online.helper.ui.page.PlayerContent
import com.online.helper.ui.page.RoomInfoSheet
import com.online.helper.ui.page.RuleConfigSheet
import com.online.helper.ui.page.RuleContent
import com.online.helper.ui.page.SettingsContent
import com.online.helper.ui.window.ComposeFloatingWindow

sealed class Route(val value: String) {
    data object app : Route("app")
    data object home : Route("home")
    data object roomInfo : Route("home/roomInfo")
    data object player : Route("player")
    data object rule : Route("rule")
    data object ruleConfig : Route("rule/ruleConfig")
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(appNavController: NavHostController) {

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
            AppScreen(appNavController)
        }
        composable(Route.roomInfo.value) {
            RoomInfoSheet(appNavController)
        }
        composable(Route.ruleConfig.value) {
            RuleConfigSheet(appNavController)
        }
        composable(Route.about.value) {
            AboutScreen(appNavController)
        }
    }
}

@Composable
fun ScaffoldNavigation(
    appNavController: NavHostController, scaffoldNavController: NavHostController
) {

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
            HomeContent(appNavController, scaffoldNavController)
        }
        composable(Route.player.value) {
            PlayerContent(scaffoldNavController)
        }
        composable(Route.rule.value) {
            RuleContent(scaffoldNavController)
        }
        composable(Route.setting.value) {
            SettingsContent(appNavController, scaffoldNavController)
        }
    }
}


@Composable
fun FWdNavigation(
    floatingWindowNavController: NavHostController
) {
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
            FloatingWindowMessageContent()
        }
        composable(Route.memberFW.value) {
            FloatingWindowMemberContent()
        }
        composable(Route.settingFW.value) {
            FloatingWindowSettingContent()
        }
    }
}