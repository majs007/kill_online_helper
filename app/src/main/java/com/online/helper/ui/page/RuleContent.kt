package com.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.online.helper.ui.components.SwitchItemContainer
import com.online.helper.viewModel.GlobalVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,
    ruleListState: LazyListState = rememberLazyListState(),
    checkedIndex: Int,
    onCheckedChange: (checkedIndex: Int) -> Unit,
    globalVM: GlobalVM = viewModel()
) {

    var isRuleConfigShow by remember { mutableStateOf(false) }
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
    LazyColumn(
        state = ruleListState,
    ) {
        itemsIndexed(roomRule) { index, item ->
            SwitchItemContainer(
                checked = checkedIndex == index,
                onCheckedChange = {
                    isRuleConfigShow = true
                    onCheckedChange(index)
                },
                icon = Icons.Filled.Delete,
                onIconClicked = {},
                iconEnabled = true,
                text = { item })
        }
    }

    RuleConfigSheet(isShow = isRuleConfigShow, onDismissRequest = { isRuleConfigShow = false })

}