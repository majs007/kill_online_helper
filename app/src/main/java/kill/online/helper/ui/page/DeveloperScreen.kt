package kill.online.helper.ui.page

import android.icu.text.DateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kill.online.helper.BuildConfig
import kill.online.helper.R
import kill.online.helper.ui.components.DrawImage
import kill.online.helper.ui.components.NavBackButton

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(appNavController: NavController) {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    val date = dateFormat.format(BuildConfig.BUILD_TIME)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "关于") },
                navigationIcon = {
                    NavBackButton(appNavController)
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Column(modifier = Modifier.padding(start = 30.dp)) {
                    DrawImage(
                        name = "app_logo",
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 30.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8F),
                            letterSpacing = 1.3.sp,
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .width(50.dp),
                            thickness = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    SmallTitle { "作者" }
                    DrawImage(
                        name = "author_avatar",
                        contentDescription = "Author Avatar",
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clip(CircleShape),
                    )
                    TextContent { "majs007" }
                    SmallTitle { "版本" }
                    TextContent { "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})" }
                    SmallTitle { "构建时间" }
                    TextContent { date }
                    SmallTitle { "开源地址" }
                    UrlContent { "https://github.com/majs007/kill_online_helper" }
                    SmallTitle { "捐赠（爱发电）" }
                    UrlContent { "https://afdian.com/a/msk007" }
                    Spacer(modifier = Modifier.padding(top = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun SmallTitle(label: @Composable () -> String) {
    Text(
        text = label(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(0.6F),
        modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
    )
}

@Composable
private fun UrlContent(
    fontFamily: FontFamily = FontFamily.SansSerif,
    url: @Composable () -> String
) {
    val uriHandler = LocalUriHandler.current
    val target = url()
    Text(
        text = target,
        style = MaterialTheme.typography.titleMedium,
        color = Color.Blue.copy(0.9F),
        fontFamily = fontFamily,
        modifier = Modifier.clickable { uriHandler.openUri(target) }
    )
}

@Composable
private fun TextContent(
    fontFamily: FontFamily = FontFamily.SansSerif,
    onClick: () -> Unit = {},
    content: @Composable () -> String
) {
    Text(
        text = content(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(0.9F),
        fontFamily = fontFamily,
        modifier = Modifier.clickable { onClick() }
    )
}