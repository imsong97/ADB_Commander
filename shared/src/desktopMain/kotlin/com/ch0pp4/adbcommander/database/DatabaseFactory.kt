package com.ch0pp4.adbcommander.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    fun init() {
        val dbDir = File(System.getProperty("user.dir"), ".adbcommander")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "adbcommander.db")

        Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(
                SavedCommandTable,
                SavedCommandExtraTable,
            )
        }

    }
}
