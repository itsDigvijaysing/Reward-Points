package com.rewardpoints.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static final String KEY_FIRST_LAUNCH = "first_launch";

    // Points tracking keys
    public static final String KEY_TOTAL_POINTS_EARNED = "total_points_earned";
    public static final String KEY_TOTAL_POINTS_SPENT = "total_points_spent";

    // Transaction and feedback keys
    private static final String KEY_TRANSACTIONS = "transactions";
    private static final String KEY_DAILY_FEEDBACK_DATE = "daily_feedback_date";
    private static final String KEY_DAILY_STREAK = "daily_streak";
    private static final String KEY_LAST_ACTIVITY_DATE = "last_activity_date";
    private static final String KEY_COMPLETED_MISSIONS_TODAY = "completed_missions_today";
    private static final String KEY_DAILY_BONUS_DATE = "daily_bonus_date";
    private static final String KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications";
    private static final String KEY_CREATED_REWARD_TODAY = "created_reward_today";
    private static final String KEY_HAS_CREATED_REWARD = "has_created_reward";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public PreferencesManager(Context context) {
        SharedPreferences tempPrefs;
        try {
            // Check if device supports encryption (API 23+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                tempPrefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } else {
                // Fallback to regular SharedPreferences for older devices
                tempPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            }
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            tempPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        preferences = tempPrefs;
        editor = preferences.edit();
        gson = new Gson();
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
        return preferences.getString(KEY_USER_NAME, "User");
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    // App settings methods
    public String getThemeMode() {
        return preferences.getString(KEY_THEME_MODE, "system");
    }

    public void setThemeMode(String themeMode) {
        editor.putString(KEY_THEME_MODE, themeMode);
        editor.apply();
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        editor.putBoolean(KEY_FIRST_LAUNCH, firstLaunch);
        editor.apply();
    }

    public boolean isNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    // Points tracking methods
    public int getTotalPointsEarned() {
        return preferences.getInt(KEY_TOTAL_POINTS_EARNED, 0);
    }

    public void setTotalPointsEarned(int points) {
        editor.putInt(KEY_TOTAL_POINTS_EARNED, points);
        editor.apply();
    }

    public int getTotalPointsSpent() {
        return preferences.getInt(KEY_TOTAL_POINTS_SPENT, 0);
    }

    public void setTotalPointsSpent(int points) {
        editor.putInt(KEY_TOTAL_POINTS_SPENT, points);
        editor.apply();
    }

    // Enhanced transaction methods
    public void addTransaction(EnhancedTransaction transaction) {
        List<EnhancedTransaction> transactions = getTransactions();
        transactions.add(0, transaction); // Add to beginning for newest first
        saveTransactions(transactions);
    }

    public List<EnhancedTransaction> getTransactions() {
        String transactionsJson = preferences.getString(KEY_TRANSACTIONS, "[]");
        Type listType = new TypeToken<List<EnhancedTransaction>>(){}.getType();
        try {
            List<EnhancedTransaction> transactions = gson.fromJson(transactionsJson, listType);
            return transactions != null ? transactions : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveTransactions(List<EnhancedTransaction> transactions) {
        String transactionsJson = gson.toJson(transactions);
        editor.putString(KEY_TRANSACTIONS, transactionsJson);
        editor.apply();
    }

    public void clearTransactions() {
        editor.putString(KEY_TRANSACTIONS, "[]");
        editor.apply();
    }

    // NEW: User Achievements (Long-term Goals) methods
    private static final String KEY_USER_ACHIEVEMENTS = "user_achievements";

    public List<com.rewardpoints.app.models.Achievement> getUserAchievements() {
        String achievementsJson = preferences.getString(KEY_USER_ACHIEVEMENTS, "[]");
        Type listType = new TypeToken<List<com.rewardpoints.app.models.Achievement>>(){}.getType();
        try {
            List<com.rewardpoints.app.models.Achievement> achievements = gson.fromJson(achievementsJson, listType);
            return achievements != null ? achievements : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveUserAchievements(List<com.rewardpoints.app.models.Achievement> achievements) {
        String achievementsJson = gson.toJson(achievements);
        editor.putString(KEY_USER_ACHIEVEMENTS, achievementsJson);
        editor.apply();
    }

    // NEW: Daily Missions methods
    private static final String KEY_USER_DAILY_MISSIONS = "user_daily_missions";
    private static final String KEY_LAST_MISSION_DATE = "last_mission_date";
    private static final String KEY_LAST_STREAK_DATE = "last_streak_date";

    public List<com.rewardpoints.app.models.DailyMission> getUserDailyMissions() {
        String missionsJson = preferences.getString(KEY_USER_DAILY_MISSIONS, "[]");
        Type listType = new TypeToken<List<com.rewardpoints.app.models.DailyMission>>(){}.getType();
        try {
            List<com.rewardpoints.app.models.DailyMission> missions = gson.fromJson(missionsJson, listType);
            return missions != null ? missions : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveUserDailyMissions(List<com.rewardpoints.app.models.DailyMission> missions) {
        String missionsJson = gson.toJson(missions);
        editor.putString(KEY_USER_DAILY_MISSIONS, missionsJson);
        editor.apply();
    }

    public String getLastMissionDate() {
        return preferences.getString(KEY_LAST_MISSION_DATE, "");
    }

    public void setLastMissionDate(String date) {
        editor.putString(KEY_LAST_MISSION_DATE, date);
        editor.apply();
    }

    public String getLastStreakDate() {
        return preferences.getString(KEY_LAST_STREAK_DATE, "");
    }

    public void setLastStreakDate(String date) {
        editor.putString(KEY_LAST_STREAK_DATE, date);
        editor.apply();
    }

    // Achievement tracking methods
    private static final String KEY_AWARDED_ACHIEVEMENTS = "awarded_achievements";

    public boolean isAchievementAwarded(String achievementId) {
        Set<String> awardedAchievements = preferences.getStringSet(KEY_AWARDED_ACHIEVEMENTS, new HashSet<String>());
        return awardedAchievements.contains(achievementId);
    }

    public void setAchievementAwarded(String achievementId, boolean awarded) {
        Set<String> awardedAchievements = new HashSet<>(preferences.getStringSet(KEY_AWARDED_ACHIEVEMENTS, new HashSet<String>()));
        if (awarded) {
            awardedAchievements.add(achievementId);
        } else {
            awardedAchievements.remove(achievementId);
        }
        editor.putStringSet(KEY_AWARDED_ACHIEVEMENTS, awardedAchievements);
        editor.apply();
    }

    // Daily feedback methods
    public boolean hasFeedbackToday() {
        String lastFeedbackDate = preferences.getString(KEY_DAILY_FEEDBACK_DATE, "");
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        return today.equals(lastFeedbackDate);
    }

    public void setFeedbackCompletedToday() {
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        editor.putString(KEY_DAILY_FEEDBACK_DATE, today);
        editor.apply();
    }

    // Daily streak methods
    public int getDailyStreak() {
        return preferences.getInt(KEY_DAILY_STREAK, 0);
    }

    public void setDailyStreak(int streak) {
        editor.putInt(KEY_DAILY_STREAK, streak);
        editor.apply();
    }

    // Counter methods (for compatibility)
    public void setCounter(int counter) {
        editor.putInt(KEY_COUNTER, counter);
        editor.apply();
    }

    // Utility methods for data cleanup
    public void clearAllData() {
        editor.clear();
        editor.apply();
    }

    // Additional settings methods for SettingsActivity compatibility
    private static final String KEY_DAILY_LIMIT_ENABLED = "daily_limit_enabled";
    private static final String KEY_DAILY_POINTS_LIMIT = "daily_points_limit";
    private static final String KEY_DAILY_REMINDERS_ENABLED = "daily_reminders_enabled";
    private static final String KEY_ACHIEVEMENT_NOTIFICATIONS_ENABLED = "achievement_notifications_enabled";

    public boolean isDailyLimitEnabled() {
        return preferences.getBoolean(KEY_DAILY_LIMIT_ENABLED, false);
    }

    public void setDailyLimitEnabled(boolean enabled) {
        editor.putBoolean(KEY_DAILY_LIMIT_ENABLED, enabled);
        editor.apply();
    }

    public int getDailyPointsLimit() {
        return preferences.getInt(KEY_DAILY_POINTS_LIMIT, 100);
    }

    public void setDailyPointsLimit(int limit) {
        editor.putInt(KEY_DAILY_POINTS_LIMIT, limit);
        editor.apply();
    }

    public boolean isDailyRemindersEnabled() {
        return preferences.getBoolean(KEY_DAILY_REMINDERS_ENABLED, true);
    }

    public void setDailyRemindersEnabled(boolean enabled) {
        editor.putBoolean(KEY_DAILY_REMINDERS_ENABLED, enabled);
        editor.apply();
    }

    public boolean isAchievementNotificationsEnabled() {
        return preferences.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS_ENABLED, true);
    }

    public void setAchievementNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }
}
