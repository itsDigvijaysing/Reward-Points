package com.rewardpoints.app.managers;

import android.content.Context;
import com.rewardpoints.app.models.CustomReward;
import com.rewardpoints.app.models.PointsSource;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.utils.PreferencesManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced manager for handling customizable rewards and points sources
 */
public class CustomizationManager {

    private static final String KEY_CUSTOM_REWARDS = "custom_rewards";
    private static final String KEY_POINTS_SOURCES = "points_sources";
    private static final String KEY_ENHANCED_TRANSACTIONS = "enhanced_transactions";

    private final PreferencesManager preferencesManager;
    private final Gson gson;

    public CustomizationManager(Context context) {
        this.preferencesManager = new PreferencesManager(context);
        this.gson = new Gson();
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        if (getCustomRewards().isEmpty()) {
            createDefaultRewards();
        }
        if (getPointsSources().isEmpty()) {
            createDefaultPointsSources();
        }
    }

    private void createDefaultRewards() {
        List<CustomReward> defaultRewards = new ArrayList<>();

        // Only 3 simple default rewards as requested
        defaultRewards.add(new CustomReward("Watch Movie", "Enjoy a movie", 150, "Entertainment"));
        defaultRewards.add(new CustomReward("Buy Treat", "Get something nice", 200, "Shopping"));
        defaultRewards.add(new CustomReward("Gaming Time", "1 hour of gaming", 250, "Entertainment"));

        saveCustomRewards(defaultRewards);
    }

    private void createDefaultPointsSources() {
        List<PointsSource> defaultSources = new ArrayList<>();

        // Only essential mood-based points (reduced from 4 to 3)
        defaultSources.add(new PointsSource("Great Day", "Feeling fantastic today", 50, "Mood"));
        defaultSources.add(new PointsSource("Good Day", "Having a nice day", 30, "Mood"));
        defaultSources.add(new PointsSource("Okay Day", "Regular day", 15, "Mood"));

        // Only basic activities (reduced from 10+ to 3)
        defaultSources.add(new PointsSource("Task Completed", "Finished a task", 25, "Achievement"));
        defaultSources.add(new PointsSource("Exercise", "Physical activity", 35, "Health"));

        savePointsSources(defaultSources);
    }

    // Add missing method for mood sources
    public void createDefaultMoodSources() {
        List<PointsSource> moodSources = new ArrayList<>();

        // Use correct constructor: (name, description, pointsValue, category)
        moodSources.add(new PointsSource("Great Day", "Feeling fantastic today!", 20, "Mood"));
        moodSources.add(new PointsSource("Good Day", "Having a good day", 15, "Mood"));
        moodSources.add(new PointsSource("Okay Day", "Day is going fine", 10, "Mood"));
        moodSources.add(new PointsSource("Challenging Day", "Tough day but staying positive", 25, "Mood"));

        // Merge with existing sources
        List<PointsSource> existingSources = getPointsSources();
        existingSources.addAll(moodSources);
        savePointsSources(existingSources);
    }

    // Custom Rewards Management
    public List<CustomReward> getCustomRewards() {
        String json = preferencesManager.getString(KEY_CUSTOM_REWARDS, "[]");
        Type listType = new TypeToken<List<CustomReward>>(){}.getType();
        try {
            List<CustomReward> rewards = gson.fromJson(json, listType);
            return rewards != null ? rewards : new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            // Handle corrupted JSON data by returning empty list and resetting
            preferencesManager.putString(KEY_CUSTOM_REWARDS, "[]");
            return new ArrayList<>();
        } catch (Exception e) {
            // Handle any other parsing errors
            return new ArrayList<>();
        }
    }

    public void saveCustomRewards(List<CustomReward> rewards) {
        String json = gson.toJson(rewards);
        preferencesManager.putString(KEY_CUSTOM_REWARDS, json);
    }

    public void addCustomReward(CustomReward reward) {
        List<CustomReward> rewards = getCustomRewards();
        rewards.add(reward);
        saveCustomRewards(rewards);
    }

    public void updateCustomReward(CustomReward updatedReward) {
        List<CustomReward> rewards = getCustomRewards();
        for (int i = 0; i < rewards.size(); i++) {
            if (rewards.get(i).getId().equals(updatedReward.getId())) {
                rewards.set(i, updatedReward);
                break;
            }
        }
        saveCustomRewards(rewards);
    }

