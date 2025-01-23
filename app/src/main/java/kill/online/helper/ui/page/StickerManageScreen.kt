package kill.online.helper.ui.page

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.data.AppSettingItem
import kill.online.helper.data.Sticker
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.components.ExternalImage
import kill.online.helper.utils.FileUtils
import kill.online.helper.utils.FileUtils.deleteFile
import kill.online.helper.utils.FileUtils.writeBytesToFile
import kill.online.helper.utils.showToast
import kill.online.helper.utils.toMD5
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.delay
import java.io.File

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StickerManageScreen(
    appNavController: NavController,
    appViewModel: AppViewModel = viewModel(),
    ztViewModel: ZeroTierViewModel = viewModel(),
    sharedText: String? = null,
    sharedUri: Uri? = null
) {
    val TAG = "StickerManage"
    val context = LocalContext.current
    var isShowAlertDialog by remember { mutableStateOf(false) }

    var label by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }

    val localStickerManage = ztViewModel.appSetting.value.stickerManage
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("QQ", "咖波", "自定义")

    var isShowTips by remember { mutableStateOf(false) }

    val lazyOnlineSticker = remember { mutableStateListOf<Sticker>() }

    val md by remember(0) {
        mutableStateOf(context.assets.open("md/stickerManagementTips.md").use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        })
    }

    LaunchedEffect(Unit) {
//        ztViewModel.initZTConfig(context)
//        ztViewModel.loadZTConfig()

        sharedText?.let {
            selectedTabIndex = tabTitles.size - 1
            input = it
            label = "输入表情url"
            isShowAlertDialog = true
        }

        /*    sharedUri?.let {
                selectedTabIndex = tabTitles.size - 1
                input = it.path.toString().substringAfterLast("/")
                label = "导入表情"
                isShowAlertDialog = true
            }*/
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = AppSettingItem.MANAGE_STICKER) }, actions = {
                IconButton(onClick = {
                    isShowTips = !isShowTips
                }) {
                    Icon(Icons.Filled.TipsAndUpdates, contentDescription = "tips")
                }
            })
        },
        floatingActionButton = {
            if (selectedTabIndex == 2) FloatingActionButton(onClick = {
                label = "输入表情url"
                input = ""
                isShowAlertDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { it ->
        val topAppBarPadding = it.calculateTopPadding()
        val bottomAppBarPadding = it.calculateBottomPadding()

        LazyColumn(
            modifier = Modifier
                .padding(top = topAppBarPadding)
                .fillMaxWidth()
        ) {
            item(0) {
                // 表情Tab
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        edgePadding = 5.dp,  // 左右边距
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 动态生成多个 Tab
                        for (i in tabTitles.indices) {
                            Tab(selected = selectedTabIndex == i,
                                onClick = { selectedTabIndex = i },
                                text = { Text(tabTitles[i]) })
                        }
                    }
                }
            }
            item(1) {
                // 表情包
                FlowRow(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 根据选中的 Tab 显示不同内容
                    when (selectedTabIndex) {

                        0 -> {
                            //QQ sticker
                            val qqStickers = ztViewModel.appSetting.value.stickerManage.filter {
                                it.name.startsWith("qq_")
                            }
                            val qqStickerChunk = qqStickers.chunked(10)
                            val lazyQQStickers = remember { mutableStateListOf<Sticker>() }
                            LaunchedEffect(Unit) {
                                qqStickerChunk.forEachIndexed { index, chunk ->
                                    lazyQQStickers.addAll(chunk)
                                    delay(1000)
                                }
                            }
                            lazyQQStickers.forEachIndexed { _, sticker ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(0.2f)
                                ) {
//                                    DrawImage(sticker.name, "sticker", Modifier.fillMaxSize())
                                    AssetLottie(
                                        name = sticker.name,
                                        directory = "sticker",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    val isEnable =
                                        localStickerManage.find { it.name == sticker.name }?.enable
                                            ?: false
                                    var isCheck by remember { mutableStateOf(isEnable) }
                                    Icon(if (isCheck) Icons.Default.CheckCircleOutline else Icons.Outlined.Circle,
                                        contentDescription = "check",
                                        tint = if (isCheck) Color.Green else Color.Black,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = {
                                                    if (isCheck) {
                                                        isCheck = false
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.let {
                                                                it.copy(stickerManage = it.stickerManage.map { s ->
                                                                    if (s.name == sticker.name) {
                                                                        s.copy(enable = false)
                                                                    } else {
                                                                        s
                                                                    }
                                                                })
                                                            }
                                                    } else {
                                                        isCheck = true
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.let {
                                                                it.copy(stickerManage = it.stickerManage.map { s ->
                                                                    if (s.name == sticker.name) {
                                                                        s.copy(enable = true)
                                                                    } else {
                                                                        s
                                                                    }
                                                                })
                                                            }
                                                    }
                                                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                                })
                                            })
                                }
                            }
                        }

                        1 -> {
                            // capoo sticker
                            val capooStickers = ztViewModel.appSetting.value.stickerManage.filter {
                                it.name.startsWith("capoo_")
                            }
                            val capooStickerChunk = capooStickers.chunked(10)
                            val lazyCapooStickers = remember { mutableStateListOf<Sticker>() }
                            LaunchedEffect(Unit) {
                                capooStickerChunk.forEachIndexed { index, chunk ->
                                    lazyCapooStickers.addAll(chunk)
                                    delay(1000)
                                }
                            }
                            lazyCapooStickers.forEachIndexed { _, sticker ->
                                Box(modifier = Modifier.fillMaxWidth(0.2f)) {
//                                    DrawImage(sticker.name, "sticker", Modifier.fillMaxSize())
                                    AssetLottie(
                                        name = sticker.name,
                                        directory = "sticker",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    val isEnable =
                                        localStickerManage.find { it.name == sticker.name }?.enable
                                            ?: false
                                    var isCheck by remember {
                                        mutableStateOf(
                                            isEnable
                                        )
                                    }
                                    Icon(if (isCheck) Icons.Default.CheckCircleOutline else Icons.Outlined.Circle,
                                        contentDescription = "check",
                                        tint = if (isCheck) Color.Green else Color.Black,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = {
                                                    if (isCheck) {
                                                        isCheck = false
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.let {
                                                                it.copy(stickerManage = it.stickerManage.map { s ->
                                                                    if (s.name == sticker.name) {
                                                                        s.copy(enable = false)
                                                                    } else {
                                                                        s
                                                                    }
                                                                })
                                                            }

                                                    } else {
                                                        isCheck = true
                                                        ztViewModel.appSetting.value =
                                                            ztViewModel.appSetting.value.let {
                                                                it.copy(stickerManage = it.stickerManage.map { s ->
                                                                    if (s.name == sticker.name) {
                                                                        s.copy(enable = true)
                                                                    } else {
                                                                        s
                                                                    }
                                                                })
                                                            }
                                                    }
                                                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                                })
                                            })
                                }
                            }
                        }

                        2 -> {

                            val onlineSticker =
                                ztViewModel.appSetting.value.stickerManage.filter { it.type == Sticker.StickerType.ONLINE }
                            Log.i(TAG, "StickerManageScreen: ${ztViewModel.appSetting.value}")
                            Log.i(TAG, "StickerManageScreen: $onlineSticker")
                            if (onlineSticker.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = topAppBarPadding * 3)
                                        .fillMaxSize()
                                ) {
                                    AssetLottie(
                                        name = "emptyWhale.lottie",
                                        modifier = Modifier
                                            .fillMaxSize(0.8f)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                            onlineSticker.forEachIndexed { _, sticker ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(0.2f)
                                ) {
                                    val dir = context.getExternalFilesDir("online_sticker")
                                    val stickerFile = File(dir, sticker.name.toMD5())
                                    val isExist = stickerFile.exists()
                                    val isEnable =
                                        localStickerManage.find { it.name == sticker.name }?.enable
                                            ?: false
                                    var isCheck by remember { mutableStateOf(isEnable) }
                                    var isSupport by remember { mutableStateOf(true) }

                                    try {
                                        val imgUrl = Uri.fromFile(stickerFile)
                                        val source = ImageDecoder.createSource(
                                            context.contentResolver,
                                            imgUrl
                                        )
                                        val drawable = ImageDecoder.decodeDrawable(source)
                                    } catch (e: Exception) {
                                        isSupport = false

                                    }

                                    if (isExist && isSupport) {
                                        ExternalImage(
                                            sticker.name.toMD5(),
                                            "online_sticker",
                                            "sticker",
                                            Modifier.fillMaxSize()
                                        )

                                    } else {
                                        Text(
                                            text = "格式不支持",
                                            modifier = Modifier.align(Alignment.TopCenter)
                                        )

                                    }
                                    // 删除按钮
                                    IconButton(
                                        onClick = {
                                            deleteFile(context, "online_sticker", sticker.name)
                                            ztViewModel.appSetting.value =
                                                ztViewModel.appSetting.value.let {
                                                    it.copy(stickerManage = it.stickerManage.filter { s ->
                                                        s.name != sticker.name
                                                    })
                                                }
                                            lazyOnlineSticker.remove(sticker)
                                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                        }, modifier = Modifier.align(Alignment.BottomStart)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "delete",
                                            tint = Color.Red,
                                        )
                                    }
                                    // 启用/禁用 按钮
                                    IconButton(
                                        onClick = {
                                            if (isCheck) {
                                                isCheck = false
                                                ztViewModel.appSetting.value =
                                                    ztViewModel.appSetting.value.let {
                                                        it.copy(stickerManage = it.stickerManage.map { s ->
                                                            if (s.name == sticker.name) {
                                                                s.copy(enable = false)
                                                            } else {
                                                                s
                                                            }
                                                        })
                                                    }
                                            } else {
                                                isCheck = true
                                                ztViewModel.appSetting.value =
                                                    ztViewModel.appSetting.value.let {
                                                        it.copy(stickerManage = it.stickerManage.map { s ->
                                                            if (s.name == sticker.name) {
                                                                s.copy(enable = true)
                                                            } else {
                                                                s
                                                            }
                                                        })
                                                    }
                                            }
                                            ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)
                                        },
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            if (isCheck) Icons.Default.CheckCircleOutline else Icons.Outlined.Circle,
                                            contentDescription = "check",
                                            tint = if (isCheck) Color.Green else Color.Black,
                                        )
                                    }

                                }
                            }
                        }

                        else -> Text("Content for Tab ${selectedTabIndex + 1}")
                    }
                }
            }
        }

        if (isShowAlertDialog) AlertDialog(icon = {
            Icon(
                imageVector = Icons.Filled.Edit, contentDescription = null
            )
        },
            title = { Text(text = label) },
            onDismissRequest = { isShowAlertDialog = false },
            confirmButton = {
                ElevatedButton(onClick = {
                    //  网络导入
                    //下载图片，更新AppSetting.stickerManage
                    if (input.isEmpty()) {
                        showToast(context, "url不能为空")
                        isShowAlertDialog = false
                        return@ElevatedButton
                    }
                    if (!input.startsWith("http")) {
                        showToast(context, "非法url")
                        isShowAlertDialog = false
                        return@ElevatedButton
                    }
                    if (input.contains("\n")) {
                        val inputList = input.split("\n")
                        inputList.forEach { url ->
                            if (url.startsWith("http")) {

                                appViewModel.download(url, onSuccess = { body ->
                                    val dir = context.getExternalFilesDir("online_sticker")
                                    val stickerFile = File(dir, url.toMD5())
                                    if (stickerFile.exists()) {
                                        showToast(context, "表情已存在")
                                        isShowAlertDialog = false
                                        return@download
                                    }
                                    writeBytesToFile(stickerFile, body.bytes())
                                    ztViewModel.appSetting.value =
                                        ztViewModel.appSetting.value.let {
                                            it.copy(
                                                stickerManage = it.stickerManage.toMutableList()
                                                    .apply {
                                                        add(
                                                            Sticker(
                                                                url,
                                                                Sticker.StickerType.ONLINE,
                                                                0
                                                            )
                                                        )
                                                    }.toList()
                                            )
                                        }
                                    ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)

                                }, onFailure = {
                                    showToast(context, "下载失败，${it.message}")
                                    isShowAlertDialog = false
                                })
                            }
                        }
                        isShowAlertDialog = false
                    } else {
                        if (input.startsWith("http")) {
                            appViewModel.download(input, onSuccess = { body ->

                                val dir = context.getExternalFilesDir("online_sticker")
                                val stickerFile = File(dir, input.toMD5())
                                if (stickerFile.exists()) {
                                    showToast(context, "表情已存在")
                                    isShowAlertDialog = false
                                    return@download
                                }
                                writeBytesToFile(stickerFile, body.bytes())
                                ztViewModel.appSetting.value = ztViewModel.appSetting.value.let {
                                    it.copy(
                                        stickerManage = it.stickerManage.toMutableList().apply {
                                            add(
                                                Sticker(
                                                    input, Sticker.StickerType.ONLINE, 0
                                                )
                                            )
                                        }.toList()
                                    )
                                }
                                ztViewModel.saveZTConfig(FileUtils.ItemName.AppSetting)

                                isShowAlertDialog = false
                            }, onFailure = {
                                showToast(context, "下载失败，${it.message}")
                                isShowAlertDialog = false
                            })
                        }
                    }
                }) {
                    Text(text = "确定")
                }
            },
            text = {
                OutlinedTextField(value = input,
                    label = { Text(text = label) },
                    maxLines = 10,
                    onValueChange = { newValue -> input = newValue })
            })
        if (isShowTips) AlertDialog(icon = {
            Icon(
                imageVector = Icons.Filled.TipsAndUpdates, contentDescription = null
            )
        }, title = { Text(text = "宝宝巴适") }, onDismissRequest = {
            isShowTips = false
        }, confirmButton = {}, text = {
            RichText {
                Markdown(md)
            }
        })
    }
}