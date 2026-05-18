package com.rewardpoints.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rewardpoints.app.data.local.db.dao.*
import com.rewardpoints.app.data.local.db.entity.*

@Database(
    entities = [
        RewardEntity::class,
        TransactionEntity::class,
        MissionEntity::class,
        PlayerStatsEntity::class,
        StatMappingEntity::class,
        DecayLogEntity::class,
        TitleEntity::class,
        TodoistTaskEntity::class,
        AiMemoryEntity::class,
        AiConversationEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rewardDao(): RewardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun missionDao(): MissionDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun statMappingDao(): StatMappingDao
    abstract fun decayLogDao(): DecayLogDao
    abstract fun titleDao(): TitleDao
    abstract fun aiMemoryDao(): AiMemoryDao

    companion object {
        const val DATABASE_NAME = "reward_points_db"

        // v1 and v2 share the same identity hash — the bump was metadata-only.
        // We still need a registered Migration so v1 installs can upgrade without
        // hitting fallbackToDestructiveMigration.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) { /* no-op */ }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE titles ADD COLUMN rewardPoints INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Add a unique index on transactions.externalId so the Todoist sync dedupe
        // check + insert is enforced at the DB level (overlapping sync runs can race).
        // We first drop any duplicates so the index can be created.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    DELETE FROM transactions
                    WHERE externalId IS NOT NULL
                      AND id NOT IN (
                        SELECT MIN(id) FROM transactions
                        WHERE externalId IS NOT NULL
                        GROUP BY externalId
                      )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_externalId " +
                            "ON transactions(externalId)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
