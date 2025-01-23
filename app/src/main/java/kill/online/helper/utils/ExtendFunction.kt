package kill.online.helper.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import kill.online.helper.ui.window.LocalFloatingWindow
import java.nio.ByteBuffer
import java.security.MessageDigest

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.dragFloatingWindow(
    enabled: Boolean = true,
    onDragEnd: (windowOffset: Offset, dragArea: Offset) -> Unit = { _, _ -> },

    ): Modifier = composed {
    if (!enabled) return@composed this
    val floatingWindow = LocalFloatingWindow.current
    val windowParams = remember { floatingWindow.windowParams }
    pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            val w = floatingWindow.view.width
            val h = floatingWindow.view.height
            val f = Rect().also { floatingWindow.view.getWindowVisibleDisplayFrame(it) }
            Log.i("fwArea", "AreaWidth:${f.width()},AreaHeight:${f.height()}")
            Log.i("oldPointer", "x:${windowParams.x},y:${windowParams.y}")
            Log.i("dragAmount", "amountX:${dragAmount.x.toInt()},amountY:${dragAmount.y.toInt()}")
            windowParams.x = (windowParams.x + dragAmount.x.toInt()).coerceIn(0..(f.width() - w))
            windowParams.y = (windowParams.y + dragAmount.y.toInt()).coerceIn(0..(f.height() - h))
            Log.i("newPointer", "x:${windowParams.x},y:${windowParams.x}")
            floatingWindow.update()
            onDragEnd(
                Offset(windowParams.x.toFloat(), windowParams.y.toFloat()),
                Offset(f.width().toFloat(), f.height().toFloat())
            )
        }
    }
}

// 扩展函数：将整数转换为字节数组
fun Int.toByteArray(): ByteArray {
    return ByteBuffer.allocate(4).putInt(this).array()
}

fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    return bitmap.asImageBitmap()
}

fun ByteArray.toPainter(): Painter {
    val imageBitmap = this.toImageBitmap()
    return object : Painter() {
        override val intrinsicSize: androidx.compose.ui.geometry.Size
            get() = androidx.compose.ui.geometry.Size(
                imageBitmap.width.toFloat(),
                imageBitmap.height.toFloat()
            )

        override fun DrawScope.onDraw() {
            drawImage(imageBitmap)
        }
    }
}

fun ByteArray.toHexString(): String {
    return joinToString(separator = " ") { byte -> String.format("%02X", byte) }
}

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val bytes = md.digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}


