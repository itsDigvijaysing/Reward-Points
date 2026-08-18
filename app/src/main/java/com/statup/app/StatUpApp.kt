package com.statup.app

import android.app.Application
import android.util.Log
import com.statup.app.data.local.datastore.UserPreferences
import com.statup.app.data.local.db.StatMappingSeeder
import com.statup.app.data.local.db.dao.StatMappingDao
import com.statup.app.data.repository.AchievementRepository
import com.statup.app.data.repository.PlayerRepository
import com.statup.app.di.appModule
import com.statup.app.notifications.Notifier
import com.statup.app.widget.StatsWidgetUpdater
import org.koin.android.ext.android.get
import com.statup.app.sync.DecayWorker
import com.statup.app.sync.TodoistSyncWorker
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class StatUpApp : Application() {
    // SupervisorJob keeps siblings alive on one child failure; the handler swallows the
    // exception so it doesn't propagate to the global Thread uncaught handler (which on
    // Android typically crashes the app at startup — bad for a one-off init failure).
    private val initExceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.e("StatUpApp", "Init coroutine failed", e)
    }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + initExceptionHandler)

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
        val database: com.statup.app.data.local.db.AppDatabase by inject()
        val userPreferences: UserPreferences by inject()
        // Eagerly resolve Notifier so its channels are created before any worker tries to notify.
        // (`by inject()` would defer construction until first access.)
        get<Notifier>()

        appScope.launch { playerRepository.initializeStats() }
        appScope.launch { achievementRepository.initializeAchievements() }
        appScope.launch { StatMappingSeeder.seedIfEmpty(database, statMappingDao) }
        appScope.launch { userPreferences.loadSecretsIfNeeded() }

        // Refresh any installed home-screen widget with the current DB state. The system also
        // calls onUpdate on install/boot; this covers app-open after background data changes.
        appScope.launch { get<StatsWidgetUpdater>().refresh() }
    }

    private fun scheduleWorkers() {
        DecayWorker.schedule(this)
        TodoistSyncWorker.schedule(this, intervalMinutes = 15)
    }
}
