package kill.online.helper.ui.page


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.ui.components.AssetLottie
import kill.online.helper.ui.components.BasicItemContainer
import kill.online.helper.utils.NetworkUtils
import kill.online.helper.viewModel.AppViewModel
import kill.online.helper.viewModel.ZeroTierViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeerContent(
    navController: NavHostController,
    appViewModel: AppViewModel = viewModel()
) {
    val TAG = "PeerContent"
    val context = LocalContext.current
    val ztViewModel: ZeroTierViewModel = viewModel()
    val currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val peerLatency = remember { mutableStateMapOf<String, Int>() }
    val scope = rememberCoroutineScope()

    val md by remember(0) {
        mutableStateOf(context.assets.open("md/peerContentTips.md").use { input ->
            input.bufferedReader().use { reader ->
                reader.readText()
            }
        })
    }


    LaunchedEffect(Unit) {
        while (true) {
            if (ztViewModel.isZTRunning)
                ztViewModel.getPeers()
            else ztViewModel.peers.clear()
            delay(1000)
            val scopes = mutableListOf<Deferred<Int?>>()
            ztViewModel.peers.forEachIndexed { _, peer ->
                val deferred = scope.async(Dispatchers.IO) {
                    peer.paths.firstOrNull { it.isPreferred }?.address?.let {
                        val newLatency = NetworkUtils.pingTest(it.hostString)
                        peerLatency[it.hostString] = newLatency
                        Log.i(TAG, "host:${it.hostString} latency: $newLatency")
                    }
                }
                scopes.add(deferred)
            }
            scopes.awaitAll()
        }
    }

    if (ztViewModel.peers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AssetLottie(
                name = "emptyFace.lottie",
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .align(Alignment.Center)
            )
        }
    }

    LazyColumn {
        itemsIndexed(ztViewModel.peers, { index, _ -> index }) { index, peer ->
            var milliseconds = -1L
            peer.paths.firstOrNull { it.isPreferred }?.let {
                milliseconds = currentTime - it.lastReceive
            }
            var hostString = ""
            var displayHost = ""
            peer.paths.firstOrNull { it.isPreferred }?.address?.let {
                hostString = it.hostString
                val host = it.hostString.toCharArray()
                var charNum = 4
                if (hostString.contains('.')) {
                    for (i in 0 until it.hostString.length) {
                        if (charNum == 0) break
                        if (it.hostString[i] != '.') {
                            host[i] = '*'
                            charNum--
                        }
                    }
                } else {
                    charNum = 6
                    for (i in 2 until it.hostString.length) {
                        if (charNum == 0) break
                        if (it.hostString[i] != ':') {
                            host[i] = '*'
                            charNum--
                        }
                    }
                }
                displayHost = String(host)
            }
            val latency = peerLatency[hostString] ?: -2
            val role = peer.role.toString().split('_').last().trim()
            when {
                milliseconds == -1L -> {
                    BasicItemContainer(icon = "😵",
                        text = { displayHost },
                        subText = { "状态：未知  延迟：${latency}ms  角色：${role}" })
                }

                role == "PLANET" -> {
                    BasicItemContainer(icon = "🌏",
                        text = { displayHost },
                        subText = { "状态：在线  延迟：${latency}ms  角色：${role}" })
                }

                role == "MOON" -> {
                    BasicItemContainer(icon = "🌝",
                        text = { displayHost },
                        subText = { "状态：在线  延迟：${latency}ms  角色：${role}" })
                }

                role == "LEAF" -> {
                    BasicItemContainer(icon = "☘️",
                        text = { displayHost },
                        subText = { "状态：在线  延迟：${latency}ms  角色：${role}" })
                }
            }
        }
    }

    if (appViewModel.isShowTips)
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Filled.TipsAndUpdates,
                    contentDescription = null
                )
            },
            title = { Text(text = "宝宝巴适") },
            onDismissRequest = {
                appViewModel.isShowTips = false
            },
            confirmButton = {},
            text = {
                RichText {
                    Markdown(md)
                }
            }
        )
}
