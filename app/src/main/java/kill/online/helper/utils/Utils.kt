package kill.online.helper.utils

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.random.Random

fun generateRandomGradientColors(): List<Color> {
    val color1 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    val color2 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    return listOf(color1, color2)
}


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun backToHome(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addCategory(Intent.CATEGORY_HOME)
    ContextCompat.startActivity(context, intent, null)
}

fun dpToPx(context: Context, dp: Float): Float {
    val density = context.resources.displayMetrics.density
    return dp * density
}

fun pxToDp(context: Context, px: Float): Dp {
    val density = context.resources.displayMetrics.density
    return (px / density).dp
}

/*@SuppressLint("Api 28", "NewApi")
fun createImageDrawableFromImageDecoder(context: Context, uri: Uri): AnimatedImageDrawable {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val drawable = ImageDecoder.decodeDrawable(source)
    return drawable as AnimatedImageDrawable
}*/

@RequiresApi(Build.VERSION_CODES.P)
fun createImageDrawableFromImageDecoder(context: Context, uri: Uri): Drawable {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val drawable = ImageDecoder.decodeDrawable(source)

    return if (drawable is AnimatedImageDrawable) {
        drawable // 如果是动画图片，直接返回 AnimatedImageDrawable
    } else {
        drawable // 如果是静态图片，返回普通的 Drawable（如 BitmapDrawable）
    }
}


/*fun getDrawableNames(context: Context): List<String> {
    val drawableNames = mutableListOf<String>()
    try {
        // 获取 R.drawable 类中的所有字段
        val drawableClass = R.drawable::class.java
        val fields: Array<Field> = drawableClass.declaredFields

        // 遍历字段，获取资源名
        for (field in fields) {
            field.isAccessible = true
            val resourceId = field.getInt(null) // 获取资源 ID
            val resourceName = context.resources.getResourceEntryName(resourceId)
            drawableNames.add(resourceName)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return drawableNames
}*/
