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
import com.ch0pp4.adbcommander.presentation.CollectionItemViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.ui.components.RenameDialog
import com.ch0pp4.adbcommander.ui.components.CreateCollectionDialog
import com.ch0pp4.adbcommander.ui.components.DeleteConfirmDialog
import com.ch0pp4.adbcommander.ui.components.TabSection
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LeftTabLayout(
    selectedCollection: UserCollectionUiModel?,
    hiddenCollectionIds: Set<Int>,
    initialExpandedCollections: Set<Int>,
    onCollectionSelected: (UserCollectionUiModel?) -> Unit,
    onExpandedCollectionsChange: (Set<Int>) -> Unit,
    onCollectionDeleted: (Int) -> Unit,
    collectionViewModel: CollectionViewModel,
    collectionItemViewModel: CollectionItemViewModel,
    modifier: Modifier = Modifier,
) {
    val collectionCommandState by collectionItemViewModel.uiState.collectAsState()
    val collectionItems by collectionItemViewModel.collectionItems.collectAsState()
    val allCollections by collectionViewModel.collections.collectAsState()
    val userCollections = allCollections.filter { it.id !in hiddenCollectionIds }
    var localUserCollections by remember { mutableStateOf(userCollections) }
    LaunchedEffect(userCollections) { localUserCollections = userCollections }

    var showSaveDialog by remember { mutableStateOf(false) }
    var expandedCollections by remember { mutableStateOf(initialExpandedCollections) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var deleteTargetCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }
    var renameTargetCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }

    LaunchedEffect(expandedCollections) {
        onExpandedCollectionsChange(expandedCollections)
    }

    LaunchedEffect(userCollections) {
        userCollections
            .filter { it.id in expandedCollections && collectionItems[it.id] == null }
            .forEach { collectionItemViewModel.loadCollectionItems(it.id) }
    }

    val saveEnabled = selectedCollection != null &&
        collectionCommandState.completedCommand.isNotBlank() &&
        (collectionCommandState.selectedItemId == null || collectionCommandState.isModified)

    val showUpdateOption = selectedCollection != null &&
        collectionCommandState.selectedItemId != null &&
        collectionCommandState.isModified

    val currentTitle = collectionCommandState.selectedTitle ?: ""

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
        var prevCollectionCount by remember { mutableStateOf(0) }
        LaunchedEffect(allCollections.size) {
            if (allCollections.size > prevCollectionCount) {
                listState.scrollToItem(0)
            }
            prevCollectionCount = allCollections.size
        }

        val reorderState = rememberReorderableLazyListState(
            lazyListState = listState,
        ) { from, to ->
            localUserCollections = localUserCollections.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }

        LaunchedEffect(reorderState) {
            snapshotFlow { reorderState.isAnyItemDragging }
                .filter { !it }
                .drop(1)
                .collect {
                    val visibleIdSet = localUserCollections.map { it.id }.toSet()
                    val visibleIter = localUserCollections.iterator()
                    val fullOrderedIds = allCollections.map { col ->
                        if (col.id in visibleIdSet) visibleIter.next().id else col.id
                    }
                    collectionViewModel.reorderCollections(fullOrderedIds)
                }
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(
                items = localUserCollections,
                key = { it.id },
            ) { collection ->
                ReorderableItem(reorderState, key = collection.id) { _ ->
                    Column {
                        TabSection(
                            label = collection.name,
                            expanded = expandedCollections.contains(collection.id),
                            items = collectionItems[collection.id] ?: emptyList(),
                            selectedItemId = if (selectedCollection?.id == collection.id) collectionCommandState.selectedItemId else null,
                            onToggleClick = {
                                expandedCollections = if (collection.id in expandedCollections) {
                                    expandedCollections - collection.id
                                } else {
                                    expandedCollections + collection.id
                                }
                            },
                            onHeaderClick = {
                                collectionItemViewModel.onReset()
                                onCollectionSelected(collection)
                            },
                            onItemSelected = { item ->
                                onCollectionSelected(collection)
                                collectionItemViewModel.selectItem(item)
                            },
                            onItemDeleted = { collectionItemViewModel.deleteItem(it) },
                            onItemRenamed = { item, title -> collectionItemViewModel.renameItem(item, title) },
                            headerModifier = Modifier.draggableHandle(),
                            onRenameClick = { renameTargetCollection = collection },
                            onDeleteClick = { deleteTargetCollection = collection },
                        )
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
                collectionItemViewModel.removeCollectionItems(target.id)
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
                collectionItemViewModel.saveCommand(name)
                showSaveDialog = false
            },
            onConfirmUpdate = { name ->
                val id = collectionCommandState.selectedItemId ?: return@RenameDialog
                collectionItemViewModel.updateCommand(id, name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }
}
