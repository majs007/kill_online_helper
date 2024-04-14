package com.online.helper.ui.window

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.core.view.doOnLayout
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch

class ComposeFloatingWindow(
    private val context: Context,

    ) : SavedStateRegistryOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory by lazy {
        SavedStateViewModelFactory(
            context.applicationContext as Application,
            this@ComposeFloatingWindow,
            null
        )
    }

    override val defaultViewModelCreationExtras: CreationExtras = MutableCreationExtras().apply {
        val application = context.applicationContext?.takeIf { it is Application }
        if (application != null) {
            set(
                ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY,
                application as Application
            )
        }
        set(SAVED_STATE_REGISTRY_OWNER_KEY, this@ComposeFloatingWindow)
        set(VIEW_MODEL_STORE_OWNER_KEY, this@ComposeFloatingWindow)
    }

    override val viewModelStore: ViewModelStore = ViewModelStore()

    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private var savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    //悬浮窗显示状态
    private var showing = false

    //画布
    var decorView: ViewGroup = FrameLayout(context)

    //窗口管理器对象
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    //内容大小
    var contentWidth = 0
    var contentHeight = 0


    //窗口布局参数初始化
    val windowParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {

        height = WindowManager.LayoutParams.WRAP_CONTENT
        width = WindowManager.LayoutParams.WRAP_CONTENT
        format = PixelFormat.TRANSLUCENT
        gravity = Gravity.START or Gravity.TOP
        windowAnimations = android.R.style.Animation_Dialog
        flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        if (context !is Activity) {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
        }
    }
    var updateLayoutParams: (it: WindowManager.LayoutParams) -> Unit = {}

    private var tag: String? = null
    private var onShow: (() -> Unit) = {}
    private var onHide: (() -> Unit) = {}
    private var onBackHandle: () -> Unit = {}


    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        enableSavedStateHandles()
    }


    fun setTag(tag: String): ComposeFloatingWindow {
        this.tag = tag
        return this
    }

    fun getTag(): String? {
        return tag
    }

    //设置悬浮窗组合式UI
    @RequiresApi(Build.VERSION_CODES.P)
    fun setContent(
        content: @Composable (composeFloatingWindow: ComposeFloatingWindow) -> Unit,

        ): ComposeFloatingWindow {
        setContentView(ComposeView(context).also {
            it.setContent {
                CompositionLocalProvider(
                    LocalFloatingWindow provides this@ComposeFloatingWindow
                ) {
                    content(this)
                }
            }
            it.setViewTreeLifecycleOwner(this@ComposeFloatingWindow)
            it.setViewTreeViewModelStoreOwner(this@ComposeFloatingWindow)
            it.setViewTreeSavedStateRegistryOwner(this@ComposeFloatingWindow)
            decorView.visibility = View.INVISIBLE
            it.doOnLayout { view ->
                this.contentWidth = view.width
                this.contentHeight = view.height
                Log.i("doOnLayout", "width:${view.width},height:${view.height}")
                windowParams.let(updateLayoutParams)
                decorView.visibility = View.VISIBLE
                update()
                it.addOnUnhandledKeyEventListener { _, event ->
                    if (event.keyCode == KeyEvent.KEYCODE_BACK && event.keyCode == KeyEvent.ACTION_DOWN ||
                        event.action == KeyEvent.KEYCODE_SOFT_LEFT || event.action == KeyEvent.KEYCODE_SOFT_RIGHT
                    ) {
                        // 在这里处理按下返回键的逻辑
                        onBackHandle()
                        return@addOnUnhandledKeyEventListener true
                    } else {
                        // 如果不处理该按键事件，返回 false，以便继续传递给其他监听器处理
                        return@addOnUnhandledKeyEventListener false
                    }
                }
            }
        })
        return this
    }

    //设置悬浮窗状态回调
    fun setCallback(onShow: () -> Unit, onHide: () -> Unit): ComposeFloatingWindow {
        this.onShow = onShow
        this.onHide = onHide
        return this
    }

    fun setOnBackHandle(onBackHandle: () -> Unit): ComposeFloatingWindow {
        this.onBackHandle = onBackHandle
        return this
    }

    //设置窗口布局参数
    fun setLayoutParams(update: (it: WindowManager.LayoutParams) -> Unit): ComposeFloatingWindow {
        updateLayoutParams = update
        return this
    }

    private fun setContentView(view: View) {
        if (decorView.childCount > 0) {
            decorView.removeAllViews()
        }
        decorView.addView(view)
        update()
    }

    fun isShowing(): Boolean {
        return showing
    }

    fun show() {
        if (checkOverlayPermission(context)) {
            //拥有权限
            require(decorView.childCount != 0) {
                "Content view cannot be empty"
            }
            if (showing) {
                update()
                return
            }
            decorView.getChildAt(0)?.takeIf { it is ComposeView }?.let { composeView ->
                val reComposer = Recomposer(AndroidUiDispatcher.CurrentThread)
                composeView.compositionContext = reComposer
                lifecycleScope.launch(AndroidUiDispatcher.CurrentThread) {
                    reComposer.runRecomposeAndApplyChanges()
                }
            }
            if (decorView.parent != null) {
                windowManager.removeViewImmediate(decorView)
            }
            windowManager.addView(decorView, windowParams)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            showing = true
        } else {
            //没有权限，申请系统悬浮窗权限
            try {
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(overlayIntent)
            } catch (e: Exception) {
                // 捕获异常并打印异常信息
                Log.e("ComposeFloatingWindow.show", "${e.message}\n")
            }
        }
        onShow()
    }

    fun update() {
        if (!showing) return
        windowManager.updateViewLayout(decorView, windowParams)
    }

    fun hide() {
        if (!showing) {
            return
        }
        showing = false
        windowManager.removeViewImmediate(decorView)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        onHide()
    }


    private fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}