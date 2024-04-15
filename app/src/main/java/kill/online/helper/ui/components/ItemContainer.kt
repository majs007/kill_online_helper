package kill.online.helper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
internal fun ItemContainer(
    icon: ImageVector,
    onIconClicked: () -> Unit,
    iconEnabled: Boolean,
    text: @Composable () -> String,
    subText: (@Composable () -> String)? = null,
    onClick: () -> Unit = {},
    enable: Boolean = true
) {
    BasicItemContainer(
        icon = icon,
        onIconClicked = onIconClicked,
        iconEnabled = iconEnabled,
        text = text,
        subText = subText,
        onClick = onClick,
        enable = enable
    )
}


@Composable
fun BasicItemContainer(
    icon: ImageVector,
    onIconClicked: () -> Unit = {},
    iconEnabled: Boolean = false,
    text: @Composable () -> String,
    subText: (@Composable () -> String)? = null,
    onClick: () -> Unit = {},
    tailContent: @Composable () -> Unit = {},
    padding: PaddingValues = PaddingValues(vertical = 15.dp),
    enable: Boolean = true
) {
    Row(modifier = Modifier
        .clickable { if (enable) onClick() }
        .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .zIndex(1F)
                    .fillMaxWidth(0.75F),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(25.dp))
                IconButton(onClick = onIconClicked, enabled = iconEnabled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(text = text(), style = MaterialTheme.typography.bodyLarge)
                    subText?.let {
                        Text(
                            text = it(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            if (tailContent != {}) tailContent()
        }
    }
}


@Composable
fun BasicItemContainer(
    icon: String,
    onIconClicked: () -> Unit = {},
    iconEnabled: Boolean = false,
    text: @Composable () -> String,
    subText: (@Composable () -> String)? = null,
    onClick: () -> Unit = {},
    tailContent: @Composable () -> Unit = {},
    padding: PaddingValues = PaddingValues(vertical = 15.dp),
    enable: Boolean = true
) {
    Row(modifier = Modifier
        .clickable { if (enable) onClick() }
        .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .zIndex(1F)
                    .fillMaxWidth(0.75F),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(25.dp))
                Text(
                    text = icon,
                    fontSize = 25.sp,
                    modifier = Modifier.clickable(enabled = iconEnabled, onClick = onIconClicked)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(text = text(), style = MaterialTheme.typography.bodyLarge)
                    subText?.let {
                        Text(
                            text = it(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            if (tailContent != {}) tailContent()
        }
    }
}


@Composable
internal fun SwitchItemContainer(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    icon: ImageVector,
    onIconClicked: () -> Unit = {},
    iconEnabled: Boolean = false,
    text: @Composable () -> String,
    subText: (@Composable () -> String)? = null,
    enable: Boolean = true
) {
    BasicItemContainer(
        icon = icon,
        onIconClicked = onIconClicked,
        iconEnabled = iconEnabled,
        text = text,
        subText = subText,
        onClick = onCheckedChange,
        padding = PaddingValues(vertical = 8.dp),
        tailContent = {
            Switch(
                checked = checked,
                onCheckedChange = { onCheckedChange() },
                modifier = Modifier.padding(end = 15.dp),
                enabled = enable
            )
        },
        enable = enable
    )
}


@Composable
internal fun NavItemContainer(
    icon: ImageVector,
    onIconClicked: () -> Unit = {},
    iconEnabled: Boolean = false,
    text: @Composable () -> String,
    subText: (@Composable () -> String)? = null,
    onClick: () -> Unit = {},
) {
    BasicItemContainer(
        icon = icon,
        onIconClicked = onIconClicked,
        iconEnabled = iconEnabled,
        text = text,
        subText = subText,
        onClick = onClick,
        padding = PaddingValues(vertical = 12.dp)
    )
}