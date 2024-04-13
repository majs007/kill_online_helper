package com.online.helper.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun generateRandomGradientColors(): List<Color> {
    val color1 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    val color2 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    return listOf(color1, color2)
}


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
fun simulateHome(context:Context){
    val intent = Intent(Intent.ACTION_MAIN)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addCategory(Intent.CATEGORY_HOME)
    ContextCompat.startActivity(context, intent, null)
}

