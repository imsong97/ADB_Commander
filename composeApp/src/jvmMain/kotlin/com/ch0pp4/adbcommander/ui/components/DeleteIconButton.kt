package com.ch0pp4.adbcommander.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeleteIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(32.dp),
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete Item",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
