package com.ch0pp4.adbcommander.local

import com.ch0pp4.adbcommander.local.database.IntentType
import com.ch0pp4.adbcommander.local.database.SavedCommandExtraTable
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
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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
            val id = SavedCommandTable.insert {
                it[SavedCommandTable.title] = title
                it[SavedCommandTable.command] = command
                it[SavedCommandTable.sourceTab] = when (intentType) {
                    null -> SourceTab.COMMAND
                    else -> SourceTab.BROADCAST
                }
                it[SavedCommandTable.intentType] = when (intentType) {
                    IntentCommandType.BROADCAST -> IntentType.BROADCAST
                    IntentCommandType.START -> IntentType.START
                    IntentCommandType.START_SERVICE -> IntentType.STARTSERVICE
                    else -> IntentType.NONE
                }
                it[SavedCommandTable.createdAt] = LocalDateTime.now()
            }[SavedCommandTable.id].value

            extras.forEach { extra ->
                SavedCommandExtraTable.insert {
                    it[SavedCommandExtraTable.savedCommandId] = id
                    it[SavedCommandExtraTable.extraType] = extra.type.name
                    it[SavedCommandExtraTable.extra] = extra.extra
                    it[SavedCommandExtraTable.value] = extra.value
                }
            }

            id
        }
    }

    override suspend fun getByTab(sourceTab: String): List<SavedCommand> = withContext(context = dispatcher) {
        transaction {
            val tab = SourceTab.valueOf(value = sourceTab)
            val commands = SavedCommandTable
                .selectAll()
                .where { SavedCommandTable.sourceTab eq tab }
                .orderBy(column = SavedCommandTable.createdAt, order = SortOrder.ASC)
                .map {
                    SavedCommand(
                        id = it[SavedCommandTable.id].value,
                        title = it[SavedCommandTable.title],
                        command = it[SavedCommandTable.command],
                        sourceTab = it[SavedCommandTable.sourceTab].name,
                        intentType = when (it[SavedCommandTable.intentType]) {
                            IntentType.BROADCAST -> IntentCommandType.BROADCAST
                            IntentType.START -> IntentCommandType.START
                            IntentType.STARTSERVICE -> IntentCommandType.START_SERVICE
                            else -> IntentCommandType.BROADCAST
                        },
                        isDefault = it[SavedCommandTable.isDefault],
                    )
                }

            commands.map { cmd ->
                val extras = SavedCommandExtraTable
                    .selectAll()
                    .where { SavedCommandExtraTable.savedCommandId eq cmd.id }
                    .map {
                        BroadcastExtra(
                            type = ExtraType.valueOf(value = it[SavedCommandExtraTable.extraType]),
                            extra = it[SavedCommandExtraTable.extra],
                            value = it[SavedCommandExtraTable.value],
                        )
                    }
                cmd.copy(extras = extras)
            }
        }
    }

    override suspend fun deleteById(id: Int): Int = withContext(context = dispatcher) {
        transaction {
            SavedCommandExtraTable.deleteWhere { SavedCommandExtraTable.savedCommandId eq id }
            SavedCommandTable.deleteWhere { SavedCommandTable.id eq id }
        }
    }

    override suspend fun updateTitle(id: Int, title: String): Int = withContext(context = dispatcher) {
        transaction {
            SavedCommandTable.update({ SavedCommandTable.id eq id }) {
                it[SavedCommandTable.title] = title
            }
        }
    }
}
