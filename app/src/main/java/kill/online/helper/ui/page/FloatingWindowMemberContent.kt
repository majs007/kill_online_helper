package kill.online.helper.ui.page

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MacroOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.data.Room
import kill.online.helper.ui.theme.FloatingWindowPadding
import kill.online.helper.ui.theme.chipPadding
import kill.online.helper.ui.window.FloatingWindowFactory
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.NetworkUtils
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FloatingWindowMemberContent(
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {
    val TAG = "FloatingWindowMemberContent"
    val context = LocalContext.current
    val myIp by remember { mutableStateOf(ztViewModel.getAssignedIP() ?: "") }
    val popUpMenuItem1 = listOf("拉黑", "禁言", "禁贴纸")
    val popUpMenuItem2 = listOf("取消拉黑", "取消禁言", "取消禁贴纸")

    var isExpanded by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf(Room.RoomMember()) }
    val playerLatency = remember { mutableStateMapOf<String, Int>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val scopes = mutableListOf<Deferred<Int?>>()
            ztViewModel.enteredRoom.value.players.forEachIndexed { _, player ->
                val deferred = scope.async(Dispatchers.IO) {
                    val newLatency = NetworkUtils.pingTest(player.ip)
                    playerLatency[player.ip] = newLatency
                    Log.i(TAG, "player:${player.name} host:${player.ip} latency: $newLatency")
                }
                scopes.add(deferred)
            }
            scopes.awaitAll()
        }
    }

    LazyColumn(
//        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = FloatingWindowPadding, end = FloatingWindowPadding)
    ) {
        itemsIndexed(ztViewModel.enteredRoom.value.players) { _, player ->
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                item(0) {
                    AssistChip(
                        onClick = {
                            appViewModel.sysToastText = "ip:${player.ip}"
                            val sysToast =
                                FloatingWindowFactory.getFloatingWindow("sysToast")
                            sysToast.show()
                            selectedPlayer = player
                            isExpanded = true
                        },
                        label = { Text(player.name) },
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.White
                        ),
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = if (player.ip == myIp) Color.Cyan else Color.White,
                            leadingIconContentColor = Color.Cyan
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Face,
                                contentDescription = "Localized description",
                            )
                        },
                        modifier = Modifier
                            .padding(start = chipPadding, end = chipPadding)
                    )
                    if (player.ip in ztViewModel.enteredRoom.value.banSendMsgList) {
                        Icon(
                            Icons.Filled.MacroOff,
                            contentDescription = "ban speak",
                            tint = Color.Green,
                        )
                    }
                    if (player.ip in ztViewModel.enteredRoom.value.banSendStickerList) {
                        Icon(
                            Icons.Filled.HideImage,
                            contentDescription = "ban sticker",
                            tint = Color.Green,
                        )
                    }
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = "ping value",
                        tint = Color.Green,
                    )
                    Text("${playerLatency[player.ip] ?: "-1"}ms")
                }
            }
        }
    }

    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { isExpanded = false }
    ) {
        DropdownMenuItem(
            enabled = myIp == ztViewModel.enteredRoom.value.roomOwnerIp,
            text = {
                if (selectedPlayer.ip in ztViewModel.appSetting.value.roomSetting.blackList) Text(
                    popUpMenuItem2[0]
                ) else Text(popUpMenuItem1[0])
            },
            onClick = {
                if (selectedPlayer.ip == myIp) {
                    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                    appViewModel.sysToastText = "是我拉黑了我自己？？？"
                    sysToast.show()
                    return@DropdownMenuItem
                }
                if (selectedPlayer.ip in ztViewModel.appSetting.value.roomSetting.blackList) {
                    val blackList =
                        ztViewModel.appSetting.value.roomSetting.blackList.toMutableList()
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        roomSetting = ztViewModel.appSetting.value.roomSetting.copy(
                            blackList = blackList.apply { remove(selectedPlayer.ip) }.toList()
                        )
                    )
                } else {
                    val blackList =
                        ztViewModel.appSetting.value.roomSetting.blackList.toMutableList()
                    ztViewModel.appSetting.value = ztViewModel.appSetting.value.copy(
                        roomSetting = ztViewModel.appSetting.value.roomSetting.copy(
                            blackList = blackList.apply { add(selectedPlayer.ip) }.toList()
                        )
                    )
                    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                    appViewModel.sysToastText = "燕子，以后联趴里没有我了，你要幸福"
                    sysToast.show()
                }
                ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
            }
        )
        DropdownMenuItem(
            enabled = myIp == ztViewModel.enteredRoom.value.roomOwnerIp,
            text = {
                if (selectedPlayer.ip in ztViewModel.enteredRoom.value.banSendMsgList) Text(
                    popUpMenuItem2[1]
                ) else Text(popUpMenuItem1[1])
            },
            onClick = {
                if (selectedPlayer.ip == myIp) {
                    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                    appViewModel.sysToastText = "是我禁言了我自己？？？"
                    sysToast.show()
                    return@DropdownMenuItem
                }
                if (selectedPlayer.ip in ztViewModel.enteredRoom.value.banSendMsgList) {
                    val index = ztViewModel.rooms.indexOfFirst { it.roomOwnerIp == myIp }
                    if (index != -1) {
                        val room = ztViewModel.rooms[index]
                        val banSendMsgList = room.banSendMsgList.toMutableList()
                        ztViewModel.rooms[index] = room.copy(
                            banSendMsgList = banSendMsgList.apply { remove(selectedPlayer.ip) }
                                .toList()
                        )
                    }
                    val banSendMsgList =
                        ztViewModel.enteredRoom.value.banSendMsgList.toMutableList()
                    ztViewModel.enteredRoom.value = ztViewModel.enteredRoom.value.copy(
                        banSendMsgList = banSendMsgList.apply { remove(selectedPlayer.ip) }.toList()
                    )
                } else {
                    val index = ztViewModel.rooms.indexOfFirst { it.roomOwnerIp == myIp }
                    if (index != -1) {
                        val room = ztViewModel.rooms[index]
                        val banSendMsgList = room.banSendMsgList.toMutableList()
                        ztViewModel.rooms[index] = room.copy(
                            banSendMsgList = banSendMsgList.apply { add(selectedPlayer.ip) }
                                .toList()
                        )
                    }
                    val banSendMsgList =
                        ztViewModel.enteredRoom.value.banSendMsgList.toMutableList()
                    ztViewModel.enteredRoom.value = ztViewModel.enteredRoom.value.copy(
                        banSendMsgList = banSendMsgList.apply { add(selectedPlayer.ip) }.toList()
                    )
                }


            }
        )
        DropdownMenuItem(
            enabled = myIp == ztViewModel.enteredRoom.value.roomOwnerIp,
            text = {
                if (selectedPlayer.ip in ztViewModel.enteredRoom.value.banSendStickerList) Text(
                    popUpMenuItem2[2]
                ) else Text(popUpMenuItem1[2])
            },
            onClick = {
                if (selectedPlayer.ip == myIp) {
                    val sysToast = FloatingWindowFactory.getFloatingWindow("sysToast")
                    appViewModel.sysToastText = "是我禁贴图了我自己？？？"
                    sysToast.show()
                    return@DropdownMenuItem
                }
                if (selectedPlayer.ip in ztViewModel.enteredRoom.value.banSendStickerList) {
                    val index = ztViewModel.rooms.indexOfFirst { it.roomOwnerIp == myIp }
                    if (index != -1) {
                        val room = ztViewModel.rooms[index]
                        val banSendStickerList = room.banSendStickerList.toMutableList()
                        ztViewModel.rooms[index] = room.copy(
                            banSendStickerList = banSendStickerList.apply { remove(selectedPlayer.ip) }
                                .toList()
                        )
                    }
                    val banSendStickerList =
                        ztViewModel.enteredRoom.value.banSendStickerList.toMutableList()
                    ztViewModel.enteredRoom.value = ztViewModel.enteredRoom.value.copy(
                        banSendStickerList = banSendStickerList.apply { remove(selectedPlayer.ip) }
                            .toList()
                    )
                } else {
                    val index = ztViewModel.rooms.indexOfFirst { it.roomOwnerIp == myIp }
                    if (index != -1) {
                        val room = ztViewModel.rooms[index]
                        val banSendStickerList = room.banSendStickerList.toMutableList()
                        ztViewModel.rooms[index] = room.copy(
                            banSendStickerList = banSendStickerList.apply { add(selectedPlayer.ip) }
                                .toList()
                        )
                    }
                    val banSendStickerList =
                        ztViewModel.enteredRoom.value.banSendStickerList.toMutableList()
                    ztViewModel.enteredRoom.value = ztViewModel.enteredRoom.value.copy(
                        banSendStickerList = banSendStickerList.apply { add(selectedPlayer.ip) }
                            .toList()
                    )
                }

            }
        )
    }
}
