package com.ch0pp4.adbcommander

import com.ch0pp4.adbcommander.model.BroadcastExtra
import com.ch0pp4.adbcommander.model.IntentCommandType
import com.ch0pp4.adbcommander.model.SavedCommand

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
}
