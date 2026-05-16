package com.ch0pp4.adbcommander

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.stringResource
import adbcommander.composeapp.generated.resources.Res
import adbcommander.composeapp.generated.resources.*

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {

    }
}