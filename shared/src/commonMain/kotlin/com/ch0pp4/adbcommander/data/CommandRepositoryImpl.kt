package com.ch0pp4.adbcommander.data

import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.Collection
import com.ch0pp4.adbcommander.data.model.CollectionSavedCommand
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.data.model.SavedCommand

class CommandRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : CommandRepository {

    override suspend fun saveBroadcastCommand(
        title: String,
        command: String,
        intentType: IntentCommandType,
        extras: List<BroadcastExtra>,
    ): Int = localDataSource.save(
        title = title,
        command = command,
        intentType = intentType,
        extras = extras
    )

    override suspend fun saveADBCommand(
        title: String,
        command: String,
        extras: List<BroadcastExtra>,
    ): Int = localDataSource.save(
        title = title,
        command = command,
        extras = extras
    )

    override suspend fun getByTab(sourceTab: String): List<SavedCommand> =
        localDataSource.getByTab(sourceTab)

    override suspend fun deleteById(id: Int): Int =
        localDataSource.deleteById(id)

    override suspend fun renameCommand(id: Int, title: String): Int =
        localDataSource.updateTitle(id, title)

    override suspend fun updateCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = localDataSource.updateContent(id, title, command, intentType, extras)

    override suspend fun saveCollection(name: String): Int = localDataSource.saveCollection(name)

    override suspend fun getAllCollections(): List<Collection> = localDataSource.getAllCollections()

    override suspend fun deleteCollection(id: Int): Int = localDataSource.deleteCollection(id)

    override suspend fun saveCollectionCommand(
        collectionId: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = localDataSource.saveCollectionCommand(collectionId, title, command, intentType, extras)

    override suspend fun getByCollection(collectionId: Int): List<CollectionSavedCommand> =
        localDataSource.getByCollection(collectionId)

    override suspend fun deleteCollectionCommand(id: Int): Int =
        localDataSource.deleteCollectionCommand(id)

    override suspend fun renameCollectionCommand(id: Int, title: String): Int =
        localDataSource.renameCollectionCommand(id, title)

    override suspend fun updateCollectionCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = localDataSource.updateCollectionCommand(id, title, command, intentType, extras)
}
