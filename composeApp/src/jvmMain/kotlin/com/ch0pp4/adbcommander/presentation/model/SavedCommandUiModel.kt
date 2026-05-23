package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.SavedCommand

data class SavedCommandUiModel(
    val id: Int,
    val title: String,
    val command: String,
    val intentType: IntentCommandType,
    val isDefault: Boolean,
    val extras: List<BroadcastExtraUiModel> = emptyList(),
)

fun SavedCommand.toPresentation() = SavedCommandUiModel(
    id = id,
    title = title,
    command = command,
    isDefault = isDefault,
    intentType = intentType.toPresentation(),
    extras = extras.map { it.toPresentation() },
)
