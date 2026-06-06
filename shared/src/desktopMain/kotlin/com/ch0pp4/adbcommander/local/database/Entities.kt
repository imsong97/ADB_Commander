package com.ch0pp4.adbcommander.local.database

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
