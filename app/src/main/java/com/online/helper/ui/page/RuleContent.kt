package com.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.online.helper.ui.components.SwitchItemContainer

@Composable
fun RuleContent(
    showRuleBottomSheet: MutableState<Boolean>
) {
    val roomRule = remember {
        listOf(
            "标准 | 素将局",
            "三英 | 素将局",
            "标准 | 阴间局",
            "武皇 | 素将局",
            "标准 | 素将局",
            "标准 | 素将局",
            "三英 | 素将局",
            "标准 | 阴间局",
            "武皇 | 素将局",
            "标准 | 素将局",
        )
    }
    var radioState by remember { mutableIntStateOf(-1) }
    LazyColumn {
        itemsIndexed(roomRule) { index, item ->
            SwitchItemContainer(
                checked = radioState == index,
                onCheckedChange = {
                    radioState = index
                    showRuleBottomSheet.value = true
                },
                icon = Icons.Filled.Delete,
                onIconClicked = {},
                iconEnabled = true,
                text = { item })
        }
    }
}