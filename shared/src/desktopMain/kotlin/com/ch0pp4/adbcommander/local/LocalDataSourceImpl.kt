package com.ch0pp4.adbcommander.local

import com.ch0pp4.adbcommander.local.database.CollectionEntity
import com.ch0pp4.adbcommander.local.database.CollectionSavedCommandEntity
import com.ch0pp4.adbcommander.local.database.CollectionSavedCommandExtraEntity
import com.ch0pp4.adbcommander.local.database.CollectionSavedCommandTable
import com.ch0pp4.adbcommander.local.database.CollectionTable
import com.ch0pp4.adbcommander.local.database.toDataModel
import com.ch0pp4.adbcommander.local.database.`toEntity()`
import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.Collection
import com.ch0pp4.adbcommander.data.model.CollectionSavedCommand
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.local.database.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class LocalDataSourceImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocalDataSource {

    override suspend fun saveCollection(name: String): Int = withContext(context = dispatcher) {
        transaction {
            CollectionEntity.all().forEach { it.sortOrder += 1 }
            CollectionEntity.new {
                this.name = name
                this.createdAt = LocalDateTime.now()
                this.sortOrder = 0
            }.id.value
        }
    }

    override suspend fun getAllCollections(): List<Collection> = withContext(context = dispatcher) {
        transaction {
            CollectionEntity
                .all()
                .orderBy(CollectionTable.sortOrder to SortOrder.ASC)
                .map { it.toDataModel() }
        }
    }

    override suspend fun reorderCollections(orderedIds: List<Int>) = withContext(context = dispatcher) {
        transaction {
            orderedIds.forEachIndexed { index, id ->
                CollectionEntity.findById(id)?.sortOrder = index
            }
        }
    }

    override suspend fun renameCollection(id: Int, name: String): Int = withContext(context = dispatcher) {
        transaction {
            val entity = CollectionEntity.findById(id) ?: return@transaction 0
            entity.name = name
            1
        }
    }

    override suspend fun deleteCollection(id: Int): Int = withContext(context = dispatcher) {
        transaction {
            CollectionSavedCommandEntity
                .find { CollectionSavedCommandTable.collectionId eq id }
                .forEach { cmd ->
                    cmd.extras.forEach { it.delete() }
                    cmd.delete()
                }
            CollectionEntity.findById(id)?.delete()
            1
        }
    }

    override suspend fun saveCollectionCommand(
        collectionId: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = withContext(context = dispatcher) {
        transaction {
            val entity = CollectionSavedCommandEntity.new {
                this.collectionId = CollectionEntity[collectionId].id
                this.title = title
                this.command = command
                this.intentType = intentType.toEntity()
                this.createdAt = LocalDateTime.now()
            }
            extras.forEach { extra ->
                CollectionSavedCommandExtraEntity.new {
                    this.savedCommand = entity
                    this.extraType = extra.type.name
                    this.extra = extra.extra
                    this.value = extra.value
                }
            }
            entity.id.value
        }
    }

    override suspend fun getByCollection(collectionId: Int): List<CollectionSavedCommand> = withContext(context = dispatcher) {
        transaction {
            CollectionSavedCommandEntity
                .find { CollectionSavedCommandTable.collectionId eq collectionId }
                .orderBy(CollectionSavedCommandTable.createdAt to SortOrder.ASC)
                .map { it.toDataModel() }
        }
    }

    override suspend fun deleteCollectionCommand(id: Int): Int = withContext(context = dispatcher) {
        transaction {
            val entity = CollectionSavedCommandEntity.findById(id) ?: return@transaction 0
            entity.extras.forEach { it.delete() }
            entity.delete()
            1
        }
    }

    override suspend fun renameCollectionCommand(id: Int, title: String): Int = withContext(context = dispatcher) {
        transaction {
            val entity = CollectionSavedCommandEntity.findById(id) ?: return@transaction 0
            entity.title = title
            1
        }
    }

    override suspend fun updateCollectionCommand(
        id: Int,
        title: String,
        command: String,
        intentType: IntentCommandType?,
        extras: List<BroadcastExtra>,
    ): Int = withContext(context = dispatcher) {
        transaction {
            val entity = CollectionSavedCommandEntity.findById(id) ?: return@transaction 0
            entity.title = title
            entity.command = command
            entity.intentType = intentType.toEntity()
            entity.extras.forEach { it.delete() }
            extras.forEach { extra ->
                CollectionSavedCommandExtraEntity.new {
                    this.savedCommand = entity
                    this.extraType = extra.type.name
                    this.extra = extra.extra
                    this.value = extra.value
                }
            }
            1
        }
    }
}
