package com.rewardpoints.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 2,
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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
