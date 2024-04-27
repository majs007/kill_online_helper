package kill.online.helper.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.Color
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

