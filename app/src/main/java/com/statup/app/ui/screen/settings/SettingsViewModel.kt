package com.statup.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statup.app.data.local.datastore.UserPreferences
import com.statup.app.data.local.db.AppDatabase
import com.statup.app.data.local.db.StatMappingSeeder
import com.statup.app.data.local.db.dao.StatMappingDao
import com.statup.app.data.repository.AchievementRepository
import com.statup.app.data.repository.PlayerRepository
import com.statup.app.rpg.AchievementTracker
import com.statup.app.sync.TodoistApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase,
    private val achievementTracker: AchievementTracker,
    private val playerRepository: PlayerRepository,
    private val todoistApi: TodoistApi,
    private val achievementRepository: AchievementRepository,
    private val statMappingDao: StatMappingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.username,
                userPreferences.todoistToken,
                userPreferences.geminiApiKey,
                userPreferences.defaultStat,
                userPreferences.showDecayAnimations,
                userPreferences.hapticFeedback,
                userPreferences.hexagonStyle,
                userPreferences.quoteSource
            ) { values: Array<Any?> ->
                SettingsUiState(
                    username = values[0] as String,
                    todoistConnected = !(values[1] as String?).isNullOrBlank(),
                    geminiConnected = !(values[2] as String?).isNullOrBlank(),
                    defaultStat = values[3] as String,
                    showDecayAnimations = values[4] as Boolean,
                    hapticFeedback = values[5] as Boolean,
                    hexagonStyle = values[6] as String,
                    quoteSource = values[7] as String,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            userPreferences.setUsername(username)
        }
    }

    fun updateDefaultStat(stat: String) {
        viewModelScope.launch {
            userPreferences.setDefaultStat(stat)
        }
    }

    fun updateShowDecayAnimations(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowDecayAnimations(show)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setHapticFeedback(enabled)
        }
    }

    fun updateHexagonStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setHexagonStyle(style)
        }
    }

    fun updateQuoteSource(source: String) {
        viewModelScope.launch {
            userPreferences.setQuoteSource(source)
        }
    }

    fun setTodoistToken(token: String?) {
        viewModelScope.launch {
            userPreferences.setTodoistToken(token)
            if (!token.isNullOrBlank()) {
                achievementTracker.onTodoistConnected()
            }
        }
    }

    /**
     * Save Gemini API key. Pass null/blank to disconnect.
     * No validation here — the agent surfaces auth errors when the user first chats.
     * (Gemini doesn't have a cheap ping endpoint; we don't want to burn a quota call on save.)
     */
    fun setGeminiApiKey(key: String?) {
        viewModelScope.launch {
            userPreferences.setGeminiApiKey(key?.takeIf { it.isNotBlank() })
        }
    }

    /**
     * Validates Todoist API token before saving.
     * Returns Result with success=true if valid, or failure with error message.
     */
    suspend fun validateAndConnectTodoist(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            return@withContext Result.failure(Exception("Token cannot be empty"))
        }
        
        try {
            val result = todoistApi.testConnection(token)
            if (result.isSuccess) {
                userPreferences.setTodoistToken(token)
                achievementTracker.onTodoistConnected()
                Result.success(true)
            } else {
                val error = result.exceptionOrNull()
                Result.failure(Exception("Invalid token: ${error?.message ?: "Connection failed"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection error: ${e.message ?: "Unknown error"}"))
        }
    }

    fun fullReset() {
        viewModelScope.launch {
            // Clear all database tables
            database.clearAllTables()

            // Clear all preferences and reset to defaults
            userPreferences.clearAll()

            // Re-initialize so app doesn't crash (stats at base 5, achievements + label
            // mappings seeded). Without re-seeding mappings here, Todoist label routing
            // would silently fall back to the default stat until next process start.
            playerRepository.initializeStats()
            achievementRepository.initializeAchievements()
            StatMappingSeeder.seed(database, statMappingDao)
        }
    }
}

data class SettingsUiState(
    val username: String = "Player",
    val todoistConnected: Boolean = false,
    val geminiConnected: Boolean = false,
    val defaultStat: String = "INT",
    val showDecayAnimations: Boolean = true,
    val hapticFeedback: Boolean = true,
    val hexagonStyle: String = "simple",
    val quoteSource: String = "OFFLINE",
    val isLoading: Boolean = true
)
