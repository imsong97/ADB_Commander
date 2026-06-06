package com.ch0pp4.adbcommander.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.btn_cancel
import adbcommander.composeapp.generated.resources.btn_save
import adbcommander.composeapp.generated.resources.dialog_name_label
import androidx.compose.foundation.layout.width
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommandNameDialog(
    title: String,
    initialValue: String = "",
    confirmEnabled: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { },
        properties = properties,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(Res.string.dialog_name_label)) },
                singleLine = true,
                modifier = Modifier.width(300.dp),
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank() && confirmEnabled,
                onClick = { onConfirm(text) },
            ) { Text(stringResource(Res.string.btn_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        },
    )
}
