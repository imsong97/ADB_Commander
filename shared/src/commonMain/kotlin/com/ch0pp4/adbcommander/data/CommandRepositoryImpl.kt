package com.ch0pp4.adbcommander.data

import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.data.model.BroadcastExtra
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
}
