package com.ch0pp4.adbcommander.datastore

import com.ch0pp4.adbcommander.model.BroadcastExtra
import com.ch0pp4.adbcommander.model.IntentCommandType
import com.ch0pp4.adbcommander.model.SavedCommand

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
