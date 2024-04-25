package kill.online.helper.ui.page

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kill.online.helper.ui.theme.messageAlpha


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowMessageContent() {
    val messageList = remember {
        listOf(
            kill.online.helper.data.Message("海绵宝宝", "hcdakskajcdka"),
            kill.online.helper.data.Message("派大星", "hdsjadakjashfdkas"),
            kill.online.helper.data.Message("海绵宝宝", "萨达大家积极哦啊就递交哦叫哦耍大刀i就"),
            kill.online.helper.data.Message("派大星", "德哈卡都去啊点进去哦ID去哦去哦的放弃哦i"),
            kill.online.helper.data.Message("章鱼哥", "别在这里发癫😅😅😅"),
        )
    }
    var inputMessage by remember { mutableStateOf("德哈卡都去啊点进去哦ID去哦去哦的放弃哦i") }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
//            .background(Color.Red)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
//                .background(Color.Green)
        ) {
            itemsIndexed(messageList) { index, item ->
                MessageCard(item)
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
//                .background(Color.Yellow)
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.KeyboardVoice, contentDescription = null)
            }
            OutlinedTextField(
                value = inputMessage, onValueChange = { inputMessage = it },
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.White.copy(0.5f),
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedBorderColor = Color.White,
                    cursorColor = Color.Gray,
                ),

                modifier = Modifier
                    .weight(0.9f)
                    .focusable()
            )
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.EmojiEmotions, contentDescription = null)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}


@Composable
fun MessageCard(msg: kill.online.helper.data.Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
//                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = msg.playerName,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = Color.White.copy(messageAlpha),
                modifier = Modifier
                    .padding(1.dp)
                    .focusable(true)
            ) {
                Text(
                    text = msg.msg,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = 10,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}