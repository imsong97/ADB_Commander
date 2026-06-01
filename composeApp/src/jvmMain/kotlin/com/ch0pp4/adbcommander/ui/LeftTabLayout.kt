package com.ch0pp4.adbcommander.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import adbcommander.composeapp.generated.resources.*
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.ui.components.DeleteIconButton
import com.ch0pp4.adbcommander.ui.theme.LightOnSurfaceVariant
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeftTabLayout(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    adbViewModel: AdbViewModel,
    broadcastViewModel: SendBroadcastViewModel,
    modifier: Modifier = Modifier,
) {
    val broadcastState by broadcastViewModel.uiState.collectAsState()
    val adbState by adbViewModel.uiState.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveDialogName by remember { mutableStateOf("") }
    var expandedTabs by remember { mutableStateOf(setOf(selectedTab)) }

    val saveEnabled = when (selectedTab) {
        MainTab.SEND_BROADCAST -> broadcastState.completedCommand.isNotBlank()
        MainTab.COMMAND_LIST -> adbState.command.isNotBlank()
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "header_broadcast") {
                TreeTabHeader(
                    label = stringResource(Res.string.tab_send_broadcast),
                    selected = selectedTab == MainTab.SEND_BROADCAST,
                    onClick = {
                        if (selectedTab == MainTab.SEND_BROADCAST) {
                            expandedTabs = if (expandedTabs.contains(MainTab.SEND_BROADCAST))
                                expandedTabs - MainTab.SEND_BROADCAST
                            else
                                expandedTabs + MainTab.SEND_BROADCAST
                        } else {
                            onTabSelected(MainTab.SEND_BROADCAST)
                            expandedTabs = expandedTabs + MainTab.SEND_BROADCAST
                        }
                    },
                )
            }
            item(key = "items_broadcast") {
                AnimatedVisibility(
                    visible = expandedTabs.contains(MainTab.SEND_BROADCAST),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        broadcastState.savedItems.forEach { item ->
                            SavedItemRow(
                                item = item,
                                onSelected = {
                                    broadcastViewModel.selectItem(item)
                                    onTabSelected(MainTab.SEND_BROADCAST)
                                },
                                onDeleted = { broadcastViewModel.deleteItem(item) },
                            )
                        }
                    }
                }
            }

            item(key = "header_command") {
                TreeTabHeader(
                    label = stringResource(Res.string.tab_command),
                    selected = selectedTab == MainTab.COMMAND_LIST,
                    onClick = {
                        if (selectedTab == MainTab.COMMAND_LIST) {
                            expandedTabs = if (expandedTabs.contains(MainTab.COMMAND_LIST))
                                expandedTabs - MainTab.COMMAND_LIST
                            else
                                expandedTabs + MainTab.COMMAND_LIST
                        } else {
                            onTabSelected(MainTab.COMMAND_LIST)
                            expandedTabs = expandedTabs + MainTab.COMMAND_LIST
                        }
                    },
                )
            }
            item(key = "items_command") {
                AnimatedVisibility(
                    visible = expandedTabs.contains(MainTab.COMMAND_LIST),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        adbState.savedItems.forEach { item ->
                            SavedItemRow(
                                item = item,
                                onSelected = {
                                    adbViewModel.selectItem(item)
                                    onTabSelected(MainTab.COMMAND_LIST)
                                },
                                onDeleted = { adbViewModel.deleteItem(item) },
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()
        TextButton(
            onClick = {
                saveDialogName = ""
                showSaveDialog = true
            },
            enabled = saveEnabled,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.save_current_command),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(Res.string.dialog_save_title)) },
            text = {
                OutlinedTextField(
                    value = saveDialogName,
                    onValueChange = { saveDialogName = it },
                    label = { Text(stringResource(Res.string.dialog_name_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = saveDialogName.isNotBlank() && saveEnabled,
                    onClick = {
                        when (selectedTab) {
                            MainTab.SEND_BROADCAST -> broadcastViewModel.saveCommand(saveDialogName)
                            MainTab.COMMAND_LIST -> adbViewModel.saveCommand(saveDialogName)
                        }
                        showSaveDialog = false
                    },
                ) { Text(stringResource(Res.string.btn_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
        )
    }
}

@Composable
private fun TreeTabHeader(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SavedItemRow(
    item: SavedCommandUiModel,
    onSelected: () -> Unit,
    onDeleted: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(start = 36.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
        DeleteIconButton(onClick = { showDeleteDialog = true })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(Res.string.dialog_delete_title)) },
            text = { Text("\"${item.title}\"\n${stringResource(Res.string.dialog_delete_message)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleted()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.btn_delete)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightOnSurfaceVariant),
                ) {
                    Text(stringResource(Res.string.btn_cancel)
                    )
                }
            },
        )
    }
}
