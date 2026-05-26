package com.ch0pp4.adbcommander.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import adbcommander.composeapp.generated.resources.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import com.ch0pp4.adbcommander.ui.components.DeleteIconButton
import com.ch0pp4.adbcommander.ui.components.ResultBox
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.BroadcastExtraUiModel
import com.ch0pp4.adbcommander.presentation.model.ExtraTypeList
import com.ch0pp4.adbcommander.presentation.model.IntentCommandType
import org.jetbrains.compose.resources.stringResource

@Composable
fun SendBroadcastCommandLayout(
    viewModel: SendBroadcastViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = state.commandType.actionFlag,
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                        .clickable { typeDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.commandType.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                DropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    IntentCommandType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                viewModel.onCommandTypeChange(type)
                                typeDropdownExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.primaryValue,
                onValueChange = viewModel::onPrimaryValueChange,
                placeholder = { Text(state.commandType.actionFlagHint) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )

            Button(onClick = viewModel::onRun, enabled = state.primaryValue.isNotBlank()) {
                Text(stringResource(Res.string.btn_run))
            }

            OutlinedButton(onClick = viewModel::onReset, enabled = state.primaryValue.isNotBlank()) {
                Text(stringResource(Res.string.btn_reset))
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.extras_label),
                style = MaterialTheme.typography.titleSmall,
            )
            IconButton(onClick = viewModel::onExtraAdd) {
                Text("➕", style = MaterialTheme.typography.labelLarge)
            }
        }

        ExtrasTable(
            extras = state.extras,
            onExtrasUpdate = viewModel::onExtrasUpdate,
            onExtraDelete = viewModel::onExtraDelete,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )

        HorizontalDivider()

        Text(
            text = stringResource(Res.string.completed_command),
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ResultBox(
                text = state.completedCommand,
                modifier = Modifier.weight(1f),
                selectable = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            )
            IconButton(onClick = viewModel::onCopy) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(Res.string.copy_command_description))
            }
        }

        Text(
            text = stringResource(Res.string.execution_result),
            style = MaterialTheme.typography.titleSmall,
        )

        ResultBox(
            text = state.executionResult,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            scrollable = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtrasTable(
    extras: List<BroadcastExtraUiModel>,
    onExtrasUpdate: (Int, BroadcastExtraUiModel) -> Unit,
    onExtraDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(stringResource(Res.string.col_type), modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(Res.string.col_extra), modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(Res.string.col_value), modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(36.dp))
        }

        HorizontalDivider()

        if (extras.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.extras_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                itemsIndexed(extras, key = { _, item -> item.id }) { index, extra ->
                    ExtraRow(
                        extra = extra,
                        onUpdate = { updated -> onExtrasUpdate(index, updated) },
                        onDelete = { onExtraDelete(index) },
                    )
                    if (index < extras.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtraRow(
    extra: BroadcastExtraUiModel,
    onUpdate: (BroadcastExtraUiModel) -> Unit,
    onDelete: () -> Unit,
) {
    var typeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it },
            modifier = Modifier.weight(1.5f),
        ) {
            OutlinedTextField(
                value = extra.type.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall,
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false },
            ) {
                ExtraTypeList.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onUpdate(extra.copy(type = type))
                            typeExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = extra.extra,
            onValueChange = { onUpdate(extra.copy(extra = it)) },
            modifier = Modifier.weight(2f),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
        )

        OutlinedTextField(
            value = extra.value,
            onValueChange = { onUpdate(extra.copy(value = it)) },
            modifier = Modifier.weight(2f),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
        )

        DeleteIconButton(onClick = onDelete, modifier = Modifier.size(36.dp))
    }
}
