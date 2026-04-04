package com.rewardpoints.app

import android.app.Application
import com.rewardpoints.app.di.appModule
import com.rewardpoints.app.sync.DecayWorker
import com.rewardpoints.app.sync.TodoistSyncWorker
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

        // Schedule background workers
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        // Schedule daily decay at midnight
        DecayWorker.schedule(this)

        // Schedule Todoist sync every 15 minutes
        TodoistSyncWorker.schedule(this, intervalMinutes = 15)
    }
}
