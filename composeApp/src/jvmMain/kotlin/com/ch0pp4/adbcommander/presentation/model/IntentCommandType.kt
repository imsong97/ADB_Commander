package com.ch0pp4.adbcommander.presentation.model

enum class IntentCommandType(
    val label: String,
    val adbCommand: String,
    val actionFlag: String,
    val actionFlagHint: String,
) {
    BROADCAST("broadcast", "am broadcast", "-a", "com.example.ACTION_NAME"),
    START("start", "am start", "-n", "com.example/.MainActivity"),
    START_SERVICE("start service", "am startservice", "-n", "com.example/.MyService"),
}
