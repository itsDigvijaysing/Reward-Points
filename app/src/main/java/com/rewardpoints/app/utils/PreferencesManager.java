package com.rewardpoints.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Utility class for managing SharedPreferences operations with encryption
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "reward_points_prefs_encrypted";

    // Keys
    public static final String KEY_COUNTER = "count";
    public static final String KEY_DAY = "day";
    public static final String KEY_TODAY_FEEDBACK = "todayf";
    public static final String KEY_HISTORY = "fulldate";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public PreferencesManager(Context context) {
        SharedPreferences tempPrefs;
        try {
            // Try to create encrypted shared preferences
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempPrefs = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            tempPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        preferences = tempPrefs;
        editor = preferences.edit();
    }

    // Counter operations
    public int getCounter() {
        return preferences.getInt(KEY_COUNTER, 0);
    }

    public void saveCounter(int counter) {
        editor.putInt(KEY_COUNTER, counter);
        editor.apply();
    }

    // Day operations
    public int getLastDay() {
        return preferences.getInt(KEY_DAY, 0);
    }

    public void saveDay(int day) {
        editor.putInt(KEY_DAY, day);
        editor.apply();
    }

    // Feedback operations
    public boolean isTodayFeedbackGiven() {
        return preferences.getBoolean(KEY_TODAY_FEEDBACK, false);
    }

    public void setTodayFeedbackGiven(boolean given) {
        editor.putBoolean(KEY_TODAY_FEEDBACK, given);
        editor.apply();
    }

    // History operations
    public String getHistory() {
        return preferences.getString(KEY_HISTORY, "No Previous History");
    }

    public void addToHistory(String entry) {
        String existingHistory = getHistory();
        if ("No Previous History".equals(existingHistory)) {
            existingHistory = "";
        }
        String newHistory = entry + (existingHistory.isEmpty() ? "" : "\n" + existingHistory);
        editor.putString(KEY_HISTORY, newHistory);
        editor.apply();
    }

    public void clearHistory() {
        editor.putString(KEY_HISTORY, "");
        editor.apply();
    }
}
