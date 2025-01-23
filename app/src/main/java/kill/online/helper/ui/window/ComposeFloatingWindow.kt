package kill.online.helper.ui.window

import android.annotation.SuppressLint
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
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
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
import kill.online.helper.ui.theme.killTheme
import kotlinx.coroutines.launch

class ComposeFloatingWindow(
    private val context: Context,
    private val title: String
) :
    SavedStateRegistryOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory, OnBackPressedDispatcherOwner {


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

    override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher {
        Log.i("OnBackPressedDispatcher", "failed")
    }


    //悬浮窗显示状态
    private var isShowing = false

    //画布，不会再更新
    var view: View = FrameLayout(context)

    //窗口管理器对象
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    //窗口布局参数初始化
    var windowParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        title = this@ComposeFloatingWindow.title
        // 大小包裹内容
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        // 透明背景
        format = PixelFormat.TRANSLUCENT
        // 左上为基
        gravity = Gravity.START or Gravity.TOP
        windowAnimations = android.R.style.Animation_Dialog
        flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE

        if (context !is Activity) {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
        }
    }

    //回调函数
    private var onShow: (() -> Unit) = {}
    private var onHide: (() -> Unit) = {}
    private var onLayout: (it: WindowManager.LayoutParams) -> Unit = {}


    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        enableSavedStateHandles()
    }

    //设置悬浮窗组合式UI
    @RequiresApi(Build.VERSION_CODES.P)
    fun setContent(
        content: @Composable (composeFloatingWindow: ComposeFloatingWindow) -> Unit,
    ): ComposeFloatingWindow {
        this.view =
            ComposeView(context).also {
                it.setContent {
                    CompositionLocalProvider(
                        LocalFloatingWindow provides this@ComposeFloatingWindow
                    ) {
                        killTheme {
                            content(this)
                        }
                    }
                }
                it.setViewTreeLifecycleOwner(this@ComposeFloatingWindow)
                it.setViewTreeViewModelStoreOwner(this@ComposeFloatingWindow)
                it.setViewTreeSavedStateRegistryOwner(this@ComposeFloatingWindow)
                it.setViewTreeOnBackPressedDispatcherOwner(this@ComposeFloatingWindow)
                it.visibility = View.INVISIBLE
                it.doOnLayout { view ->
                    Log.i("doOnLayout", "width:${view.width},height:${view.height}")
                    onLayout(windowParams)
                    update()
                    view.visibility = View.VISIBLE
                }
            }
        return this
    }

    //设置悬浮窗状态回调
    fun setCallback(
        onShow: () -> Unit = {},
        onHide: () -> Unit = {},
        onLayout: (it: WindowManager.LayoutParams) -> Unit = {}
    ): ComposeFloatingWindow {
        this.onShow = onShow
        this.onHide = onHide
        this.onLayout = onLayout
        return this
    }

    fun updateLayoutParams(lambda: (WindowManager.LayoutParams) -> Unit) {
        lambda(windowParams)
        update()
    }

    fun isShowing(): Boolean {
        return isShowing
    }

    @SuppressLint("NewApi")
    fun show() {
        if (checkOverlayPermission(context)) {
            //拥有权限
            if (isShowing) {
                update()
                return
            }
            val reComposer = Recomposer(AndroidUiDispatcher.CurrentThread)
            view.compositionContext = reComposer
            lifecycleScope.launch(AndroidUiDispatcher.CurrentThread) { reComposer.runRecomposeAndApplyChanges() }
            windowManager.addView(view, windowParams)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            isShowing = true
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
        if (!isShowing) return
        windowManager.updateViewLayout(view, windowParams)
    }

    fun hide() {
        if (!isShowing) return
        isShowing = false
        windowManager.removeViewImmediate(view)
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