package com.ch0pp4.adbcommander.data.model

data class CollectionSavedCommand(
    val id: Int,
    val collectionId: Int,
    val title: String,
    val command: String,
    val intentType: IntentCommandType?,
    val extras: List<BroadcastExtra> = emptyList(),
)
