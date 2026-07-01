package com.ch0pp4.adbcommander.local.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

enum class IntentType { NONE, BROADCAST, START, STARTSERVICE } // for send broadcast

object CollectionTable : IntIdTable(name = "collection") {
    val name = varchar(name = "name", length = 255)
    val createdAt = datetime(name = "created_at")
    val sortOrder = integer(name = "sort_order").default(0)
}

object CollectionSavedCommandTable : IntIdTable(name = "collection_saved_command") {
    val collectionId = reference(name = "collection_id", foreign = CollectionTable)
    val title = varchar(name = "title", length = 255)
    val command = text(name = "command")
    val intentType = enumerationByName(name = "intent_type", length = 20, klass = IntentType::class).default(defaultValue = IntentType.NONE)
    val createdAt = datetime(name = "created_at")
}

object CollectionSavedCommandExtraTable : IntIdTable(name = "collection_saved_command_extra") {
    val savedCommandId = reference(name = "saved_command_id", foreign = CollectionSavedCommandTable)
    val extraType = varchar(name = "extra_type", length = 20)
    val extra = varchar(name = "extra", length = 255)
    val value = varchar(name = "value", length = 255)
}
