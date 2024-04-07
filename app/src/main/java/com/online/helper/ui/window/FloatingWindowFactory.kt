package com.online.helper.ui.window

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalContext

object FloatingWindowFactory {
    private lateinit var applicationContext: Context

    private val floatingWindowList = mutableMapOf<String, ComposeFloatingWindow>()
    fun getFloatingWindow(tag: String, content: @Composable () -> Unit = {}): ComposeFloatingWindow {

        return floatingWindowList[tag] ?: ComposeFloatingWindow(applicationContext)
            .setTag(tag)
            .setContent(content).also {
                floatingWindowList[tag] = it
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