    public void deleteCustomReward(String rewardId) {
        List<CustomReward> rewards = getCustomRewards();
        // API 21+ compatible removal instead of removeIf()
        for (int i = rewards.size() - 1; i >= 0; i--) {
            if (rewards.get(i).getId().equals(rewardId)) {
                rewards.remove(i);
            }
        }
        saveCustomRewards(rewards);
    }

    public CustomReward getRewardById(String id) {
        List<CustomReward> rewards = getCustomRewards();
        for (CustomReward reward : rewards) {
            if (reward.getId().equals(id)) {
                return reward;
            }
        }
        return null;
    }

    // Points Sources Management
    public List<PointsSource> getPointsSources() {
        String json = preferencesManager.getString(KEY_POINTS_SOURCES, "[]");
        Type listType = new TypeToken<List<PointsSource>>(){}.getType();
        try {
            List<PointsSource> sources = gson.fromJson(json, listType);
            return sources != null ? sources : new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            // Handle corrupted JSON data by returning empty list and resetting
            preferencesManager.putString(KEY_POINTS_SOURCES, "[]");
            return new ArrayList<>();
        } catch (Exception e) {
            // Handle any other parsing errors
            return new ArrayList<>();
        }
    }

    public void savePointsSources(List<PointsSource> sources) {
        String json = gson.toJson(sources);
        preferencesManager.putString(KEY_POINTS_SOURCES, json);
    }

    public void addPointsSource(PointsSource source) {
        List<PointsSource> sources = getPointsSources();
        sources.add(source);
        savePointsSources(sources);
    }

    public void updatePointsSource(PointsSource updatedSource) {
        List<PointsSource> sources = getPointsSources();
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).getId().equals(updatedSource.getId())) {
                sources.set(i, updatedSource);
                break;
            }
        }
        savePointsSources(sources);
    }

    public void deletePointsSource(String sourceId) {
        List<PointsSource> sources = getPointsSources();
        // API 21+ compatible removal instead of removeIf()
        for (int i = sources.size() - 1; i >= 0; i--) {
            if (sources.get(i).getId().equals(sourceId)) {
                sources.remove(i);
            }
        }
        savePointsSources(sources);
    }

    public List<PointsSource> getPointsSourcesByCategory(String category) {
        List<PointsSource> allSources = getPointsSources();
        List<PointsSource> filteredSources = new ArrayList<>();
        for (PointsSource source : allSources) {
            if (source.getCategory().equals(category) && source.isActive()) {
                filteredSources.add(source);
            }
        }
        return filteredSources;
    }

    // Enhanced Transactions Management
    public List<EnhancedTransaction> getEnhancedTransactions() {
        String json = preferencesManager.getString(KEY_ENHANCED_TRANSACTIONS, "[]");
        Type listType = new TypeToken<List<EnhancedTransaction>>(){}.getType();
        try {
            List<EnhancedTransaction> transactions = gson.fromJson(json, listType);
            return transactions != null ? transactions : new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            // Handle corrupted JSON data by returning empty list and resetting
            preferencesManager.putString(KEY_ENHANCED_TRANSACTIONS, "[]");
            return new ArrayList<>();
        } catch (Exception e) {
            // Handle any other parsing errors
            return new ArrayList<>();
        }
    }

    public void saveEnhancedTransactions(List<EnhancedTransaction> transactions) {
        String json = gson.toJson(transactions);
        preferencesManager.putString(KEY_ENHANCED_TRANSACTIONS, json);
    }

    public void addTransaction(EnhancedTransaction transaction) {
        List<EnhancedTransaction> transactions = getEnhancedTransactions();
        transactions.add(transaction);
        saveEnhancedTransactions(transactions);
    }

    // Add missing methods that are being called
    public List<PointsSource> getAllPointsSources() {
        return getPointsSources();
    }

    public List<EnhancedTransaction> getAllTransactions() {
        return getEnhancedTransactions();
    }

    public void clearAllTransactions() {
        saveEnhancedTransactions(new ArrayList<>());
    }

    public void resetAllData() {
        // Clear all custom data
        saveCustomRewards(new ArrayList<>());
        savePointsSources(new ArrayList<>());
        saveEnhancedTransactions(new ArrayList<>());

        // Recreate defaults
        createDefaultRewards();
        createDefaultPointsSources();
    }
}
