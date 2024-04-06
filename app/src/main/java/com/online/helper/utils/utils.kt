package com.online.helper.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun generateRandomGradientColors(): List<Color> {
    val color1 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    val color2 = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    return listOf(color1, color2)
}

@Composable
fun showToastMessage(activity: ComponentActivity, message: String, duration: Int = Toast.LENGTH_SHORT) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activity) {
        coroutineScope.launch {
            delay(100) // Delay to make sure the context has been updated
            Toast.makeText(activity, message, duration).show()
        }
    }
}
fun showToast(context: ComponentActivity, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

