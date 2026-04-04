package com.rewardpoints.app.di

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.AppDatabase
import com.rewardpoints.app.data.repository.AchievementRepository
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.data.repository.RewardRepository
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.rpg.DecayEngine
import com.rewardpoints.app.rpg.RankCalculator
import com.rewardpoints.app.rpg.StatsEngine
import com.rewardpoints.app.sync.TodoistApi
import com.rewardpoints.app.sync.TodoistSyncManager
import com.rewardpoints.app.ui.screen.achievements.AchievementsViewModel
import com.rewardpoints.app.ui.screen.history.HistoryViewModel
import com.rewardpoints.app.ui.screen.rewards.RewardsViewModel
import com.rewardpoints.app.ui.screen.settings.SettingsViewModel
import com.rewardpoints.app.ui.screen.stats.StatsViewModel
import com.rewardpoints.app.ui.screen.status.StatusViewModel
import com.rewardpoints.app.ui.screen.tasks.TasksViewModel
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().rewardDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().missionDao() }
    single { get<AppDatabase>().playerStatsDao() }
    single { get<AppDatabase>().statMappingDao() }
    single { get<AppDatabase>().decayLogDao() }
    single { get<AppDatabase>().titleDao() }
    single { get<AppDatabase>().aiMemoryDao() }

    // DataStore
    single { UserPreferences(androidContext()) }

    // HTTP Client
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.NONE
            }
            install(io.ktor.client.plugins.HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }
        }
    }

    // Todoist
    single { TodoistApi(get()) }
    single { TodoistSyncManager(get(), get(), get(), get(), get()) }

    // Repositories
    single { PlayerRepository(get(), get()) }
    single { PointsRepository(get(), get(), get(), get()) }
    single { RewardRepository(get(), get()) }
    single { AchievementRepository(get()) }

    // RPG Engines
    single { StatsEngine(get()) }
    single { RankCalculator() }
    single { DecayEngine(get(), get(), get(), get()) }
    single { AchievementTracker(get(), get(), get()) }

    // ViewModels
    viewModel { StatusViewModel(get(), get(), get(), get(), get()) }
    viewModel { RewardsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { TasksViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { AchievementsViewModel(get()) }
    viewModel { StatsViewModel(get(), get()) }
}
