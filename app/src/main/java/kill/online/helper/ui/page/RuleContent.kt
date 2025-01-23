package kill.online.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.components.SwitchItemContainer
import kill.online.helper.utils.FileUtils
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

    val md by remember(0) {
        mutableStateOf(context.assets.open("md/ruleContentTips.md").use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        })
    }

    if (ztViewModel.roomRules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AssetLottie(
                name = "emptyCat.lottie",
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .align(Alignment.Center)
            )
        }
    }
    LazyColumn(
        state = ruleListState,
    ) {
        itemsIndexed(ztViewModel.roomRules, { index, _ -> index }) { index, item ->
            SwitchItemContainer(checked = ztViewModel.roomRules[index].checked, onCheckedChange = {
                val lastCheckedIndex = ztViewModel.roomRules.indexOfFirst { it.checked }
                //若存在上一次选中的规则，则将上次规则取消选中
                if (lastCheckedIndex != -1) ztViewModel.roomRules[index] =
                    ztViewModel.roomRules[index].copy(checked = false)
                //若上次选中规则与当前规则不同，则将当前规则选中
                if (lastCheckedIndex != index) {
                    ztViewModel.roomRules[index] = ztViewModel.roomRules[index].copy(checked = true)
                }
                ztViewModel.saveZTConfig(FileUtils.ItemName.RoomRules)
            }, icon = Icons.Filled.Delete, onIconClicked = {
                // 删除房间规则
                ztViewModel.roomRules.removeAt(index)
                ztViewModel.saveZTConfig(FileUtils.ItemName.RoomRules)
            }, iconEnabled = true, text = { "${item.mode} | ${item.rule}" }, onClick = {
                clickedIndex = index
                isRuleConfigShow = true
            })
        }
    }

    RuleConfigSheet(
        isShow = isRuleConfigShow, onDismissRequest = {
            isRuleConfigShow = false
            appViewModel.isAddRule.value = false
        }, clickedIndex = clickedIndex
    )
    if (appViewModel.isShowTips) AlertDialog(icon = {
        Icon(
            imageVector = Icons.Filled.TipsAndUpdates, contentDescription = null
        )
    }, title = { Text(text = "宝宝巴适") }, onDismissRequest = {
        appViewModel.isShowTips = false
    }, confirmButton = {}, text = {
        RichText {
            Markdown(md)
        }
    })

}