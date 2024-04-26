package kill.online.helper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kill.online.helper.ui.theme.appPadding
import kill.online.helper.ui.theme.doubleSpacePadding
import kill.online.helper.ui.theme.imePadding
import kill.online.helper.ui.theme.textPadding
import kill.online.helper.viewModel.AppViewModel


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RuleConfigSheet(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
    clickedIndex: Int,
    sheetState: SheetState = rememberModalBottomSheetState(),
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val gameMode = remember { listOf("标准", "三英", "武皇") }
    var checkedIndex by remember { mutableIntStateOf(0) }
    var rule by remember { mutableStateOf("") }
    if (isShow) {
        checkedIndex = gameMode.indexOf(appViewModel.roomRule.value[clickedIndex].mode)
        rule = appViewModel.roomRule.value[clickedIndex].rule
    } else {
        checkedIndex = 0
        rule = ""
    }

    if (isShow || appViewModel.isAddRule.value) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest, sheetState = sheetState, modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = appPadding, end = appPadding)
//                    .background(Color.Cyan)
            ) {

                Text(text = "游戏模式：${gameMode[checkedIndex]}")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
//                        .background(Color.Blue)
                ) {
                    gameMode.forEachIndexed { index, s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
//                                .background(Color.Red)
                        ) {
                            Text(text = s)
                            RadioButton(
                                selected = index == checkedIndex,
                                onClick = { checkedIndex = index },
                            )
                        }
                    }
                }

                Text(text = "房间描述：", modifier = Modifier.padding(bottom = textPadding))
                TextField(
                    value = rule,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    onValueChange = { newText -> rule = newText },
                    placeholder = { Text(text = "例：素将局，禁点将") },
                    modifier = Modifier.fillMaxWidth()
                )
                ElevatedButton(
                    onClick = {
                        if (!isShow) appViewModel.addRoomRule(gameMode[checkedIndex], rule, context)
                        else appViewModel.updateRoomRule(clickedIndex, context) {
                            it.copy(mode = gameMode[checkedIndex], rule = rule)
                        }
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .padding(top = doubleSpacePadding)
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "完成")
                }
            }
            Spacer(modifier = Modifier.height(imePadding))
        }
    }

}
