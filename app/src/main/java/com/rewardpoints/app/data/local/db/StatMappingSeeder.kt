package com.rewardpoints.app.data.local.db

import androidx.room.withTransaction
import com.rewardpoints.app.data.local.db.dao.StatMappingDao
import com.rewardpoints.app.data.local.db.entity.StatMappingEntity

/**
 * Default Todoist label → stat mappings. Called from app startup and from
 * `SettingsViewModel.fullReset` so a reset doesn't leave the table empty until the
 * next process start.
 */
object StatMappingSeeder {

    private val DEFAULTS = listOf(
        "STR" to "STR", "INT" to "INT", "WIS" to "WIS",
        "DEX" to "DEX", "CHA" to "CHA", "VIT" to "VIT"
    )

    /**
     * Seed only if the mappings table is empty. The check-then-insert pair runs inside
     * a Room transaction so a concurrent caller (e.g. app start racing with a reset)
     * can't both observe "empty" and double-insert.
     */
    suspend fun seedIfEmpty(database: AppDatabase, dao: StatMappingDao) {
        database.withTransaction {
            if (dao.getAllOnce().isNotEmpty()) return@withTransaction
            insertDefaults(dao)
        }
    }

    /** Unconditional seed (e.g. after `database.clearAllTables()` during fullReset). */
    suspend fun seed(database: AppDatabase, dao: StatMappingDao) {
        database.withTransaction { insertDefaults(dao) }
    }

    private suspend fun insertDefaults(dao: StatMappingDao) {
        val now = System.currentTimeMillis()
        DEFAULTS.forEach { (label, stat) ->
            dao.insert(
                StatMappingEntity(
                    sourceType = "LABEL",
                    sourceId = label.lowercase(),
                    sourceName = label,
                    statType = stat,
                    createdAt = now
                )
            )
        }
    }
}
