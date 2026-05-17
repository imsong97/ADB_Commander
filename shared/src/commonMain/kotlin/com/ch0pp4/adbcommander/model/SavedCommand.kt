package com.ch0pp4.adbcommander.model

data class SavedCommand(
    val id: Int,
    val title: String,
    val command: String,
    val sourceTab: String,
    val intentType: IntentCommandType,
    val isDefault: Boolean,
    val extras: List<BroadcastExtra> = emptyList(),
)
