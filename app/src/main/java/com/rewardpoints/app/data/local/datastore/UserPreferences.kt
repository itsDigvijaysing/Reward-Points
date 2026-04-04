package com.rewardpoints.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private object Keys {
        val USERNAME = stringPreferencesKey("username")
        val TODOIST_TOKEN = stringPreferencesKey("todoist_token")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val DEFAULT_STAT = stringPreferencesKey("default_stat")
        val SYNC_INTERVAL_MINUTES = intPreferencesKey("sync_interval_minutes")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val SHOW_DECAY_ANIMATIONS = booleanPreferencesKey("show_decay_animations")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val HEXAGON_STYLE = stringPreferencesKey("hexagon_style")
    }

    val username: Flow<String> = context.dataStore.data.map { it[Keys.USERNAME] ?: "Player" }
    val todoistToken: Flow<String?> = context.dataStore.data.map { it[Keys.TODOIST_TOKEN] }
    val geminiApiKey: Flow<String?> = context.dataStore.data.map { it[Keys.GEMINI_API_KEY] }
    val defaultStat: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_STAT] ?: "INT" }
    val syncIntervalMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.SYNC_INTERVAL_MINUTES] ?: 15 }
    val lastSyncTime: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_SYNC_TIME] ?: 0L }
    val showDecayAnimations: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_DECAY_ANIMATIONS] ?: true }
    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAPTIC_FEEDBACK] ?: true }
    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }
    val hexagonStyle: Flow<String> = context.dataStore.data.map { it[Keys.HEXAGON_STYLE] ?: "simple" }

    suspend fun setUsername(username: String) {
        context.dataStore.edit { it[Keys.USERNAME] = username }
    }

    suspend fun setTodoistToken(token: String?) {
        context.dataStore.edit {
            if (token != null) it[Keys.TODOIST_TOKEN] = token
            else it.remove(Keys.TODOIST_TOKEN)
        }
    }

    suspend fun setGeminiApiKey(key: String?) {
        context.dataStore.edit {
            if (key != null) it[Keys.GEMINI_API_KEY] = key
            else it.remove(Keys.GEMINI_API_KEY)
        }
    }

    suspend fun setDefaultStat(stat: String) {
        context.dataStore.edit { it[Keys.DEFAULT_STAT] = stat }
    }

    suspend fun setSyncIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SYNC_INTERVAL_MINUTES] = minutes }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNC_TIME] = time }
    }

    suspend fun setShowDecayAnimations(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_DECAY_ANIMATIONS] = show }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setHexagonStyle(style: String) {
        context.dataStore.edit { it[Keys.HEXAGON_STYLE] = style }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
