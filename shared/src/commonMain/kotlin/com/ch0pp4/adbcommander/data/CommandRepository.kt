package com.ch0pp4.adbcommander.data

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.Collection
import com.ch0pp4.adbcommander.data.model.SavedCommandModel
import com.ch0pp4.adbcommander.data.model.IntentCommandType

interface CommandRepository {
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
    suspend fun getByCollection(collectionId: Int): List<SavedCommandModel>
    suspend fun deleteCollectionCommand(id: Int): Int
    suspend fun reorderCollections(orderedIds: List<Int>)
    suspend fun renameCollectionCommand(id: Int, title: String): Int
    suspend fun updateCollectionCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int
}
