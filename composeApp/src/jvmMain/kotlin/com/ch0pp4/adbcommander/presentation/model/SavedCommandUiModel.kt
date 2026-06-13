package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.CollectionSavedCommand
import com.ch0pp4.adbcommander.data.model.SavedCommand

data class SavedCommandUiModel(
    val id: Int,
    val title: String,
    val command: String,
    val intentType: IntentCommandType,
    val extras: List<BroadcastExtraUiModel> = emptyList(),
)

fun SavedCommand.toPresentation() = SavedCommandUiModel(
    id = id,
    title = title,
    command = command,
    intentType = intentType.toPresentation(),
    extras = extras.map { it.toPresentation() },
)

fun CollectionSavedCommand.toPresentation() = SavedCommandUiModel(
    id = id,
    title = title,
    command = command,
    intentType = intentType?.toPresentation() ?: IntentCommandType.ADB,
    extras = extras.map { it.toPresentation() },
)
