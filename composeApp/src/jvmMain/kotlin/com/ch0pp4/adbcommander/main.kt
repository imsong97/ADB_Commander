package com.ch0pp4.adbcommander

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModelStore
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.adb_commander_icon
import adbcommander.composeapp.generated.resources.app_name
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ch0pp4.adbcommander.local.database.DatabaseFactory
import com.ch0pp4.adbcommander.di.AppContainer
import com.ch0pp4.adbcommander.preference.AppPreferences
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.CollectionCommandViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.ui.AdbCommandLayout
import com.ch0pp4.adbcommander.ui.AppMenuBar
import com.ch0pp4.adbcommander.ui.CollectionCommandLayout
import com.ch0pp4.adbcommander.ui.LeftTabLayout
import com.ch0pp4.adbcommander.ui.SendBroadcastCommandLayout
import com.ch0pp4.adbcommander.ui.theme.AdbCommanderTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    DatabaseFactory.init()
    val windowState = rememberWindowState(size = DpSize(width = 1200.dp, height = 700.dp))
    val viewModelStore = remember { ViewModelStore() }
    val appContainer = remember { AppContainer() }
    val appPreferences = remember { AppPreferences() }

    Window(
        onCloseRequest = {
            exitApplication()
            viewModelStore.clear()
        },
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.adb_commander_icon),
        state = windowState,
    ) {
        val adbViewModel: AdbViewModel = viewModel { appContainer.adbViewModel }
        val broadcastViewModel: SendBroadcastViewModel = viewModel { appContainer.sendBroadcastViewModel }
        val collectionViewModel: CollectionViewModel = viewModel { appContainer.collectionViewModel }
        val collectionCommandViewModel: CollectionCommandViewModel = viewModel { appContainer.collectionCommandViewModel }
        var selectedTab by remember { mutableStateOf(MainTab.SEND_BROADCAST) }
        var selectedCollection by remember { mutableStateOf<UserCollectionUiModel?>(null) }
        var visibleTabs by remember { mutableStateOf(appPreferences.getVisibleTabs()) }
        val userCollections by collectionViewModel.collections.collectAsState()
        var hiddenCollectionIds by remember { mutableStateOf(setOf<Int>()) }

        LaunchedEffect(userCollections) {
            selectedCollection = userCollections.find { it.id == selectedCollection?.id }
        }

        AppMenuBar(
            visibleTabs = visibleTabs,
            userCollections = userCollections,
            hiddenCollectionIds = hiddenCollectionIds,
            onTabVisibilityChange = { tab, checked ->
                val newVisible = if (checked) visibleTabs + tab else visibleTabs - tab
                visibleTabs = newVisible
                appPreferences.setTabVisible(tab, checked)
                if (selectedTab !in newVisible) {
                    newVisible.firstOrNull()?.let { newTab ->
                        when (selectedTab) {
                            MainTab.SEND_BROADCAST -> broadcastViewModel.clearSelection()
                            MainTab.COMMAND_LIST -> adbViewModel.clearSelection()
                        }
                        selectedTab = newTab
                    }
                }
            },
            onCollectionVisibilityChange = { id, visible ->
                hiddenCollectionIds = if (visible) hiddenCollectionIds - id else hiddenCollectionIds + id
                if (!visible && selectedCollection?.id == id) {
                    selectedCollection = null
                    collectionCommandViewModel.onReset()
                }
            },
        )

        AdbCommanderTheme {
            var leftPanelWidth by remember { mutableStateOf(220.dp) }
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
                        selectedTab = selectedTab,
                        selectedCollection = selectedCollection,
                        visibleTabs = visibleTabs,
                        hiddenCollectionIds = hiddenCollectionIds,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            selectedCollection = null
                        },
                        onCollectionSelected = { collection ->
                            selectedCollection = collection
                            if (collection != null) {
                                collectionCommandViewModel.setCollection(collection.id)
                            }
                        },
                        adbViewModel = adbViewModel,
                        broadcastViewModel = broadcastViewModel,
                        collectionViewModel = collectionViewModel,
                        collectionCommandViewModel = collectionCommandViewModel,
                    )

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    val newWidth = leftPanelWidth + with(density) { delta.toDp() }
                                    leftPanelWidth = newWidth.coerceIn(150.dp, 400.dp)
                                }
                            ),
                    )

                    when {
                        visibleTabs.isEmpty() && selectedCollection == null -> Box(modifier = Modifier.fillMaxSize())
                        selectedCollection != null -> CollectionCommandLayout(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = collectionCommandViewModel,
                        )
                        else -> when (selectedTab) {
                            MainTab.SEND_BROADCAST -> SendBroadcastCommandLayout(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = broadcastViewModel,
                            )
                            MainTab.COMMAND_LIST -> AdbCommandLayout(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = adbViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
