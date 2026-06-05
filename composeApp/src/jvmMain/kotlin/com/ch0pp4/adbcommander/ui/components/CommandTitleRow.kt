package com.ch0pp4.adbcommander.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.untitled_default
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommandTitleRow(
    title: String?,
    isModified: Boolean,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title ?: stringResource(Res.string.untitled_default),
            style = style,
        )
        if (isModified) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
