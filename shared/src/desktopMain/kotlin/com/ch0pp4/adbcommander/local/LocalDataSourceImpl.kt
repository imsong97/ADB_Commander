package com.ch0pp4.adbcommander.local

import com.ch0pp4.adbcommander.local.database.IntentType
import com.ch0pp4.adbcommander.local.database.SavedCommandEntity
import com.ch0pp4.adbcommander.local.database.SavedCommandExtraEntity
import com.ch0pp4.adbcommander.local.database.SavedCommandTable
import com.ch0pp4.adbcommander.local.database.SourceTab
import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.ExtraType
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.data.model.SavedCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class LocalDataSourceImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocalDataSource {

    override suspend fun save(
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = withContext(context = dispatcher) {
        transaction {
            val entity = SavedCommandEntity.new {
                this.title = title
                this.command = command
                this.sourceTab = when (intentType) {
                    null -> SourceTab.COMMAND
                    else -> SourceTab.BROADCAST
                }
                this.intentType = when (intentType) {
                    IntentCommandType.BROADCAST -> IntentType.BROADCAST
                    IntentCommandType.START -> IntentType.START
                    IntentCommandType.START_SERVICE -> IntentType.STARTSERVICE
                    else -> IntentType.NONE
                }
                this.createdAt = LocalDateTime.now()
            }

            extras.forEach { extra ->
                SavedCommandExtraEntity.new {
                    this.savedCommand = entity
                    this.extraType = extra.type.name
                    this.extra = extra.extra
                    this.value = extra.value
                }
            }

            entity.id.value
        }
    }

    override suspend fun getByTab(sourceTab: String): List<SavedCommand> = withContext(context = dispatcher) {
        transaction {
            val tab = SourceTab.valueOf(value = sourceTab)
            SavedCommandEntity
                .find { SavedCommandTable.sourceTab eq tab }
                .orderBy(SavedCommandTable.createdAt to SortOrder.ASC)
                .map { entity ->
                    SavedCommand(
                        id = entity.id.value,
                        title = entity.title,
                        command = entity.command,
                        sourceTab = entity.sourceTab.name,
                        intentType = when (entity.intentType) {
                            IntentType.BROADCAST -> IntentCommandType.BROADCAST
                            IntentType.START -> IntentCommandType.START
                            IntentType.STARTSERVICE -> IntentCommandType.START_SERVICE
                            else -> IntentCommandType.BROADCAST
                        },
                        isDefault = entity.isDefault,
                        extras = entity.extras.map { extra ->
                            BroadcastExtra(
                                type = ExtraType.valueOf(extra.extraType),
                                extra = extra.extra,
                                value = extra.value,
                            )
                        },
                    )
                }
        }
    }

    override suspend fun deleteById(id: Int): Int = withContext(context = dispatcher) {
        transaction {
            val entity = SavedCommandEntity.findById(id) ?: return@transaction 0
            entity.extras.forEach { it.delete() }
            entity.delete()
            1
        }
    }

    override suspend fun updateTitle(id: Int, title: String): Int = withContext(context = dispatcher) {
        transaction {
            val entity = SavedCommandEntity.findById(id) ?: return@transaction 0
            entity.title = title
            1
        }
    }
}
