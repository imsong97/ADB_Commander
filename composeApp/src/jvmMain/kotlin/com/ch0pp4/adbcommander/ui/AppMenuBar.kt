package com.ch0pp4.adbcommander.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.*
import com.ch0pp4.adbcommander.presentation.model.MainTab
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun FrameWindowScope.AppMenuBar(
    visibleTabs: Set<MainTab>,
    userCollections: List<UserCollectionUiModel>,
    hiddenCollectionIds: Set<Int>,
    onTabVisibilityChange: (tab: MainTab, visible: Boolean) -> Unit,
    onCollectionVisibilityChange: (id: Int, visible: Boolean) -> Unit,
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
            if (userCollections.isNotEmpty()) {
                Separator()
                userCollections.forEach { collection ->
                    CheckboxItem(
                        text = collection.name,
                        checked = collection.id !in hiddenCollectionIds,
                        onCheckedChange = { onCollectionVisibilityChange(collection.id, it) },
                    )
                }
            }
        }
    }
}
