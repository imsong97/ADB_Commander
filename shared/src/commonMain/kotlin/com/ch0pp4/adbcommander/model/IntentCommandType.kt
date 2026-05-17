package com.ch0pp4.adbcommander.model

enum class IntentCommandType(
    val label: String,
    val adbCommand: String,
    val actionFlag: String,
) {
    BROADCAST("broadcast", "am broadcast", "-a"),
    START("start", "am start", "-n"),
    START_SERVICE("start service", "am startservice", "-n"),
}
