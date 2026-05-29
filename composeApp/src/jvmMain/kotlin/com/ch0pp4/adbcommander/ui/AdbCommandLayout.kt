package com.ch0pp4.adbcommander.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import adbcommander.composeapp.generated.resources.*
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.ui.components.ResultBox
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdbCommandLayout(
    viewModel: AdbViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.adb_command_label),
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.command,
                onValueChange = viewModel::onCommandChange,
                placeholder = { Text(stringResource(Res.string.adb_command_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(
                onClick = viewModel::onRun,
                enabled = state.command.isNotBlank(),
            ) {
                Text(stringResource(Res.string.btn_run))
            }
            OutlinedButton(
                onClick = viewModel::onReset,
                enabled = state.command.isNotBlank() || state.executionResult.isNotBlank(),
            ) {
                Text(stringResource(Res.string.btn_reset))
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.execution_result),
            style = MaterialTheme.typography.titleSmall,
        )

        ResultBox(
            text = state.executionResult,
            modifier = Modifier.fillMaxSize(),
            selectable = true,
            scrollable = true,
        )
    }
}
