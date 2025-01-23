package kill.online.helper.ui.page

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.HeartBroken
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.BuildConfig
import kill.online.helper.data.API_GITHUB_RELEASES_LATEST
import kill.online.helper.data.AppSettingItem
import kill.online.helper.data.GITHUB_RELEASES_URL
import kill.online.helper.data.Release
import kill.online.helper.route.Route
import kill.online.helper.ui.components.BasicItemContainer
import kill.online.helper.ui.components.SwitchItemContainer
import kill.online.helper.ui.components.Title
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.showToast
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
    val TAG = "SettingsContent"
    val context = LocalContext.current
    var isShowAlertDialog by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    val md by remember(0) {
        mutableStateOf(context.assets.open("md/settingContentTips.md").use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        })
    }
    LazyColumn(
        state = settingListState,
        modifier = Modifier
    ) {
        item(AppSettingItem.ADVANCED_NETWORK_SETTING) {
            Title(text = { AppSettingItem.ADVANCED_NETWORK_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.useCellularData,
                onCheckedChange = {
                    val useCellularData = ztViewModel.appSetting.value.useCellularData
                    ztViewModel.appSetting.value =
                        ztViewModel.appSetting.value.copy(useCellularData = !useCellularData)
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.CellTower,
                iconEnabled = true,
                text = { AppSettingItem.USE_CELLULAR_DATA },
                subText = { "优先使用移动数据，否则请使用【分配公网地址】的WiFi" },
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.disableIpv6,
                onCheckedChange = {
                    val disableIpv6 = ztViewModel.appSetting.value.disableIpv6
                    ztViewModel.appSetting.value =
                        ztViewModel.appSetting.value.copy(disableIpv6 = !disableIpv6)
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.NotInterested,
                iconEnabled = true,
                text = { AppSettingItem.DISABLE_IPV6 },
                subText = { "不使用zero tier分配的ipv6地址" },
            )
            BasicItemContainer(
                icon = Icons.Default.Bedtime,
                text = { AppSettingItem.ZT_MOON_SETTING },
                subText = { "Moon为中继加速节点" },
                onClick = { appNavController.navigate(Route.moonSetting.value) },
            )
        }
        item(AppSettingItem.ROOM_SETTING) {
            Title(text = { AppSettingItem.ROOM_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.isCustomRoomName,
                onCheckedChange = {
                    val isCustomRoomName = ztViewModel.appSetting.value.roomSetting.isCustomRoomName
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        roomSetting = ztViewModel.appSetting.value.roomSetting.copy(isCustomRoomName = !isCustomRoomName)
                    )
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.DriveFileRenameOutline,
                iconEnabled = true,
                text = { AppSettingItem.CUSTOM_ROOM_NAME },
                subText = { "我的🏠还蛮大的" },
                onClick = {
                    label = AppSettingItem.CUSTOM_ROOM_NAME
                    input = ztViewModel.appSetting.value.roomSetting.roomName
                    isShowAlertDialog = true
                }
            )

            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.isPrivateRoom,
                onCheckedChange = {
                    val isPrivateRoom = ztViewModel.appSetting.value.roomSetting.isPrivateRoom
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        roomSetting = ztViewModel.appSetting.value.roomSetting.copy(isPrivateRoom = !isPrivateRoom)
                    )
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.Key,
                iconEnabled = true,
                text = { AppSettingItem.ROOM_PASSWORD },
                subText = { "猜猜是谁没收到邀请" },
                onClick = {
                    label = AppSettingItem.ROOM_PASSWORD
                    input = ztViewModel.appSetting.value.roomSetting.roomPassword
                    isShowAlertDialog = true
                }
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.roomSetting.enableBlackList,
                onCheckedChange = {
                    val enableBlackList = ztViewModel.appSetting.value.roomSetting.enableBlackList
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        roomSetting = ztViewModel.appSetting.value.roomSetting.copy(enableBlackList = !enableBlackList)
                    )
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Outlined.HeartBroken,
                iconEnabled = true,
                text = { AppSettingItem.BLACK_LIST },
                subText = { "你不准参加联趴" },
                onClick = {
                    label = AppSettingItem.BLACK_LIST
                    input = ztViewModel.appSetting.value.roomSetting.blackList.joinToString("\n")
                    isShowAlertDialog = true
                }
            )
            BasicItemContainer(
                icon = Icons.Default.Face,
                text = { AppSettingItem.PLAYER_NAME },
                subText = { "游戏中的昵称" },
                onClick = {
                    label = AppSettingItem.PLAYER_NAME
                    input = ztViewModel.appSetting.value.playerName
                    isShowAlertDialog = true
                },
            )
        }
        item(AppSettingItem.FW_ROOM_SETTING) {
            Title(text = { AppSettingItem.FW_ROOM_SETTING })
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.fwRoomSetting.autoPlayAudio,
                onCheckedChange = {
                    val autoPlayAudio = ztViewModel.appSetting.value.fwRoomSetting.autoPlayAudio
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        fwRoomSetting = ztViewModel.appSetting.value.fwRoomSetting.copy(
                            autoPlayAudio = !autoPlayAudio
                        )
                    )
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.PlayCircleOutline,
                iconEnabled = true,
                text = { AppSettingItem.AUTO_PLAY_AUDIO },
                subText = { "若关闭则需点击播放语音消息" },
            )
            SwitchItemContainer(
                checked = ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage,
                onCheckedChange = {
                    val bulletMessage =
                        ztViewModel.appSetting.value.fwRoomSetting.enableBulletMessage
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        fwRoomSetting = ztViewModel.appSetting.value.fwRoomSetting.copy(
                            enableBulletMessage = !bulletMessage
                        )
                    )
                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                },
                icon = Icons.Filled.ClearAll,
                iconEnabled = true,
                text = { AppSettingItem.BULLET_MESSAGE },
                subText = { "谁看消息还用悬浮窗😋" },
            )
            BasicItemContainer(
                icon = Icons.Default.Numbers,
                text = { AppSettingItem.MAX_BULLET_MESSAGE },
                subText = { "屏幕中不会超过${ztViewModel.appSetting.value.fwRoomSetting.maxBulletMessage}条弹幕" },
                onClick = {
                    label = AppSettingItem.MAX_BULLET_MESSAGE
                    input = ztViewModel.appSetting.value.fwRoomSetting.maxBulletMessage.toString()
                    isShowAlertDialog = true
                },
            )
            BasicItemContainer(
                icon = Icons.Outlined.EmojiEmotions,
                text = { AppSettingItem.MANAGE_STICKER },
                subText = { "我的图可太多了🤺" },
                onClick = { appNavController.navigate(Route.stickerManage.value) },
            )
        }
        item(AppSettingItem.OTHER) {
            Title(text = { AppSettingItem.OTHER })
            BasicItemContainer(
                icon = Icons.Filled.Code,
                text = { AppSettingItem.DEVELOPER },
                onClick = { appNavController.navigate(Route.developer.value) },
            )
            BasicItemContainer(
                icon = Icons.AutoMirrored.Outlined.Help,
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
                onClick = {

                    appViewModel.download(API_GITHUB_RELEASES_LATEST, { responseBody ->
                        try {
                            val jsonStr = responseBody.string()
                            val release = appViewModel.gson.fromJson(jsonStr, Release::class.java)
                            if (release.tag_name != BuildConfig.VERSION_NAME) {
                                label = AppSettingItem.CHECK_UPDATE
                                input = "检测到新版本${release.tag_name}，请前往GitHub下载"
                                isShowAlertDialog = true
                            } else {
                                showToast(context, "当前已是最新版本")
                            }
                        } catch (e: Exception) {
                            showToast(context, "数据异常")
                            Log.e(TAG, "download:\n${e.message}")
                        }
                    }, {
                        showToast(context, "检查更新失败")
                    })
                },
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
                        AppSettingItem.BLACK_LIST -> Icons.Outlined.Block
                        AppSettingItem.MAX_BULLET_MESSAGE -> Icons.Default.Numbers
                        AppSettingItem.CHECK_UPDATE -> Icons.Filled.Update
                        else -> Icons.Filled.Edit
                    }, contentDescription = null
                )
            },
            title = { Text(text = label) },
            onDismissRequest = {
                isShowAlertDialog = false
            },
            confirmButton = {
                ElevatedButton(onClick = {
                    when (label) {
                        AppSettingItem.CUSTOM_ROOM_NAME -> {
                            ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                                roomSetting = ztViewModel.appSetting.value.roomSetting.copy(roomName = input)
                            )
                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        }

                        AppSettingItem.ROOM_PASSWORD -> {
                            ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                                roomSetting = ztViewModel.appSetting.value.roomSetting.copy(
                                    roomPassword = input
                                )
                            )
                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        }

                        AppSettingItem.BLACK_LIST -> {
                            val blackList = input.split("\n").map { it.trim() }.toList()
                            ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                                roomSetting = ztViewModel.appSetting.value.roomSetting.copy(
                                    blackList = blackList
                                )
                            )
                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        }

                        AppSettingItem.PLAYER_NAME -> {
                            ztViewModel.appSetting.value =
                                ztViewModel.appSetting.value.copy(playerName = input)
                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        }

                        AppSettingItem.MAX_BULLET_MESSAGE -> {
                            ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                                fwRoomSetting = ztViewModel.appSetting.value.fwRoomSetting.copy(
                                    maxBulletMessage = input.toInt()
                                )
                            )
                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                        }

                        AppSettingItem.CHECK_UPDATE -> {

                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL))
                            // 判断是否有浏览器应用可以处理这个 Intent
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                // 如果没有浏览器可以打开链接，显示错误消息或提示
                                showToast(context, "未发现浏览器")
                            }
                        }
                    }
                    isShowAlertDialog = false
                }) {
                    Text(text = "确定")
                }
            },
            text = {
                OutlinedTextField(value = input,
                    label = { Text(text = label) },
                    maxLines = 10,
                    onValueChange = { newValue -> input = newValue })
            }
        )
    if (appViewModel.isShowTips)
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Filled.TipsAndUpdates,
                    contentDescription = null
                )
            },
            title = { Text(text = "宝宝巴适") },
            onDismissRequest = {
                appViewModel.isShowTips = false
            },
            confirmButton = {},
            text = {
                RichText {
                    Markdown(md)
                }
            }
        )

}



