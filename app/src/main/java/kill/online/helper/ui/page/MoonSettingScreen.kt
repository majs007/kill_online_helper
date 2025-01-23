package kill.online.helper.ui.page

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kill.online.helper.data.AppSettingItem
import kill.online.helper.ui.components.SwitchItemContainer
import kill.online.helper.utils.FileUtils
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kill.online.helper.zeroTier.model.Moon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonSettingScreen(
    appNavController: NavController,
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {
    var isShowAlertDialog by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var editingMoonIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = AppSettingItem.ZT_MOON_SETTING) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                label = AppSettingItem.MOON_WORLD_ID
                input = ""
                editingMoonIndex = -1
                isShowAlertDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        floatingActionButtonPosition = FabPosition.End,

        ) { it ->
        val topAppBarPadding = it.calculateTopPadding()
        val bottomAppBarPadding = it.calculateBottomPadding()

        LazyColumn(modifier = Modifier.padding(top = topAppBarPadding)) {
            itemsIndexed(ztViewModel.ztMoons, { index, _ -> index }) { index, moon ->
                SwitchItemContainer(
                    checked = moon.checked,
                    onCheckedChange = {
                        ztViewModel.ztMoons[index] = moon.copy(checked = !moon.checked)
                        ztViewModel.saveZTConfig(FileUtils.ItemName.ZTMoons)
                    },
                    icon = Icons.Filled.Delete,
                    onIconClicked = {
                        // 删除Moon
                        ztViewModel.ztMoons.removeAt(index)
                        ztViewModel.saveZTConfig(FileUtils.ItemName.ZTMoons)
                    },
                    iconEnabled = true,
                    text = { "ID：${moon.moonWorldId}" },
                    subText = { "状态：${if (moon.state == Moon.MoonState.ORBIT) "入轨" else "未入轨"}" },
                    onClick = {
                        label = AppSettingItem.MOON_WORLD_ID
                        input = moon.moonWorldId
                        editingMoonIndex = index
                        isShowAlertDialog = true
                    }
                )
            }
        }
        if (isShowAlertDialog)
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                },
                title = { Text(text = label) },
                onDismissRequest = {
                    isShowAlertDialog = false
                },
                confirmButton = {
                    ElevatedButton(onClick = {
                        val moon = Moon(moonWorldId = input, moonSeed = input)
                        if (editingMoonIndex != -1) {
                            ztViewModel.ztMoons[editingMoonIndex] = moon
                        } else
                            ztViewModel.ztMoons.add(moon)
                        ztViewModel.saveZTConfig(FileUtils.ItemName.ZTMoons)
                        isShowAlertDialog = false
                    }) {
                        Text(text = "确定")
                    }
                },
                text = {
                    OutlinedTextField(value = input,
                        label = { Text(text = label) },
                        maxLines = 1,
                        onValueChange = { newValue -> input = newValue })
                }
            )

    }
}