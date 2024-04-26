package kill.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kill.online.helper.data.Message
import kill.online.helper.ui.components.Title
import kill.online.helper.viewModel.AppViewModel

@Composable
fun SettingsContent(
    appNavController: NavHostController, scaffoldNavController: NavHostController,
    settingListState: LazyListState = rememberLazyListState(),
    appViewModel: AppViewModel = viewModel()
) {

    val settings = remember { listOf("高级网络设置", "悬浮球设置", "房间设置", "关于") }
    val settingsIcon = remember {
        listOf(
            Icons.Filled.Wifi, Icons.Filled.ChildCare,
            Icons.Outlined.Home, Icons.Outlined.Info
        )
    }

    LazyColumn(
        state = settingListState,
        modifier = Modifier
    ) {
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
        item {
            ElevatedButton(onClick = {
                appViewModel.sendMessage("172.22.21.212", Message("player1", "Hello"))
            }) {
                Text(text = "发送")
            }

        }
        items(appViewModel.messages.value) { message ->
            Text(text = message.msg)
        }
    }

}