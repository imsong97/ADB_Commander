package com.ch0pp4.adbcommander.local.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.LocalDateTime

object DatabaseFactory {

    fun init() {
        val dbDir = File(findProjectRoot(), ".adbcommander")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "adbcommander.db")
        val isFirstRun = !dbFile.exists()

        Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}?" +
                    "journal_mode=WAL" + // WAL모드 - 동시성향상
                    "&busy_timeout=5000" + // 락 대기 5초
                    "&synchronous=NORMAL", // fsync 최적화
            driver = "org.sqlite.JDBC"
        )

        transaction {
            val hasSortOrder = exec("PRAGMA table_info(collection)") { rs ->
                var found = false
                while (rs.next()) {
                    if (rs.getString("name") == "sort_order") found = true
                }
                found
            } == true
            if (!hasSortOrder) {
                exec("ALTER TABLE collection ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0")
            }

            SchemaUtils.create(
                CollectionTable,
                CollectionSavedCommandTable,
                CollectionSavedCommandExtraTable,
            )

            if (isFirstRun) {
                seedSampleCommands()
            } else {
                // sort_order가 모두 0이면 createdAt 역순으로 초기값 설정
                val all = CollectionEntity.all().toList()
                if (all.size > 1 && all.all { it.sortOrder == 0 }) {
                    all.sortedByDescending { it.createdAt }
                        .forEachIndexed { index, entity -> entity.sortOrder = index }
                }
            }
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

    private fun seedSampleCommands() {
        val collection = CollectionEntity.new {
            name = "Example"
            createdAt = LocalDateTime.now()
        }

        CollectionSavedCommandEntity.new {
            collectionId = collection.id
            title = "디바이스 목록"
            command = "adb devices"
            intentType = IntentType.NONE
            createdAt = LocalDateTime.now()
        }

        val broadcastCommand = CollectionSavedCommandEntity.new {
            collectionId = collection.id
            title = "배터리 레벨 변경"
            command = "adb shell am broadcast -a android.intent.action.BATTERY_CHANGED --ei level 50"
            intentType = IntentType.BROADCAST
            createdAt = LocalDateTime.now()
        }
        CollectionSavedCommandExtraEntity.new {
            savedCommand = broadcastCommand
            extraType = "INT"
            extra = "level"
            value = "50"
        }
    }
}
