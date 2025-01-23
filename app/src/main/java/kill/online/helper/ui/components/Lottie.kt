package kill.online.helper.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun DrawLottie(
    modifier: Modifier = Modifier,
    name: String,
    alignment: Alignment = Alignment.Center,
) {
    val context = LocalContext.current
    val mediaUri by remember {
        mutableStateOf(
            "android.resource://${context.packageName}/drawable/$name"
        )
    }
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Url(mediaUri))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        alignment = alignment,
        modifier = modifier,
    )
}

@Composable
fun AssetLottie(
    modifier: Modifier = Modifier,
    name: String,
    directory: String = "lottie",
    alignment: Alignment = Alignment.Center,
    iterations: Int = LottieConstants.IterateForever,
    onEnd: () -> Unit = {},
) {
    val TAG = "AssetLottie"
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Asset("${directory}/${name}"))
    val progress = animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
    )
    LaunchedEffect(progress.isAtEnd) {
//        Log.d(TAG, "AssetLottie: $progress")
        if (progress.isAtEnd && progress.value == 1.0f) {
            Log.i(TAG, "AssetLottie: play end, process: ${progress.value}")
            onEnd()
        }
    }
    LottieAnimation(
        composition = composition,
        progress = { progress.value },
        alignment = alignment,
        modifier = modifier
    )
    ContentScale.Fit
}