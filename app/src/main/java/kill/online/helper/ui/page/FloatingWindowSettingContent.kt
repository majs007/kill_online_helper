package kill.online.helper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kill.online.helper.data.AppSettingItem
import kill.online.helper.ui.theme.FloatingWindowPadding
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel


@Composable
fun FloatingWindowSettingContent(
    appViewModel: AppViewModel,
    ztViewModel: ZeroTierViewModel
) {
    LazyColumn(
        modifier = Modifier
            .padding(start = FloatingWindowPadding, end = FloatingWindowPadding)
            .fillMaxWidth()
    ) {
        val isRoomOwner = ztViewModel.enteredRoom.value.roomOwnerIp == ztViewModel.getAssignedIP()
        item {
            FWSwitcher(Icons.Default.MicOff,
                AppSettingItem.BAN_ALL_MESSAGE,
                enabled = isRoomOwner,
                checked = ztViewModel.enteredRoom.value.banAllMessage,
                onCheckedChange = {
                    val banAllMessage = ztViewModel.enteredRoom.value.banAllMessage
                    ztViewModel.enteredRoom.value =
                        ztViewModel.enteredRoom.value.copy(banAllMessage = !banAllMessage)
                })
        }
        item {
            FWSwitcher(Icons.Filled.HideImage,
                AppSettingItem.BAN_ALL_STICKER,
                enabled = isRoomOwner,
                checked = ztViewModel.enteredRoom.value.banAllSticker,
                onCheckedChange = {
                    val banAllSticker = ztViewModel.enteredRoom.value.banAllSticker
                    ztViewModel.enteredRoom.value =
                        ztViewModel.enteredRoom.value.copy(banAllSticker = !banAllSticker)
                })
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FWSwitcher(
    imageVector: ImageVector,
    text: String,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    FlowRow(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
//            .background(Color.Red.copy(alpha = messageAlpha)),
    ) {
        Row(
            modifier = Modifier
//                .background(Color.Green.copy(alpha = messageAlpha))
                .align(Alignment.CenterVertically)
        ) {
            Icon(imageVector, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(text)
        }
        Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
//                .background(Color.Blue.copy(alpha = messageAlpha))
                .align(Alignment.CenterVertically)
        )
    }

}