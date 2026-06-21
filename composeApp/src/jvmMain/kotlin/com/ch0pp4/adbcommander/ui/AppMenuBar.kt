package com.ch0pp4.adbcommander.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.menu_view
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun FrameWindowScope.AppMenuBar(
    userCollections: List<UserCollectionUiModel>,
    hiddenCollectionIds: Set<Int>,
    onCollectionVisibilityChange: (id: Int, visible: Boolean) -> Unit,
) {
    MenuBar {
        Menu(stringResource(Res.string.menu_view)) {
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
