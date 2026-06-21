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
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.adb_commander_icon
import adbcommander.composeapp.generated.resources.app_name
import com.ch0pp4.adbcommander.local.database.DatabaseFactory
import com.ch0pp4.adbcommander.di.AppContainer
import com.ch0pp4.adbcommander.preference.AppPreferences
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.ui.AppMenuBar
import com.ch0pp4.adbcommander.ui.CollectionCommandLayout
import com.ch0pp4.adbcommander.ui.LeftTabLayout
import com.ch0pp4.adbcommander.ui.theme.AdbCommanderTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    DatabaseFactory.init()
    val windowState = rememberWindowState(size = DpSize(width = 1200.dp, height = 700.dp))
    val appContainer = remember { AppContainer() }
    val appPreferences = remember { AppPreferences() }

    Window(
        onCloseRequest = {
            appContainer.clear()
            exitApplication()
        },
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.adb_commander_icon),
        state = windowState,
    ) {
        val collectionViewModel = appContainer.collectionViewModel
        val collectionCommandViewModel = appContainer.collectionItemViewModel
        var selectedCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }
        val userCollections by collectionViewModel.collections.collectAsState()
        var hiddenCollectionIds by remember { mutableStateOf(appPreferences.getHiddenCollectionIds()) }

        LaunchedEffect(userCollections) {
            selectedCollection = userCollections.find { it.id == selectedCollection?.id }
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
        )

        AdbCommanderTheme {
            var leftPanelWidth by remember { mutableStateOf(appPreferences.getLeftPanelWidth().dp) }
            val density = LocalDensity.current

            Surface(modifier = Modifier.fillMaxSize()) {
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
