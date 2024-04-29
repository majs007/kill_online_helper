package kill.online.helper.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kill.online.helper.ui.components.SwitchItemContainer
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.StateUtils.delete
import kill.online.helper.utils.StateUtils.load
import kill.online.helper.utils.StateUtils.update

import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,
    ruleListState: LazyListState = rememberLazyListState(),
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel()
) {

    val context = LocalContext.current
    var isRuleConfigShow by remember { mutableStateOf(false) }
    var clickedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(key1 = null) {
//        appViewModel.loadRoomRule(context)
        load(FileUtils.ItemName.RoomRule, ztViewModel.roomRule, listOf())
    }
    LazyColumn(
        state = ruleListState,
    ) {
        itemsIndexed(ztViewModel.roomRule.value) { index, item ->
            SwitchItemContainer(
                checked = ztViewModel.roomRule.value[index].checked,
                onCheckedChange = {
                    val lastCheckedIndex = ztViewModel.getCheckedRuleIndex()
                    //若存在上一次选中的规则，则将上次规则取消选中
                    if (lastCheckedIndex != -1)
                    /*       appViewModel.updateRoomRule(
                               appViewModel.getCheckedRuleIndex(),
                               context
                           ) {
                               it.copy(checked = false)
                           }*/
                        update(
                            FileUtils.ItemName.RoomRule,
                            ztViewModel.roomRule,
                            ztViewModel.getCheckedRuleIndex()
                        ) {
                            it.copy(checked = false)
                        }
                    //若上次选中规则与当前规则不同，则将当前规则选中
                    if (lastCheckedIndex != index) {
                        /*    appViewModel.updateRoomRule(index, context) {
                                it.copy(checked = true)
                            }*/
                        update(
                            FileUtils.ItemName.RoomRule,
                            ztViewModel.roomRule, index
                        ) {
                            it.copy(checked = true)
                        }
                    }
                },
                icon = Icons.Filled.Delete,
                onIconClicked = {
                    // 删除房间规则
//                    appViewModel.removeRoomRule(index, context)
                    delete(FileUtils.ItemName.RoomRule, ztViewModel.roomRule, index)

                },
                iconEnabled = true,
                text = { "${item.mode} | ${item.rule}" },
                onClick = {
                    clickedIndex = index
                    isRuleConfigShow = true
                }
            )
        }
    }

    RuleConfigSheet(
        isShow = isRuleConfigShow,
        onDismissRequest = {
            isRuleConfigShow = false
            appViewModel.isAddRule.value = false
        },
        clickedIndex = clickedIndex
    )

}