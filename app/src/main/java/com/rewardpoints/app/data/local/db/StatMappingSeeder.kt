package com.rewardpoints.app.data.local.db

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

    suspend fun seedIfEmpty(dao: StatMappingDao) {
        if (dao.getAllOnce().isNotEmpty()) return
        seed(dao)
    }

    suspend fun seed(dao: StatMappingDao) {
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
