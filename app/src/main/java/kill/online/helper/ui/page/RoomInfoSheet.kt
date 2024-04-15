package kill.online.helper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.ui.theme.appPadding
import kill.online.helper.ui.theme.chipPadding
import kill.online.helper.ui.theme.quadrupleSpacePadding
import kill.online.helper.ui.theme.textLineHeight
import kill.online.helper.viewModel.GlobalVM


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomInfoSheet(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val globalVM: GlobalVM = viewModel()
    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            // Sheet content
            Box(
                modifier = Modifier.fillMaxWidth()
//                    .background(Color.Cyan)
            ) {
                Text(text = "房间A", modifier = Modifier.align(Alignment.Center))
            }
            Column(modifier = Modifier.padding(start = appPadding, end = appPadding)) {
                Text(text = "房主：章鱼哥", lineHeight = textLineHeight)
                Text(text = "游戏模式：标准", lineHeight = textLineHeight)
                Text(text = "房间人数：5-8", lineHeight = textLineHeight)
                Text(text = "房间描述：素将局", lineHeight = textLineHeight)
                Text(text = "房间成员：", lineHeight = textLineHeight)
            }
            FlowRow(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = appPadding, end = appPadding)
            ) {
                globalVM.players.forEachIndexed { index, s ->
                    AssistChip(
                        onClick = { },
                        label = { Text(s) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Face,
                                contentDescription = "Localized description",
//                                Modifier.size(AssistChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier
                            .padding(start = chipPadding, end = chipPadding)
                    )
                }
            }
            Spacer(modifier = Modifier.height(quadrupleSpacePadding))
        }
    }

}

