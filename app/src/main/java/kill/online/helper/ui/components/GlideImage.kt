/*
package kill.online.helper.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import java.io.File

@Composable
fun GlideImage(url: String, modifier: Modifier = Modifier) {
    // 使用 AndroidView 将传统的 ImageView 集成到 Compose 中
    AndroidView(factory = { context: Context ->
        ImageView(context).apply {
            // 这里使用 Glide 加载图像
            Glide.with(context)
                .load(url) // 通过 URL 加载图像
                .into(this) // 加载到 ImageView 中
        }
    }, update = { imageView: ImageView ->
        Glide.with(imageView.context)
            .load(url) // 更新时加载图像
            .into(imageView) // 加载到 ImageView 中
    }, modifier = modifier
    )
}

@Composable
fun GlideImage(uri: Uri, modifier: Modifier = Modifier) {
    // 使用 AndroidView 将传统的 ImageView 集成到 Compose 中
    AndroidView(factory = { context: Context ->
        ImageView(context).apply {
            // 这里使用 Glide 加载图像
            Glide.with(context)
                .load(uri) // 通过 URL 加载图像
                .into(this) // 加载到 ImageView 中
        }
    }, update = { imageView: ImageView ->
        Glide.with(imageView.context)
            .load(uri) // 更新时加载图像
            .into(imageView) // 加载到 ImageView 中
    }, modifier = modifier
    )
}

@Composable
fun GlideImage(file: File, modifier: Modifier = Modifier) {
    // 使用 AndroidView 将传统的 ImageView 集成到 Compose 中
    AndroidView(factory = { context: Context ->
        ImageView(context).apply {
            // 这里使用 Glide 加载图像
            Glide.with(context)
                .load(file)
                .into(this) // 加载到 ImageView 中
        }
    }, update = { imageView: ImageView ->
        Glide.with(imageView.context)
            .load(file) // 更新时加载图像
            .into(imageView) // 加载到 ImageView 中
    }, modifier = modifier
    )
}

@Composable
fun GlideImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
    // 使用 AndroidView 将传统的 ImageView 集成到 Compose 中
    AndroidView(factory = { context: Context ->
        ImageView(context).apply {
            // 这里使用 Glide 加载图像
            Glide.with(context)
                .load(bitmap)
                .into(this) // 加载到 ImageView 中
        }
    }, update = { imageView: ImageView ->
        Glide.with(imageView.context)
            .load(bitmap) // 更新时加载图像
            .into(imageView) // 加载到 ImageView 中
    }, modifier = modifier
    )
}*/
