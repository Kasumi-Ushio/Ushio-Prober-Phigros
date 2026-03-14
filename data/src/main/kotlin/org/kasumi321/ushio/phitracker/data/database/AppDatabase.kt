package org.kasumi321.ushio.phitracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecordEntity::class, UserEntity::class, SyncSnapshotEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun userDao(): UserDao
    abstract fun syncSnapshotDao(): SyncSnapshotDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sync_snapshots` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `rks` REAL NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `dataCount` INTEGER NOT NULL,
                        `lastSyncedSongId` TEXT,
                        `lastSyncedDifficulty` TEXT,
                        `lastSyncedScore` INTEGER,
                        `lastSyncedAccuracy` REAL
                    )
                """.trimIndent())
            }
        }
    }
}
