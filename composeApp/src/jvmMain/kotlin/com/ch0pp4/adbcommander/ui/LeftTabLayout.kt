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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import adbcommander.composeapp.generated.resources.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.ui.components.CommandNameDialog
import com.ch0pp4.adbcommander.ui.components.DeleteIconButton
import com.ch0pp4.adbcommander.ui.theme.LightOnSurfaceVariant
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeftTabLayout(
    selectedTab: MainTab,
    visibleTabs: Set<MainTab>,
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
        MainTab.SEND_BROADCAST -> broadcastState.completedCommand.isNotBlank() &&
            (broadcastState.selectedItemId == null || broadcastState.isModified)
        MainTab.COMMAND_LIST -> adbState.command.isNotBlank() &&
            (adbState.selectedItemId == null || adbState.isModified)
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (MainTab.SEND_BROADCAST in visibleTabs) {
                item(key = "header_broadcast") {
                    TreeTabHeader(
                        label = stringResource(Res.string.tab_send_broadcast),
                        expanded = expandedTabs.contains(MainTab.SEND_BROADCAST),
                        onClick = {
                            if (selectedTab == MainTab.SEND_BROADCAST) {
                                expandedTabs = if (expandedTabs.contains(MainTab.SEND_BROADCAST))
                                    expandedTabs - MainTab.SEND_BROADCAST
                                else
                                    expandedTabs + MainTab.SEND_BROADCAST
                            } else {
                                broadcastViewModel.onReset()
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
                                    isSelected = item.id == broadcastState.selectedItemId,
                                    onSelected = {
                                        broadcastViewModel.selectItem(item)
                                        adbViewModel.clearSelection()
                                        onTabSelected(MainTab.SEND_BROADCAST)
                                    },
                                    onDeleted = { broadcastViewModel.deleteItem(item) },
                                    onRenamed = { broadcastViewModel.renameItem(item, it) },
                                )
                            }
                        }
                    }
                }
            }

            if (MainTab.COMMAND_LIST in visibleTabs) {
                item(key = "header_command") {
                    TreeTabHeader(
                        label = stringResource(Res.string.tab_command),
                        expanded = expandedTabs.contains(MainTab.COMMAND_LIST),
                        onClick = {
                            if (selectedTab == MainTab.COMMAND_LIST) {
                                expandedTabs = if (expandedTabs.contains(MainTab.COMMAND_LIST))
                                    expandedTabs - MainTab.COMMAND_LIST
                                else
                                    expandedTabs + MainTab.COMMAND_LIST
                            } else {
                                adbViewModel.onReset()
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
                                    isSelected = item.id == adbState.selectedItemId,
                                    onSelected = {
                                        adbViewModel.selectItem(item)
                                        broadcastViewModel.clearSelection()
                                        onTabSelected(MainTab.COMMAND_LIST)
                                    },
                                    onDeleted = { adbViewModel.deleteItem(item) },
                                    onRenamed = { adbViewModel.renameItem(item, it) },
                                )
                            }
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
        CommandNameDialog(
            title = stringResource(Res.string.dialog_save_title),
            confirmEnabled = saveEnabled,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            onConfirm = { name ->
                when (selectedTab) {
                    MainTab.SEND_BROADCAST -> broadcastViewModel.saveCommand(name)
                    MainTab.COMMAND_LIST -> adbViewModel.saveCommand(name)
                }
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }
}

@Composable
private fun TreeTabHeader(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = rotation },
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SavedItemRow(
    item: SavedCommandUiModel,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onDeleted: () -> Unit,
    onRenamed: (String) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onSelected)
            .padding(start = 24.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title,
            modifier = Modifier.weight(1f).padding(top = 8.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = {
                renameText = item.title
                showRenameDialog = true
            },
            modifier = Modifier.size(28.dp).alpha(if (isHovered) 1f else 0f),
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = stringResource(Res.string.edit_command_title),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DeleteIconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(28.dp).alpha(if (isHovered) 1f else 0f),
        )
    }

    if (showRenameDialog) {
        CommandNameDialog(
            title = stringResource(Res.string.dialog_rename_title),
            initialValue = item.title,
            onConfirm = { name ->
                onRenamed(name)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
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
                ) {
                    Text(stringResource(Res.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightOnSurfaceVariant),
                ) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
        )
    }
}
