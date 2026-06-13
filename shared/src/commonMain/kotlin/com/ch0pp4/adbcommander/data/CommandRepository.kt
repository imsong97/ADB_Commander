package com.ch0pp4.adbcommander.data

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.Collection
import com.ch0pp4.adbcommander.data.model.CollectionSavedCommand
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.data.model.SavedCommand

interface CommandRepository {
    suspend fun saveBroadcastCommand(
        title: String,
        command: String,
        intentType: IntentCommandType,
        extras: List<BroadcastExtra> = emptyList(),
    ): Int
    suspend fun saveADBCommand(title: String, command: String, extras: List<BroadcastExtra> = emptyList()): Int
    suspend fun getByTab(sourceTab: String): List<SavedCommand>
    suspend fun deleteById(id: Int): Int
    suspend fun renameCommand(id: Int, title: String): Int
    suspend fun updateCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType? = null,
        extras: List<BroadcastExtra> = emptyList(),
    ): Int
    suspend fun saveCollection(name: String): Int
    suspend fun getAllCollections(): List<Collection>
    suspend fun deleteCollection(id: Int): Int
    suspend fun renameCollection(id: Int, name: String): Int
    suspend fun saveCollectionCommand(
        collectionId: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int
    suspend fun getByCollection(collectionId: Int): List<CollectionSavedCommand>
    suspend fun deleteCollectionCommand(id: Int): Int
    suspend fun renameCollectionCommand(id: Int, title: String): Int
    suspend fun updateCollectionCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int
}
