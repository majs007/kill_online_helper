package kill.online.helper.ui.window

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.dragFloatingWindow(): Modifier = composed {
    val floatingWindow = LocalFloatingWindow.current
    val windowParams = remember { floatingWindow.windowParams }
    pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            val w = floatingWindow.decorView.width
            val h = floatingWindow.decorView.height
            val f = Rect().also { floatingWindow.decorView.getWindowVisibleDisplayFrame(it) }
            Log.i("content", "contentWidth:${f.width()},contentHeight:${f.height()}")
            Log.i("oldPointer", "x:${windowParams.x},y:${windowParams.x}")
            Log.i("dragAmount", "amountX:${dragAmount.x.toInt()},amountY:${dragAmount.y.toInt()}")
            windowParams.x = (windowParams.x + dragAmount.x.toInt()).coerceIn(0..(f.width() - w))
            windowParams.y = (windowParams.y + dragAmount.y.toInt()).coerceIn(0..(f.height() - h))
            Log.i("newPointer", "x:${windowParams.x},y:${windowParams.x}")
            floatingWindow.update()
        }
    }
}


