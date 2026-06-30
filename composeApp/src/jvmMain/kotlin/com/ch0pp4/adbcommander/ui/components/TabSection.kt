package com.ch0pp4.adbcommander.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.dialog_delete_message
import adbcommander.composeapp.generated.resources.dialog_delete_title
import adbcommander.composeapp.generated.resources.dialog_rename_title
import adbcommander.composeapp.generated.resources.edit_command_title
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.ui.theme.LightSidebarSelected
import org.jetbrains.compose.resources.stringResource

@Composable
fun TabSection(
    label: String,
    expanded: Boolean,
    items: List<SavedCommandUiModel>,
    selectedItemId: Int?,
    onHeaderClick: () -> Unit,
    onToggleClick: () -> Unit,
    onItemSelected: (SavedCommandUiModel) -> Unit,
    onItemDeleted: (SavedCommandUiModel) -> Unit,
    onItemRenamed: (SavedCommandUiModel, String) -> Unit,
    headerModifier: Modifier = Modifier,
    onRenameClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    TreeTabHeader(
        label = label,
        expanded = expanded,
        onClick = onHeaderClick,
        onToggleClick = onToggleClick,
        modifier = headerModifier,
        onRenameClick = onRenameClick,
        onDeleteClick = onDeleteClick,
    )
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column {
            items.forEach { item ->
                SavedItemRow(
                    item = item,
                    isSelected = item.id == selectedItemId,
                    onSelected = { onItemSelected(item) },
                    onDeleted = { onItemDeleted(item) },
                    onRenamed = { onItemRenamed(item, it) },
                )
            }
        }
    }
}

@Composable
fun TreeTabHeader(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null,
    onRenameClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = onToggleClick,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = rotation },
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
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
        if (onRenameClick != null) {
            IconButton(
                onClick = onRenameClick,
                modifier = Modifier.size(28.dp).alpha(if (isHovered) 1f else 0f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
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
fun SavedItemRow(
    item: SavedCommandUiModel,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onDeleted: () -> Unit,
    onRenamed: (String) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
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
            onClick = { showRenameDialog = true },
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
        RenameDialog(
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
