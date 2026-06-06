package com.ch0pp4.adbcommander

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import adbcommander.composeapp.generated.resources.app_name
import com.ch0pp4.adbcommander.local.database.DatabaseFactory
import com.ch0pp4.adbcommander.di.AppContainer
import com.ch0pp4.adbcommander.preference.AppPreferences
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.ui.AdbCommandLayout
import com.ch0pp4.adbcommander.ui.AppMenuBar
import com.ch0pp4.adbcommander.ui.LeftTabLayout
import com.ch0pp4.adbcommander.ui.SendBroadcastCommandLayout
import com.ch0pp4.adbcommander.ui.theme.AdbCommanderTheme
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
        state = windowState,
    ) {
        val adbViewModel: AdbViewModel = remember(key1 = viewModelStore, key2 = appContainer) {
            appContainer.getAdbViewModel(viewModelStore)
        }
        val broadcastViewModel: SendBroadcastViewModel = remember(key1 = viewModelStore, key2 = appContainer) {
            appContainer.getSendBroadcastViewModel(viewModelStore)
        }
        var selectedTab by remember { mutableStateOf(MainTab.SEND_BROADCAST) }
        var visibleTabs by remember { mutableStateOf(appPreferences.getVisibleTabs()) }

        AppMenuBar(
            visibleTabs = visibleTabs,
            onTabVisibilityChange = { tab, checked ->
                val newVisible = if (checked) visibleTabs + tab else visibleTabs - tab
                visibleTabs = newVisible
                appPreferences.setTabVisible(tab, checked)
                if (!checked && selectedTab == tab) {
                    newVisible.firstOrNull()?.let { selectedTab = it }
                }
            },
        )

        AdbCommanderTheme {
            var leftPanelWidth by remember { mutableStateOf(220.dp) }
            val density = LocalDensity.current

            Surface(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    LeftTabLayout(
                        modifier = Modifier.width(leftPanelWidth).fillMaxHeight(),
                        selectedTab = selectedTab,
                        visibleTabs = visibleTabs,
                        onTabSelected = { selectedTab = it },
                        adbViewModel = adbViewModel,
                        broadcastViewModel = broadcastViewModel,
                    )

                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    val newWidth = leftPanelWidth + with(density) { delta.toDp() }
                                    leftPanelWidth = newWidth.coerceIn(150.dp, 400.dp)
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        VerticalDivider()
                    }

                    if (visibleTabs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        when (selectedTab) {
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
