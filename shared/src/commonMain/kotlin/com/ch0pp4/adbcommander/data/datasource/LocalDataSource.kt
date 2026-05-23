package com.ch0pp4.adbcommander.data.datasource

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.data.model.SavedCommand

interface LocalDataSource {
    suspend fun save(
        title: String,
        command: String,
        intentType: IntentCommandType? = null,
        extras: List<BroadcastExtra>
    ): Int
    suspend fun getByTab(sourceTab: String): List<SavedCommand>
    suspend fun deleteById(id: Int): Int
}
