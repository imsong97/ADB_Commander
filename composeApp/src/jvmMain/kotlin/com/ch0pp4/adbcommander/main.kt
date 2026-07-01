package com.ch0pp4.adbcommander

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Cursor
import java.awt.FileDialog
import java.io.File
import androidx.lifecycle.ViewModelStore
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.adb_commander_icon
import adbcommander.composeapp.generated.resources.app_name
import adbcommander.composeapp.generated.resources.export_default_filename
import adbcommander.composeapp.generated.resources.export_dialog_title
import adbcommander.composeapp.generated.resources.toast_export_failure
import adbcommander.composeapp.generated.resources.toast_export_success
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ch0pp4.adbcommander.local.database.DatabaseFactory
import com.ch0pp4.adbcommander.di.AppContainer
import com.ch0pp4.adbcommander.presentation.CollectionItemViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.model.ExportResult
import com.ch0pp4.adbcommander.presentation.model.CollectionUiModel
import com.ch0pp4.adbcommander.ui.AppMenuBar
import com.ch0pp4.adbcommander.ui.CollectionCommandLayout
import com.ch0pp4.adbcommander.ui.LeftTabLayout
import com.ch0pp4.adbcommander.ui.components.ToastHost
import com.ch0pp4.adbcommander.ui.components.rememberToastState
import com.ch0pp4.adbcommander.ui.theme.AdbCommanderTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    DatabaseFactory.init()
    val windowState = rememberWindowState(size = DpSize(width = 1200.dp, height = 700.dp))
    val viewModelStore = remember { ViewModelStore() }
    val appContainer = remember { AppContainer() }
    val appPreferences = appContainer.appPreferences

    Window(
        onCloseRequest = {
            exitApplication()
            viewModelStore.clear()
        },
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.adb_commander_icon),
        state = windowState,
    ) {
        val collectionViewModel: CollectionViewModel = viewModel { appContainer.collectionViewModel }
        val collectionCommandViewModel: CollectionItemViewModel = viewModel { appContainer.collectionCommandViewModel }
        var selectedCollection by remember { mutableStateOf<CollectionUiModel?>(null) }
        val userCollections by collectionViewModel.collections.collectAsState()
        var hiddenCollectionIds by remember { mutableStateOf(appPreferences.getHiddenCollectionIds()) }
        val toastState = rememberToastState()
        val exportSuccessMsg = stringResource(Res.string.toast_export_success)
        val exportFailureMsg = stringResource(Res.string.toast_export_failure)
        val exportDialogTitle = stringResource(Res.string.export_dialog_title)
        val exportDefaultFilename = stringResource(Res.string.export_default_filename)

        LaunchedEffect(userCollections) {
            selectedCollection = userCollections.find { it.id == selectedCollection?.id }
        }

        LaunchedEffect(collectionViewModel) {
            collectionViewModel.exportResult.collect { result ->
                when (result) {
                    ExportResult.Success -> exportSuccessMsg
                    is ExportResult.Failure -> result.message.ifBlank { exportFailureMsg }
                }.also {
                    toastState.show(it)
                }
            }
        }

        AppMenuBar(
            userCollections = userCollections,
            hiddenCollectionIds = hiddenCollectionIds,
            onCollectionVisibilityChange = { id, visible ->
                val newHidden = if (visible) hiddenCollectionIds - id else hiddenCollectionIds + id
                hiddenCollectionIds = newHidden
                appPreferences.setHiddenCollectionIds(newHidden)
                if (!visible && selectedCollection?.id == id) {
                    selectedCollection = null
                    collectionCommandViewModel.onReset()
                }
            },
            onExportClick = {
                val dialog = FileDialog(window, exportDialogTitle, FileDialog.SAVE).apply {
                    file = exportDefaultFilename
                    isVisible = true
                }
                val directory = dialog.directory
                val fileName = dialog.file
                if (directory != null && fileName != null) {
                    val targetFile = File(directory, if (fileName.endsWith(".txt")) fileName else "$fileName.txt")
                    collectionViewModel.exportToFile(targetFile)
                }
            },
        )

        AdbCommanderTheme {
            var leftPanelWidth by remember { mutableStateOf(appPreferences.getLeftPanelWidth().dp) }
            val density = LocalDensity.current

            Surface(modifier = Modifier.fillMaxSize()) {
                ToastHost(state = toastState, modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LeftTabLayout(
                            modifier = Modifier
                                .width(leftPanelWidth)
                                .fillMaxHeight()
                                .padding(top = 8.dp, start = 8.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            selectedCollection = selectedCollection,
                            hiddenCollectionIds = hiddenCollectionIds,
                            initialExpandedCollections = remember { appPreferences.getExpandedCollectionIds() },
                            onCollectionSelected = { collection ->
                                selectedCollection = collection
                                if (collection != null) {
                                    collectionCommandViewModel.setCollection(collection.id)
                                }
                            },
                            onExpandedCollectionsChange = { appPreferences.setExpandedCollectionIds(it) },
                            onCollectionDeleted = { id ->
                                appPreferences.removeExpandedCollectionId(id)
                                appPreferences.removeHiddenCollectionId(id)
                                hiddenCollectionIds = hiddenCollectionIds - id
                            },
                            collectionViewModel = collectionViewModel,
                            collectionItemViewModel = collectionCommandViewModel,
                        )

                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        val newWidth = (leftPanelWidth + with(density) { delta.toDp() }).coerceIn(150.dp, 400.dp)
                                        leftPanelWidth = newWidth
                                    },
                                    onDragStopped = {
                                        appPreferences.setLeftPanelWidth(leftPanelWidth.value)
                                    }
                                ),
                        )

                        if (selectedCollection != null) {
                            CollectionCommandLayout(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = collectionCommandViewModel,
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
