package com.ch0pp4.adbcommander

import com.ch0pp4.adbcommander.datastore.LocalDataSource
import com.ch0pp4.adbcommander.model.BroadcastExtra
import com.ch0pp4.adbcommander.model.IntentCommandType
import com.ch0pp4.adbcommander.model.SavedCommand

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
}
