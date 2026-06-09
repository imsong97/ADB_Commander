package com.ch0pp4.adbcommander.local.database

import com.ch0pp4.adbcommander.data.model.BroadcastExtra
import com.ch0pp4.adbcommander.data.model.ExtraType
import com.ch0pp4.adbcommander.data.model.IntentCommandType
import com.ch0pp4.adbcommander.data.model.SavedCommand
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class SavedCommandEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SavedCommandEntity>(SavedCommandTable)

    var title      by SavedCommandTable.title
    var command    by SavedCommandTable.command
    var sourceTab  by SavedCommandTable.sourceTab
    var intentType by SavedCommandTable.intentType
    var isDefault  by SavedCommandTable.isDefault
    var createdAt  by SavedCommandTable.createdAt
    val extras     by SavedCommandExtraEntity referrersOn SavedCommandExtraTable.savedCommandId
}

class SavedCommandExtraEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SavedCommandExtraEntity>(SavedCommandExtraTable)

    var savedCommand by SavedCommandEntity referencedOn SavedCommandExtraTable.savedCommandId
    var extraType    by SavedCommandExtraTable.extraType
    var extra        by SavedCommandExtraTable.extra
    var value        by SavedCommandExtraTable.value
}

fun SavedCommandEntity.toDataModel() = SavedCommand(
    id = id.value,
    title = title,
    command = command,
    sourceTab = sourceTab.name,
    intentType = when (intentType) {
        IntentType.BROADCAST -> IntentCommandType.BROADCAST
        IntentType.START -> IntentCommandType.START
        IntentType.STARTSERVICE -> IntentCommandType.START_SERVICE
        else -> IntentCommandType.BROADCAST
    },
    isDefault = isDefault,
    extras = extras.map { extra ->
        BroadcastExtra(
            type = ExtraType.valueOf(extra.extraType),
            extra = extra.extra,
            value = extra.value,
        )
    },
)
