package com.rewardpoints.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Enhanced utility class for managing SharedPreferences operations with encryption
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "reward_points_prefs_encrypted";

    // Original Keys
    public static final String KEY_COUNTER = "count";
    public static final String KEY_DAY = "day";
    public static final String KEY_TODAY_FEEDBACK = "todayf";
    public static final String KEY_HISTORY = "fulldate";

    // New Enhanced Keys
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_DAILY_REMINDER_TIME = "daily_reminder_time";
    public static final String KEY_FIRST_LAUNCH = "first_launch";
    public static final String KEY_APP_VERSION = "app_version";

    // Points tracking keys
    public static final String KEY_TOTAL_POINTS_EARNED = "total_points_earned";
    public static final String KEY_TOTAL_POINTS_SPENT = "total_points_spent";

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

    // Original methods (keeping backward compatibility)
    public int getCounter() {
        return preferences.getInt(KEY_COUNTER, 0);
    }

    public void saveCounter(int counter) {
        editor.putInt(KEY_COUNTER, counter);
        editor.apply();
    }

    public int getLastDay() {
        return preferences.getInt(KEY_DAY, 0);
    }

    public void saveDay(int day) {
        editor.putInt(KEY_DAY, day);
        editor.apply();
    }

    public boolean isTodayFeedbackGiven() {
        return preferences.getBoolean(KEY_TODAY_FEEDBACK, false);
    }

    public void setTodayFeedbackGiven(boolean given) {
        editor.putBoolean(KEY_TODAY_FEEDBACK, given);
        editor.apply();
    }

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

    // New enhanced methods for additional functionality
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    // User profile methods
    public String getUserName() {
        return getString(KEY_USER_NAME, "User");
    }

    public void setUserName(String name) {
        putString(KEY_USER_NAME, name);
    }

    // App settings methods
    public String getThemeMode() {
        return getString(KEY_THEME_MODE, "SYSTEM");
    }

    public void setThemeMode(String mode) {
        putString(KEY_THEME_MODE, mode);
    }

    public boolean areNotificationsEnabled() {
        return getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
    }

    // Points tracking methods
    public int getTotalPointsEarned() {
        return getInt(KEY_TOTAL_POINTS_EARNED, 0);
    }

    public void setTotalPointsEarned(int points) {
        putInt(KEY_TOTAL_POINTS_EARNED, points);
    }

    public void addToTotalPointsEarned(int points) {
        int current = getTotalPointsEarned();
        setTotalPointsEarned(current + points);
    }

    public int getTotalPointsSpent() {
        return getInt(KEY_TOTAL_POINTS_SPENT, 0);
    }

    public void setTotalPointsSpent(int points) {
        putInt(KEY_TOTAL_POINTS_SPENT, points);
    }

    public void addToTotalPointsSpent(int points) {
        int current = getTotalPointsSpent();
        setTotalPointsSpent(current + points);
    }

    // Calculate net points (earned - spent)
    public int getNetPoints() {
        return getTotalPointsEarned() - getTotalPointsSpent();
    }

    // First launch detection methods
    public boolean isFirstLaunch() {
        return getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirst) {
        putBoolean(KEY_FIRST_LAUNCH, isFirst);
    }

    // App version tracking
    public String getAppVersion() {
        return getString(KEY_APP_VERSION, "1.0.0");
    }

    public void setAppVersion(String version) {
        putString(KEY_APP_VERSION, version);
    }

    // Utility methods
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public boolean contains(String key) {
        return preferences.contains(key);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }
}
