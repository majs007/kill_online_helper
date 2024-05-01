package kill.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kill.online.helper.data.AppSettingItem
import kill.online.helper.route.Route
import kill.online.helper.ui.components.BasicItemContainer
import kill.online.helper.ui.components.SwitchItemContainer
import kill.online.helper.ui.components.Title
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.StateUtils.update
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel


@Composable
fun SettingsContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,
    settingListState: LazyListState = rememberLazyListState(),
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {
    var isShowAlertDialog by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    LazyColumn(
        state = settingListState,
        modifier = Modifier
    ) {
        item {
            Title(text = { AppSettingItem.ADVANCED_NETWORK_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.useCellularData,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.useCellularData = !it.useCellularData
                        it.copy()
                    }
                },
                icon = Icons.Filled.CellTower,
                iconEnabled = true,
                text = { AppSettingItem.USE_CELLULAR_DATA },
                subText = { "优先使用移动数据，否则请使用【分配公网地址】的WiFi" },
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.disableIpv6,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.disableIpv6 = !it.disableIpv6
                        it.copy()
                    }
                },
                icon = Icons.Filled.NotInterested,
                iconEnabled = true,
                text = { AppSettingItem.DISABLE_IPV6 },
                subText = { "禁用Ipv6会降低连通性" },
            )
        }
        item {
            Title(text = { AppSettingItem.ROOM_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.customRoomName,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.roomSetting.customRoomName = !it.roomSetting.customRoomName
                        it.copy()
                    }
                },
                icon = Icons.Filled.DriveFileRenameOutline,
                iconEnabled = true,
                text = { AppSettingItem.CUSTOM_ROOM_NAME },
                subText = { ztViewModel.appSetting.value.roomSetting.roomName },
                onClick = {
                    label = AppSettingItem.CUSTOM_ROOM_NAME
                    input = ztViewModel.appSetting.value.roomSetting.roomName
                    isShowAlertDialog = true
                }
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.isPrivateRoom,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.roomSetting.isPrivateRoom = !it.roomSetting.isPrivateRoom
                        it.copy()
                    }
                },
                icon = Icons.Filled.Key,
                iconEnabled = true,
                text = { AppSettingItem.ROOM_PASSWORD },
                subText = { ztViewModel.appSetting.value.roomSetting.roomPassword },
                onClick = {
                    label = AppSettingItem.ROOM_PASSWORD
                    input = ztViewModel.appSetting.value.roomSetting.roomPassword
                    isShowAlertDialog = true
                }
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.enableBlackList,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.roomSetting.enableBlackList = !it.roomSetting.enableBlackList
                        it.copy()
                    }
                },
                icon = Icons.Outlined.HeartBroken,
                iconEnabled = true,
                text = { AppSettingItem.BLACK_LIST },
                subText = { "\"我们好像已经渐行渐远了\"" },
                onClick = {
                }
            )
        }
        item {
            Title(text = { AppSettingItem.FW_ROOM_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.fwRoomSetting.autoPlayAudio,
                onCheckedChange = {
                    update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                        it.fwRoomSetting.autoPlayAudio = !it.fwRoomSetting.autoPlayAudio
                        it.copy()
                    }
                },
                icon = Icons.Filled.PlayCircleOutline,
                iconEnabled = true,
                text = { AppSettingItem.AUTO_PLAY_AUDIO },
                subText = { "若关闭则需点击播放语音消息" },

                )
            BasicItemContainer(
                icon = Icons.Outlined.EmojiEmotions,
                text = { AppSettingItem.MANAGE_EMOJI },
                onClick = { /*TODO 表情包管理界面*/ },
            )
        }
        item {
            Title(text = { AppSettingItem.OTHER })
            BasicItemContainer(
                icon = Icons.Filled.Code,
                text = { AppSettingItem.DEVELOPER },
                onClick = { appNavController.navigate(Route.developer.value) },
            )
            BasicItemContainer(
                icon = Icons.Outlined.Help,
                text = { AppSettingItem.HELP },
                onClick = { appNavController.navigate(Route.help.value) },
            )
            BasicItemContainer(
                icon = Icons.Filled.WorkspacePremium,
                text = { AppSettingItem.OPEN_SOURCE },
                onClick = { appNavController.navigate(Route.openSource.value) },
            )
            BasicItemContainer(
                icon = Icons.Filled.Update,
                text = { AppSettingItem.CHECK_UPDATE },
                onClick = { /*TODO 检查更新界面*/ },
            )
        }
    }
    if (isShowAlertDialog)
        AlertDialog(
            icon = {
                Icon(
                    imageVector = when (label) {
                        AppSettingItem.CUSTOM_ROOM_NAME -> Icons.Filled.DriveFileRenameOutline
                        AppSettingItem.ROOM_PASSWORD -> Icons.Filled.Key
                        else -> Icons.Filled.Edit
                    }, contentDescription = null
                )

            },
            title = {
                Text(
                    text = when (label) {
                        AppSettingItem.CUSTOM_ROOM_NAME -> AppSettingItem.CUSTOM_ROOM_NAME
                        AppSettingItem.ROOM_PASSWORD -> AppSettingItem.ROOM_PASSWORD
                        else -> "输入"
                    }
                )

            },
            onDismissRequest = {
                isShowAlertDialog = false
                input = ""
            },
            confirmButton = {
                ElevatedButton(onClick = {
                    when (label) {
                        AppSettingItem.CUSTOM_ROOM_NAME -> {
                            update(FileUtils.ItemName.AppSetting, ztViewModel.appSetting) {
                                it.roomSetting.roomName = input
                                it.copy()
                            }
                        }
                    }
                    isShowAlertDialog = false
                    input = ""
                }) {
                    Text(text = "确定")
                }

            },
            text = {
                OutlinedTextField(value = input,
                    label = {
                        Text(
                            text =
                            when (label) {
                                AppSettingItem.CUSTOM_ROOM_NAME -> "房间名"
                                AppSettingItem.ROOM_PASSWORD -> "密码"
                                else -> ""
                            }
                        )
                    },
                    onValueChange = { newValue -> input = newValue })
            })

}



