package com.ch0pp4.adbcommander

import com.ch0pp4.adbcommander.database.IntentType
import com.ch0pp4.adbcommander.database.SavedCommandExtraTable
import com.ch0pp4.adbcommander.database.SavedCommandTable
import com.ch0pp4.adbcommander.database.SourceTab
import com.ch0pp4.adbcommander.datastore.LocalDataSource
import com.ch0pp4.adbcommander.model.BroadcastExtra
import com.ch0pp4.adbcommander.model.ExtraType
import com.ch0pp4.adbcommander.model.IntentCommandType
import com.ch0pp4.adbcommander.model.SavedCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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
                it[SavedCommandTable.createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }[SavedCommandTable.id].value

            extras.forEach { extra ->
                SavedCommandExtraTable.insert {
                    it[savedCommandId] = id
                    it[extraType] = extra.type.name
                    it[SavedCommandExtraTable.extra] = extra.extra
                    it[value] = extra.value
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
            SavedCommandExtraTable.deleteWhere { savedCommandId eq id }
            SavedCommandTable.deleteWhere { SavedCommandTable.id eq id }
        }
    }
}
