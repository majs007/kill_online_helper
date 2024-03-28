package com.online.helper.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.online.helper.ui.theme.chipPadding
import com.online.helper.ui.theme.octupleSpacePadding
import com.online.helper.ui.theme.playerPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerContent() {
    var players = remember {
        listOf(
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA",
            "章鱼哥", "派大星", "海绵宝宝", "小蜗", "蟹老板", "神秘奇男子AAA"
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .background(Color.Red)
            .padding(start = playerPadding, end = playerPadding)
    ) {
        LazyColumn {
            item {
                FlowRow(
                    modifier = Modifier
//                    .background(Color.Cyan)
                        .fillMaxWidth()
//                .fillMaxHeight(0.8f)
                ) {
                    players.forEachIndexed { index, s ->
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
                Spacer(modifier = Modifier.height(octupleSpacePadding))
            }


        }

    }


}