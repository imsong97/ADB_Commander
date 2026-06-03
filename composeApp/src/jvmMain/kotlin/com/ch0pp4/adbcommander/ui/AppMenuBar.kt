package com.ch0pp4.adbcommander.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.*
import com.ch0pp4.adbcommander.presentation.model.MainTab
import org.jetbrains.compose.resources.stringResource

@Composable
fun FrameWindowScope.AppMenuBar(
    visibleTabs: Set<MainTab>,
    onTabVisibilityChange: (tab: MainTab, visible: Boolean) -> Unit,
) {
    MenuBar {
        Menu(stringResource(Res.string.menu_view)) {
            MainTab.entries.forEach { tab ->
                CheckboxItem(
                    text = when (tab) {
                        MainTab.SEND_BROADCAST -> stringResource(Res.string.tab_send_broadcast)
                        MainTab.COMMAND_LIST -> stringResource(Res.string.tab_command)
                    },
                    checked = tab in visibleTabs,
                    onCheckedChange = { onTabVisibilityChange(tab, it) },
                )
            }
        }
    }
}
