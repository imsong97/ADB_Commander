package com.ch0pp4.adbcommander.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

enum class SourceTab { BROADCAST, COMMAND }

enum class IntentType { NONE, BROADCAST, START, STARTSERVICE } // for send broadcast

object SavedCommandTable : IntIdTable(name = "saved_command") {
    val title = varchar(name = "title", length = 255)
    val command = text(name = "command")
    val sourceTab = enumerationByName(name = "source_tab", length = 20, klass = SourceTab::class)
    val intentType = enumerationByName(name = "intent_type", length = 20, klass = IntentType::class).default(defaultValue = IntentType.NONE)
    val isDefault = bool(name = "is_default").default(defaultValue = false)
    val createdAt = datetime(name = "created_at")
}

object SavedCommandExtraTable : IntIdTable(name = "saved_command_extra") {
    val savedCommandId = reference(name = "saved_command_id", foreign = SavedCommandTable)
    val extraType = varchar(name = "extra_type", length = 20)
    val extra = varchar(name = "extra", length = 255)
    val value = varchar(name = "value", length = 255)
}
