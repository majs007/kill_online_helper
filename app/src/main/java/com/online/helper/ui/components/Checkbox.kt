package com.online.helper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


internal data class CheckBoxGroupItem<T>(
    val icon: ImageVector? = null,
    val text: @Composable () -> String,
    val value: T
)

@Composable
internal fun <T> CheckboxGroupDialog(
    title: String? = null,
    displayState: MutableState<Boolean>,
    items: Array<CheckBoxGroupItem<T>>,
    onClick: (CheckBoxGroupItem<T>) -> Unit
) {
    if (!displayState.value) return

    @Composable
    fun container(item: CheckBoxGroupItem<T>) {
        Row(modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(item) }
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .requiredWidth(175.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.icon?.let {
                    Icon(imageVector = it, contentDescription = null)
                    Spacer(modifier = Modifier.width(20.dp))
                }
                Text(
                    text = item.text(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    OneBtnAlterDialog(
        onDismissRequest = { },
        title = { title?.let { Text(text = it) } },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .985F),
        tonalElevation = 0.dp,
        button = {
            TextButton(onClick = { displayState.value = false }) {
                Text(text = "Cancel")
            }
        }
    ) {
        items.forEach { container(it) }
    }
}
