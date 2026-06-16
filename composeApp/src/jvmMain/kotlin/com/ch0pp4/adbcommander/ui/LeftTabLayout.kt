package com.ch0pp4.adbcommander.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import adbcommander.composeapp.generated.resources.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.CollectionCommandViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.ui.components.RenameDialog
import com.ch0pp4.adbcommander.ui.components.CreateCollectionDialog
import com.ch0pp4.adbcommander.ui.components.DeleteConfirmDialog
import com.ch0pp4.adbcommander.ui.components.TabSection
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeftTabLayout(
    selectedTab: MainTab,
    selectedCollection: UserCollectionUiModel?,
    visibleTabs: Set<MainTab>,
    hiddenCollectionIds: Set<Int>,
    initialExpandedTabs: Set<MainTab>,
    initialExpandedCollections: Set<Int>,
    onTabSelected: (MainTab) -> Unit,
    onCollectionSelected: (UserCollectionUiModel?) -> Unit,
    onExpandedTabsChange: (Set<MainTab>) -> Unit,
    onExpandedCollectionsChange: (Set<Int>) -> Unit,
    onCollectionDeleted: (Int) -> Unit,
    adbViewModel: AdbViewModel,
    broadcastViewModel: SendBroadcastViewModel,
    collectionViewModel: CollectionViewModel,
    collectionCommandViewModel: CollectionCommandViewModel,
    modifier: Modifier = Modifier,
) {
    val broadcastState by broadcastViewModel.uiState.collectAsState()
    val adbState by adbViewModel.uiState.collectAsState()
    val collectionCommandState by collectionCommandViewModel.uiState.collectAsState()
    val collectionItems by collectionCommandViewModel.collectionItems.collectAsState()
    val allCollections by collectionViewModel.collections.collectAsState()
    val userCollections = allCollections.filter { it.id !in hiddenCollectionIds }

    var showSaveDialog by remember { mutableStateOf(false) }
    var expandedTabs by remember { mutableStateOf(initialExpandedTabs) }

    // 컬렉션 개별 토글
    var expandedCollections by remember { mutableStateOf(initialExpandedCollections) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var deleteTargetCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }
    var renameTargetCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }

    LaunchedEffect(expandedTabs) {
        onExpandedTabsChange(expandedTabs)
    }

    LaunchedEffect(expandedCollections) {
        onExpandedCollectionsChange(expandedCollections)
    }

    LaunchedEffect(userCollections) {
        userCollections
            .filter { it.id in expandedCollections && collectionItems[it.id] == null }
            .forEach { collectionCommandViewModel.loadCollectionItems(it.id) }
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

        val listState = rememberLazyListState()
        var hasScrolledToTop by remember { mutableStateOf(false) }
        LaunchedEffect(allCollections) {
            if (!hasScrolledToTop && allCollections.isNotEmpty()) {
                listState.scrollToItem(0)
                hasScrolledToTop = true
            }
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(
                items = userCollections,
                key = { it.id },
            ) { collection ->
                TabSection(
                    label = collection.name,
                    expanded = expandedCollections.contains(collection.id),
                    items = collectionItems[collection.id] ?: emptyList(),
                    selectedItemId = if (selectedCollection?.id == collection.id) collectionCommandState.selectedItemId else null,
                    onHeaderClick = {
                        if (expandedCollections.contains(collection.id)) {
                            expandedCollections = expandedCollections - collection.id
                        } else {
                            expandedCollections = expandedCollections + collection.id
                            if (selectedCollection?.id != collection.id) {
                                broadcastViewModel.clearSelection()
                                adbViewModel.clearSelection()
                                collectionCommandViewModel.onReset()
                                onCollectionSelected(collection)
                            } else {
                                collectionCommandViewModel.loadCollectionItems(collection.id)
                            }
                        }
                    },
                    onItemSelected = { item ->
                        onCollectionSelected(collection)
                        collectionCommandViewModel.selectItem(item)
                        broadcastViewModel.clearSelection()
                        adbViewModel.clearSelection()
                    },
                    onItemDeleted = { collectionCommandViewModel.deleteItem(it) },
                    onItemRenamed = { item, title -> collectionCommandViewModel.renameItem(item, title) },
                    onRenameClick = { renameTargetCollection = collection },
                    onDeleteClick = { deleteTargetCollection = collection },
                )
            }

            if (MainTab.SEND_BROADCAST in visibleTabs) {
                item(key = "broadcast") {
                    TabSection(
                        label = stringResource(Res.string.tab_send_broadcast),
                        expanded = expandedTabs.contains(MainTab.SEND_BROADCAST),
                        items = broadcastState.savedItems,
                        selectedItemId = broadcastState.selectedItemId,
                        onHeaderClick = {
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
                        onItemSelected = { item ->
                            broadcastViewModel.selectItem(item)
                            adbViewModel.clearSelection()
                            collectionCommandViewModel.clearSelection()
                            onTabSelected(MainTab.SEND_BROADCAST)
                        },
                        onItemDeleted = { broadcastViewModel.deleteItem(it) },
                        onItemRenamed = { item, title -> broadcastViewModel.renameItem(item, title) },
                    )
                }
            }

            if (MainTab.COMMAND_LIST in visibleTabs) {
                item(key = "command") {
                    TabSection(
                        label = stringResource(Res.string.tab_command),
                        expanded = expandedTabs.contains(MainTab.COMMAND_LIST),
                        items = adbState.savedItems,
                        selectedItemId = adbState.selectedItemId,
                        onHeaderClick = {
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
                        onItemSelected = { item ->
                            adbViewModel.selectItem(item)
                            broadcastViewModel.clearSelection()
                            collectionCommandViewModel.clearSelection()
                            onTabSelected(MainTab.COMMAND_LIST)
                        },
                        onItemDeleted = { adbViewModel.deleteItem(it) },
                        onItemRenamed = { item, title -> adbViewModel.renameItem(item, title) },
                    )
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

    renameTargetCollection?.let { target ->
        RenameDialog(
            title = stringResource(Res.string.dialog_rename_title),
            initialValue = target.name,
            onConfirm = { name ->
                collectionViewModel.renameCollection(target.id, name)
                renameTargetCollection = null
            },
            onDismiss = { renameTargetCollection = null },
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
                collectionCommandViewModel.removeCollectionItems(target.id)
                onCollectionDeleted(target.id)
                deleteTargetCollection = null
            },
            onDismiss = { deleteTargetCollection = null },
        )
    }

    if (showSaveDialog) {
        RenameDialog(
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
                        val id = collectionCommandState.selectedItemId ?: return@RenameDialog
                        collectionCommandViewModel.updateCommand(id, name)
                    }
                    else -> when (selectedTab) {
                        MainTab.SEND_BROADCAST -> {
                            val id = broadcastState.selectedItemId ?: return@RenameDialog
                            broadcastViewModel.updateCommand(id, name)
                        }
                        MainTab.COMMAND_LIST -> {
                            val id = adbState.selectedItemId ?: return@RenameDialog
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
