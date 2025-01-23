package kill.online.helper.ui.window

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable

object FloatingWindowFactory {
    private lateinit var applicationContext: Context

    private val floatingWindowList = mutableMapOf<String, ComposeFloatingWindow>()

    @RequiresApi(Build.VERSION_CODES.P)
    fun getFloatingWindow(
        title: String,
        content: @Composable ((fw: ComposeFloatingWindow) -> Unit) = @Composable {},
        config: (composeFloatingWindow: ComposeFloatingWindow) -> Unit = {}
    ): ComposeFloatingWindow {

        return floatingWindowList[title] ?: ComposeFloatingWindow(applicationContext, title)
            .setContent(content).also {
                floatingWindowList[title] = it
                config(it)
            }
    }


    fun removeFloatingWindow(tag: String): Boolean {

        return if (floatingWindowList.containsKey(tag)) {
            floatingWindowList.minus(tag)
            true
        } else {
            false
        }
    }

    fun has(title: String): Boolean {
        return floatingWindowList.containsKey(title)
    }

    fun setApplicationContext(applicationContext: Context) {
        this.applicationContext = applicationContext
    }
}

