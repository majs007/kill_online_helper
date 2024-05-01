package kill.online.helper.ui.page

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kill.online.helper.ui.components.NavBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(appNavController: NavController) {
    val context = LocalContext.current
    val md = """
        # Q&A
        ## 1. ****
        *********
        ## 2. ****
        *********
        # 技巧
        ## 1. ****
        *********
        ## 2. ****
        *********
    """.trimIndent()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮助") },
                navigationIcon = { NavBackButton(appNavController) }
            )
        }
    ) {

        LazyColumn(modifier = Modifier.padding(top = it.calculateTopPadding())) {
            item {
                RichText(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Markdown(md)
                }
            }

        }
    }


}