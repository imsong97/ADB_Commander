package com.ch0pp4.adbcommander

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ADBCommander",
    ) {

    }
}