package com.ch0pp4.adbcommander.local.database

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.Collection
import com.ch0pp4.adbcommander.data.model.SavedCommandModel
import com.ch0pp4.adbcommander.data.model.ExtraType
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CollectionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionEntity>(CollectionTable)

    var name      by CollectionTable.name
    var createdAt by CollectionTable.createdAt
    var sortOrder by CollectionTable.sortOrder
}

fun CollectionEntity.toDataModel() = Collection(id = id.value, name = name, sortOrder = sortOrder)

class CollectionSavedCommandEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionSavedCommandEntity>(CollectionSavedCommandTable)

    var collectionId by CollectionSavedCommandTable.collectionId
    var title        by CollectionSavedCommandTable.title
    var command      by CollectionSavedCommandTable.command
    var intentType   by CollectionSavedCommandTable.intentType
    var createdAt    by CollectionSavedCommandTable.createdAt
    val extras       by CollectionSavedCommandExtraEntity referrersOn CollectionSavedCommandExtraTable.savedCommandId
}

class CollectionSavedCommandExtraEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionSavedCommandExtraEntity>(CollectionSavedCommandExtraTable)

    var savedCommand by CollectionSavedCommandEntity referencedOn CollectionSavedCommandExtraTable.savedCommandId
    var extraType    by CollectionSavedCommandExtraTable.extraType
    var extra        by CollectionSavedCommandExtraTable.extra
    var value        by CollectionSavedCommandExtraTable.value
}

fun IntentCommandType?.toEntity(): IntentType = when (this) {
    IntentCommandType.BROADCAST -> IntentType.BROADCAST
    IntentCommandType.START -> IntentType.START
    IntentCommandType.START_SERVICE -> IntentType.STARTSERVICE
    else -> IntentType.NONE
}

fun CollectionSavedCommandEntity.toDataModel() = SavedCommandModel(
    id = id.value,
    collectionId = collectionId.value,
    title = title,
    command = command,
    intentType = when (intentType) {
        IntentType.NONE -> null
        IntentType.BROADCAST -> IntentCommandType.BROADCAST
        IntentType.START -> IntentCommandType.START
        IntentType.STARTSERVICE -> IntentCommandType.START_SERVICE
    },
    extras = extras.map { extra ->
        BroadcastExtra(
            type = ExtraType.valueOf(extra.extraType),
            extra = extra.extra,
            value = extra.value,
        )
    },
)
