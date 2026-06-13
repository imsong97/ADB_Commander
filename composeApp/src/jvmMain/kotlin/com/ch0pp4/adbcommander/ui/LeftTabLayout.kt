package com.ch0pp4.adbcommander.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.CollectionCommandViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.ui.components.CommandNameDialog
import com.ch0pp4.adbcommander.ui.components.CreateCollectionDialog
import com.ch0pp4.adbcommander.ui.components.DeleteConfirmDialog
import com.ch0pp4.adbcommander.ui.components.DeleteIconButton
import com.ch0pp4.adbcommander.ui.theme.LightSidebarSelected
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeftTabLayout(
    selectedTab: MainTab,
    selectedCollection: UserCollectionUiModel?,
    visibleTabs: Set<MainTab>,
    onTabSelected: (MainTab) -> Unit,
    onCollectionSelected: (UserCollectionUiModel?) -> Unit,
    adbViewModel: AdbViewModel,
    broadcastViewModel: SendBroadcastViewModel,
    collectionViewModel: CollectionViewModel,
    collectionCommandViewModel: CollectionCommandViewModel,
    modifier: Modifier = Modifier,
) {
    val broadcastState by broadcastViewModel.uiState.collectAsState()
    val adbState by adbViewModel.uiState.collectAsState()
    val collectionCommandState by collectionCommandViewModel.uiState.collectAsState()
    val userCollections by collectionViewModel.collections.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var expandedTabs by remember { mutableStateOf(setOf(selectedTab)) }
    var expandedCollections by remember { mutableStateOf(setOf<Int>()) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var deleteTargetCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }

    LaunchedEffect(selectedCollection) {
        val newId = selectedCollection?.id
        if (newId != null) {
            expandedCollections = setOf(newId)
        }
        // null(다른 탭으로 이동)이면 expandedCollections 그대로 유지
    }

    val saveEnabled = when {
        selectedCollection != null -> collectionCommandState.completedCommand.isNotBlank() &&
            (collectionCommandState.selectedItemId == null || collectionCommandState.isModified)
        else -> when (selectedTab) {
            MainTab.SEND_BROADCAST -> broadcastState.completedCommand.isNotBlank() &&
                (broadcastState.selectedItemId == null || broadcastState.isModified)
            MainTab.COMMAND_LIST -> adbState.command.isNotBlank() &&
                (adbState.selectedItemId == null || adbState.isModified)
        }
    }

    val showUpdateOption = when {
        selectedCollection != null -> collectionCommandState.selectedItemId != null && collectionCommandState.isModified
        else -> when (selectedTab) {
            MainTab.SEND_BROADCAST -> broadcastState.selectedItemId != null && broadcastState.isModified
            MainTab.COMMAND_LIST -> adbState.selectedItemId != null && adbState.isModified
        }
    }

    val currentTitle = when {
        selectedCollection != null -> collectionCommandState.selectedTitle ?: ""
        else -> when (selectedTab) {
            MainTab.SEND_BROADCAST -> broadcastState.selectedTitle ?: ""
            MainTab.COMMAND_LIST -> adbState.selectedTitle ?: ""
        }
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.new_collection),
                fontSize = 10.sp,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { showCreateCollectionDialog = true },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(Res.string.new_collection),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(
                items = userCollections,
                key = { it.id },
            ) { collection ->
                TreeTabHeader(
                    label = collection.name,
                    expanded = expandedCollections.contains(collection.id),
                    onClick = {
                        if (selectedCollection?.id == collection.id) {
                            expandedCollections = if (expandedCollections.contains(collection.id))
                                expandedCollections - collection.id
                            else
                                expandedCollections + collection.id
                        } else {
                            broadcastViewModel.clearSelection()
                            adbViewModel.clearSelection()
                            collectionCommandViewModel.onReset()
                            onCollectionSelected(collection)
                            expandedCollections = expandedCollections + collection.id
                        }
                    },
                    onDeleteClick = { deleteTargetCollection = collection },
                )
                AnimatedVisibility(
                    visible = expandedCollections.contains(collection.id),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        collectionCommandState.savedItems.forEach { item ->
                            SavedItemRow(
                                item = item,
                                isSelected = item.id == collectionCommandState.selectedItemId,
                                onSelected = {
                                    collectionCommandViewModel.selectItem(item)
                                    broadcastViewModel.clearSelection()
                                    adbViewModel.clearSelection()
                                    onCollectionSelected(collection)
                                },
                                onDeleted = { collectionCommandViewModel.deleteItem(item) },
                                onRenamed = { collectionCommandViewModel.renameItem(item, it) },
                            )
                        }
                    }
                }
            }

            if (MainTab.SEND_BROADCAST in visibleTabs) {
                item(key = "header_broadcast") {
                    TreeTabHeader(
                        label = stringResource(Res.string.tab_send_broadcast),
                        expanded = expandedTabs.contains(MainTab.SEND_BROADCAST),
                        onClick = {
                            if (selectedTab == MainTab.SEND_BROADCAST && selectedCollection == null) {
                                expandedTabs = if (expandedTabs.contains(MainTab.SEND_BROADCAST))
                                    expandedTabs - MainTab.SEND_BROADCAST
                                else
                                    expandedTabs + MainTab.SEND_BROADCAST
                            } else {
                                broadcastViewModel.onReset()
                                adbViewModel.clearSelection()
                                onCollectionSelected(null)
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
                                        collectionCommandViewModel.clearSelection()
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
                            if (selectedTab == MainTab.COMMAND_LIST && selectedCollection == null) {
                                expandedTabs = if (expandedTabs.contains(MainTab.COMMAND_LIST))
                                    expandedTabs - MainTab.COMMAND_LIST
                                else
                                    expandedTabs + MainTab.COMMAND_LIST
                            } else {
                                adbViewModel.onReset()
                                broadcastViewModel.clearSelection()
                                onCollectionSelected(null)
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
                                        collectionCommandViewModel.clearSelection()
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
            onClick = { showSaveDialog = true },
            enabled = saveEnabled,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.save_current_command),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            onConfirm = { name ->
                collectionViewModel.createCollection(name)
                showCreateCollectionDialog = false
            },
            onDismiss = { showCreateCollectionDialog = false },
        )
    }

    deleteTargetCollection?.let { target ->
        DeleteConfirmDialog(
            title = stringResource(Res.string.dialog_delete_collection_title),
            message = "\"${target.name}\"\n${stringResource(Res.string.dialog_delete_collection_message)}",
            onConfirm = {
                collectionViewModel.deleteCollection(target.id)
                if (selectedCollection?.id == target.id) {
                    onCollectionSelected(null)
                }
                expandedCollections = expandedCollections - target.id
                deleteTargetCollection = null
            },
            onDismiss = { deleteTargetCollection = null },
        )
    }

    if (showSaveDialog) {
        CommandNameDialog(
            title = stringResource(Res.string.dialog_save_title),
            initialValue = if (showUpdateOption) currentTitle else "",
            confirmEnabled = saveEnabled,
            showUpdateOption = showUpdateOption,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            onConfirm = { name ->
                when {
                    selectedCollection != null -> collectionCommandViewModel.saveCommand(name)
                    else -> when (selectedTab) {
                        MainTab.SEND_BROADCAST -> broadcastViewModel.saveCommand(name)
                        MainTab.COMMAND_LIST -> adbViewModel.saveCommand(name)
                    }
                }
                showSaveDialog = false
            },
            onConfirmUpdate = { name ->
                when {
                    selectedCollection != null -> {
                        val id = collectionCommandState.selectedItemId ?: return@CommandNameDialog
                        collectionCommandViewModel.updateCommand(id, name)
                    }
                    else -> when (selectedTab) {
                        MainTab.SEND_BROADCAST -> {
                            val id = broadcastState.selectedItemId ?: return@CommandNameDialog
                            broadcastViewModel.updateCommand(id, name)
                        }
                        MainTab.COMMAND_LIST -> {
                            val id = adbState.selectedItemId ?: return@CommandNameDialog
                            adbViewModel.updateCommand(id, name)
                        }
                    }
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
    onAddClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hoverable(interactionSource)
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onAddClick != null) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(28.dp).alpha(if (isHovered) 1f else 0f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onDeleteClick != null) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(28.dp).alpha(if (isHovered) 1f else 0f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .background(
                color = if (isSelected) LightSidebarSelected else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp))
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onSelected)
            .padding(start = 18.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title,
            modifier = Modifier.weight(1f).padding(top = 6.dp, bottom = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
        DeleteConfirmDialog(
            title = stringResource(Res.string.dialog_delete_title),
            message = "\"${item.title}\"\n${stringResource(Res.string.dialog_delete_message)}",
            onConfirm = {
                onDeleted()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
