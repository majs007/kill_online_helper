package com.online.helper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.online.helper.ui.theme.FloatingWindowPadding
import com.online.helper.ui.theme.chipPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingWindowMemberContent() {
    val players = listOf("章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA")
    FlowRow(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = FloatingWindowPadding, end = FloatingWindowPadding)
    ) {
        players.forEachIndexed { index, s ->
            AssistChip(
                onClick = { },
                label = { Text(s) },
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
            )
        }
    }
}
