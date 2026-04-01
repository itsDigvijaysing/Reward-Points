package com.rewardpoints.app

import android.app.Application
import com.rewardpoints.app.data.local.db.dao.StatMappingDao
import com.rewardpoints.app.data.local.db.entity.StatMappingEntity
import com.rewardpoints.app.data.repository.AchievementRepository
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.di.appModule
import com.rewardpoints.app.sync.DecayWorker
import com.rewardpoints.app.sync.TodoistSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class StatUpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@StatUpApp)
            modules(appModule)
        }

        initializeData()
        scheduleWorkers()
    }

    private fun initializeData() {
        val playerRepository: PlayerRepository by inject()
        val achievementRepository: AchievementRepository by inject()
        val statMappingDao: StatMappingDao by inject()

        CoroutineScope(Dispatchers.IO).launch {
            playerRepository.initializeStats()
            achievementRepository.initializeAchievements()
            initializeStatMappings(statMappingDao)
        }
    }

    private suspend fun initializeStatMappings(dao: StatMappingDao) {
        val existing = dao.getAllOnce()
        if (existing.isNotEmpty()) return

        // Seed default label→stat mappings for Todoist
        val now = System.currentTimeMillis()
        val defaults = listOf(
            "STR" to "STR", "INT" to "INT", "WIS" to "WIS",
            "DEX" to "DEX", "CHA" to "CHA", "VIT" to "VIT"
        )
        defaults.forEach { (label, stat) ->
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

    private fun scheduleWorkers() {
        DecayWorker.schedule(this)
        TodoistSyncWorker.schedule(this, intervalMinutes = 15)
    }
}
