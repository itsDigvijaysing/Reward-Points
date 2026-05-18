package com.rewardpoints.app.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AES-256-GCM encrypted storage for API tokens (Todoist, future Gemini key).
 * Backed by AndroidX Security `EncryptedSharedPreferences`; keys are protected by
 * the device's Android Keystore. All read/write APIs are suspending and dispatched
 * on IO to avoid blocking the main thread.
 */
class SecretStorage(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secret_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    suspend fun putString(key: String, value: String?) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }

    companion object {
        const val KEY_TODOIST_TOKEN = "todoist_token"
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
    }
}
