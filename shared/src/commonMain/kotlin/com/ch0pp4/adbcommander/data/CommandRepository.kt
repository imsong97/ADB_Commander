package com.ch0pp4.adbcommander.data

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
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
}
