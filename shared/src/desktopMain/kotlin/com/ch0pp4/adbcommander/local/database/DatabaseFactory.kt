package com.ch0pp4.adbcommander.local.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    fun init() {
        val dbDir = File(findProjectRoot(), ".adbcommander")
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

    private fun findProjectRoot(): File {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null) {
            if (File(dir, "settings.gradle.kts").exists()) return dir
            dir = dir.parentFile
        }
        return File(System.getProperty("user.dir"))
    }
}
