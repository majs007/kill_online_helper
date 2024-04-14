package com.online.helper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.online.helper.ui.theme.appPadding
import com.online.helper.ui.theme.quadrupleSpacePadding
import com.online.helper.ui.theme.textPadding


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleConfigSheet(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val gameMode = mutableListOf(
        "标准",
        "三英",
        "武皇",
    )
    var selectedID by remember { mutableIntStateOf(0) }
    var roomDescriptionText by remember { mutableStateOf("") }


    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            // Sheet content

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = appPadding, end = appPadding)
//                    .background(Color.Cyan)
            ) {

                Text(text = "游戏模式：${gameMode[selectedID]}")
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
//                        .background(Color.Blue)
                ) {
                    gameMode.forEachIndexed { index, s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
//                                .background(Color.Red)
                        ) {
                            Text(text = s)
                            RadioButton(
                                selected = selectedID == index,
                                onClick = { selectedID = index },
                            )
                        }

                    }
                }

                Text(text = "房间描述：", modifier = Modifier.padding(bottom = textPadding))
                TextField(
                    value = roomDescriptionText,
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    onValueChange = { newText -> roomDescriptionText = newText },
                    placeholder = { Text(text = "素将局，禁点将") },
                )
                ElevatedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .padding(top = textPadding)
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "完成")
                }
            }
            Spacer(modifier = Modifier.height(quadrupleSpacePadding))
        }
    }

}
