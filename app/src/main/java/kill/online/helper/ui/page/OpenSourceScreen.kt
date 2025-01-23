package kill.online.helper.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kill.online.helper.ui.components.NavBackButton

@Immutable
enum class OpenSourceReference(val label: String, val url: String) {
    ZeroTier("ZeroTierOne", "https://github.com/zerotier/ZeroTierOne"),
    ZeroTierFix("ZeroTierFix", "https://github.com/kaaass/ZerotierFix"),
    LottieAndroid("lottie-android", "https://github.com/airbnb/lottie-android/"),
    Retrofit("Retrofit", "https://github.com/square/retrofit"),
    OkHttp("OkHttp", "https://github.com/square/okhttp"),

    //    UltraSwipeRefresh("UltraSwipeRefreshLayout", "https://github.com/jenly1314/UltraSwipeRefresh"),
    ComposeRichText("compose-richtext", "https://github.com/halilozercan/compose-richtext"),
    Nanohttpd("nanoHttpd", "https://github.com/NanoHttpd/nanohttpd")
}

@Composable
private fun OpenSourceReferenceCard(name: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(url) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.SansSerif
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceScreen(appNavController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "开源许可") },
                navigationIcon = { NavBackButton(appNavController) }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            items(OpenSourceReference.entries.toTypedArray()) { reference ->
                OpenSourceReferenceCard(name = reference.label, url = reference.url)
            }
        }
    }
}