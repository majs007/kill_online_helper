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
        tag: String,
        content: @Composable ((fw: ComposeFloatingWindow) -> Unit) = @Composable {},
        onBackHandle: () -> Unit = {},
        config: (composeFloatingWindow: ComposeFloatingWindow) -> Unit = {}
    ): ComposeFloatingWindow {

        return floatingWindowList[tag] ?: ComposeFloatingWindow(applicationContext)
            .setTag(tag)
            .setContent(content).also {
                floatingWindowList[tag] = it
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

    fun has(tag: String): Boolean {
        return floatingWindowList.containsKey(tag)
    }

    fun setApplicationContext(applicationContext: Context) {
        this.applicationContext = applicationContext
    }
}

