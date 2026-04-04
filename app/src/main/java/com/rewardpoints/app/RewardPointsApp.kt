package com.rewardpoints.app

import android.app.Application
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

class RewardPointsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RewardPointsApp)
            modules(appModule)
        }

        initializeData()
        scheduleWorkers()
    }

    private fun initializeData() {
        val playerRepository: PlayerRepository by inject()
        val achievementRepository: AchievementRepository by inject()

        CoroutineScope(Dispatchers.IO).launch {
            playerRepository.initializeStats()
            achievementRepository.initializeAchievements()
        }
    }

    private fun scheduleWorkers() {
        DecayWorker.schedule(this)
        TodoistSyncWorker.schedule(this, intervalMinutes = 15)
    }
}
