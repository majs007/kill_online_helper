package kill.online.helper.ui.page

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kill.online.helper.data.Room
import kill.online.helper.ui.theme.FloatingWindowPadding
import kill.online.helper.ui.theme.chipPadding
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.StateUtils.add
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingWindowMemberContent(
    appViewModel: AppViewModel = AppViewModel(),
    ztViewModel: ZeroTierViewModel = ZeroTierViewModel()
) {
    val popUpMenuItem = listOf("踢出", "拉黑")
    var expanded by remember { mutableStateOf(false) }
    var clickedPlayer by remember { mutableStateOf(Room.Member()) }
    FlowRow(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = FloatingWindowPadding, end = FloatingWindowPadding)
    ) {
        ztViewModel.enteredRoom.value.players.forEachIndexed { index, s ->
            AssistChip(
                onClick = { },
                label = { Text(s.name) },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = Color.White
                ),
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = Color.White,
                    leadingIconContentColor = Color.Cyan
                ),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "Localized description",
//                                Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                modifier = Modifier
                    .padding(start = chipPadding, end = chipPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                expanded = true
                            })
                    }
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(popUpMenuItem[0]) },
                onClick = {
                    //TODO 发一条退出房间的消息
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(popUpMenuItem[1]) },
                onClick = {
                    add(FileUtils.ItemName.Blacklist, ztViewModel.blacklist, clickedPlayer)
                }
            )
        }
    }
}
