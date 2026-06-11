package com.rewardpoints.app.di

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.AppDatabase
import com.rewardpoints.app.data.local.db.RoomTransactor
import com.rewardpoints.app.data.repository.AchievementRepository
import com.rewardpoints.app.data.repository.MissionRepository
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.data.repository.RewardRepository
import com.rewardpoints.app.ai.AgentApi
import com.rewardpoints.app.ai.AgentContextBuilder
import com.rewardpoints.app.ai.AgentRepository
import com.rewardpoints.app.ai.GeminiAgentApi
import com.rewardpoints.app.notifications.Notifier
import com.rewardpoints.app.quotes.AnimechanApi
import com.rewardpoints.app.quotes.OfflineQuotePack
import com.rewardpoints.app.quotes.QuotePack
import com.rewardpoints.app.quotes.QuoteRepository
import com.rewardpoints.app.quotes.ZenQuotesApi
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.rpg.DecayEngine
import com.rewardpoints.app.rpg.RankCalculator
import com.rewardpoints.app.rpg.StatsEngine
import com.rewardpoints.app.rpg.Transactor
import com.rewardpoints.app.sync.TodoistApi
import com.rewardpoints.app.sync.TodoistSyncManager
import com.rewardpoints.app.ui.screen.agent.AgentViewModel
import com.rewardpoints.app.widget.StatsWidgetUpdater
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

    // Wraps Room's withTransaction so engines (e.g. DecayEngine) can run a read-modify-write
    // atomically without depending on the concrete AppDatabase — keeps them JVM-unit-testable.
    single<Transactor> { RoomTransactor(get()) }

    // DataStore
    single { UserPreferences(androidContext()) }

    // Notifications
    single { Notifier(androidContext()) }

    // Home-screen widget refresher — pushes APPWIDGET_UPDATE broadcasts when data changes.
    single { StatsWidgetUpdater(androidContext()) }

    // HTTP Client
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(io.ktor.client.plugins.HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }
            expectSuccess = false
        }
    }

    // Todoist
    single { TodoistApi(get()) }
    single { TodoistSyncManager(get(), get(), get(), get()) }

    // Daily Quote — UserPreferences implements the DailyQuoteStore slice.
    single<QuotePack> { OfflineQuotePack(androidContext()) }
    single {
        QuoteRepository(
            store = get<UserPreferences>(),
            animeApi = AnimechanApi(get()),
            motivationApi = ZenQuotesApi(get()),
            offlinePack = get()
        )
    }

    // AI Agent (Gemini)
    // GeminiAgentApi resolves the API key on-demand via a suspending lambda so it always
    // reads the latest value from encrypted storage — no need to recreate the singleton when
    // the user updates their key in Settings.
    single<AgentApi> {
        val userPreferences = get<com.rewardpoints.app.data.local.datastore.UserPreferences>()
        GeminiAgentApi(
            httpClient = get(),
            apiKeyProvider = { userPreferences.getGeminiApiKey() }
        )
    }
    single { AgentContextBuilder(get<PlayerRepository>(), get(), get()) }
    single { AgentRepository(get(), get()) }

    // Repositories
    single { PlayerRepository(get(), get()) }
    single { MissionRepository(get(), get()) }
    single { PointsRepository(get(), get(), get(), get(), get(), get()) }
    // AchievementRepository takes an optional points-award lambda. We resolve PointsRepository
    // lazily through the Koin container so we don't introduce a circular dependency
    // (AchievementTracker → AchievementRepository → PointsRepository → AchievementTracker).
    single {
        AchievementRepository(
            database = get(),
            titleDao = get(),
            pointsAwarder = { achievementId, points ->
                get<PointsRepository>().addPoints(
                    points = points,
                    type = com.rewardpoints.app.domain.model.TransactionType.EARN,
                    source = com.rewardpoints.app.domain.model.TransactionSource.MANUAL,
                    description = "Achievement reward: $achievementId"
                )
            }
        )
    }
    single { RewardRepository(get(), get(), get()) }

    // RPG Engines
    single { StatsEngine() }
    single { RankCalculator() }
    single {
        DecayEngine(
            statsStore = get<PlayerRepository>(),
            decayLogDao = get(),
            dayStore = get<UserPreferences>(),
            transactor = get(),
            transactionDao = get(),
            achievementTracker = get(),
            widgetUpdater = get()
        )
    }
    single { AchievementTracker(get(), get(), get()) }

    // ViewModels
    viewModel { StatusViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { RewardsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TasksViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get<PointsRepository>().transactions) }
    viewModel { AchievementsViewModel(get()) }
    viewModel { StatsViewModel(get(), get()) }
    viewModel { AgentViewModel(get(), get()) }
}
