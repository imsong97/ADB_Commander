package com.ch0pp4.adbcommander.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.btn_cancel
import adbcommander.composeapp.generated.resources.btn_create
import adbcommander.composeapp.generated.resources.dialog_create_collection_title
import adbcommander.composeapp.generated.resources.dialog_name_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateCollectionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = { },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(Res.string.dialog_create_collection_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 50) text = it },
                label = { Text(stringResource(Res.string.dialog_name_label)) },
                modifier = Modifier.width(300.dp).focusRequester(focusRequester),
                singleLine = true,
                supportingText = { Text("${text.length}/50") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
            ) {
                Text(stringResource(Res.string.btn_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        },
    )
}
