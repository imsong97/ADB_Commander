package com.ch0pp4.adbcommander.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.btn_cancel
import adbcommander.composeapp.generated.resources.btn_save
import adbcommander.composeapp.generated.resources.dialog_name_label
import adbcommander.composeapp.generated.resources.save_option_new
import adbcommander.composeapp.generated.resources.save_option_update
import androidx.compose.foundation.layout.padding
import org.jetbrains.compose.resources.stringResource

private enum class SaveOption { UPDATE, NEW }

@Composable
fun CommandNameDialog(
    title: String,
    initialValue: String = "",
    confirmEnabled: Boolean = true,
    showUpdateOption: Boolean = false,
    properties: DialogProperties = DialogProperties(),
    onConfirm: (String) -> Unit,
    onConfirmUpdate: (String) -> Unit = {},
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }
    var saveOption by remember { mutableStateOf(SaveOption.UPDATE) }

    AlertDialog(
        onDismissRequest = { },
        properties = properties,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.width(300.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(Res.string.dialog_name_label)) },
                    singleLine = true,
                )
                if (showUpdateOption) {
                    Spacer(Modifier.height(16.dp))
                    SaveOptionRow(
                        label = stringResource(Res.string.save_option_update),
                        selected = saveOption == SaveOption.UPDATE,
                        onSelect = { saveOption = SaveOption.UPDATE },
                    )
                    Spacer(Modifier.height(8.dp))
                    SaveOptionRow(
                        label = stringResource(Res.string.save_option_new),
                        selected = saveOption == SaveOption.NEW,
                        onSelect = { saveOption = SaveOption.NEW },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank() && confirmEnabled,
                onClick = {
                    if (showUpdateOption && saveOption == SaveOption.UPDATE) {
                        onConfirmUpdate(text)
                    } else {
                        onConfirm(text)
                    }
                },
            ) { Text(stringResource(Res.string.btn_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.btn_cancel))
            }
        },
    )
}

@Composable
private fun SaveOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(300.dp).padding(start = 4.dp),
        )
    }
}
