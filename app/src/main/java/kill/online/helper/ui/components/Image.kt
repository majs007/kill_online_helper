package kill.online.helper.ui.components

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kill.online.helper.utils.createImageDrawableFromImageDecoder
import java.io.File

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun DrawImage(
    name:String,
    contentDescription:String = "",
    modifier: Modifier
){
    val context = LocalContext.current
    val mediaUri by remember {
        mutableStateOf(
            Uri.parse(
                "android.resource://${context.packageName}/drawable/$name"
            )
        )
    }
    val drawable = rememberDrawablePainter(
        drawable = createImageDrawableFromImageDecoder(
            context,
            mediaUri
        ),
    )
    androidx.compose.foundation.Image(
        painter = drawable,
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ExternalImage(
    name:String,
    directory:String = "online_sticker",
    contentDescription:String = "",
    modifier: Modifier
){
    val context = LocalContext.current
    val dir = context.getExternalFilesDir(directory)
    val file = File(dir, name)
    val mediaUri by remember {
        mutableStateOf(
            Uri.fromFile(file)
        )
    }
    val drawable = rememberDrawablePainter(
        drawable = createImageDrawableFromImageDecoder(
            context,
            mediaUri
        ),
    )
    androidx.compose.foundation.Image(
        painter = drawable,
        contentDescription = contentDescription,
        modifier = modifier
    )
}