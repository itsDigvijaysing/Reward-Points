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

        defaultRewards.add(new CustomReward("Full Movie Experience", "Watch a complete movie", 200, "Entertainment"));
        defaultRewards.add(new CustomReward("2 Hours Gaming", "Play games for 2 hours", 300, "Entertainment"));
        defaultRewards.add(new CustomReward("1 Hour Travel Time", "Relax during travel", 200, "Travel"));
        defaultRewards.add(new CustomReward("Shopping Spree", "Buy something nice (500rs)", 500, "Shopping"));
        defaultRewards.add(new CustomReward("Favorite Food Treat", "Order your favorite food", 150, "Food & Dining"));
        defaultRewards.add(new CustomReward("New Book Purchase", "Buy a new book to read", 180, "Education"));
        defaultRewards.add(new CustomReward("Spa/Massage Session", "Relax with a massage", 400, "Health & Fitness"));
        defaultRewards.add(new CustomReward("Coffee Shop Visit", "Enjoy coffee at favorite cafe", 80, "Food & Dining"));

        saveCustomRewards(defaultRewards);
    }

    private void createDefaultPointsSources() {
        List<PointsSource> defaultSources = new ArrayList<>();

        // Mood-based points
        defaultSources.add(new PointsSource("Very Happy Day", "Feeling amazing today", 120, "Mood"));
        defaultSources.add(new PointsSource("Happy Day", "Good day overall", 60, "Mood"));
        defaultSources.add(new PointsSource("Okay Day", "Neutral mood", 20, "Mood"));
        defaultSources.add(new PointsSource("Difficult Day", "Challenging but managed", 10, "Mood"));

        // Achievement-based points
        defaultSources.add(new PointsSource("Major Achievement", "Significant accomplishment", 200, "Achievement"));
        defaultSources.add(new PointsSource("Important Achievement", "Notable accomplishment", 120, "Achievement"));
        defaultSources.add(new PointsSource("Good Achievement", "Nice accomplishment", 60, "Achievement"));

        // Mission-based points
        defaultSources.add(new PointsSource("Health Mission", "Completed health activity", 25, "Mission"));
        defaultSources.add(new PointsSource("Work Mission", "Completed work task", 25, "Mission"));
        defaultSources.add(new PointsSource("Self Development", "Learning or growth activity", 25, "Mission"));

        // Custom activities
        defaultSources.add(new PointsSource("Exercise Session", "Completed workout", 40, "Health"));
        defaultSources.add(new PointsSource("Reading Session", "Read for 30+ minutes", 30, "Education"));
        defaultSources.add(new PointsSource("Meditation", "Mindfulness practice", 35, "Health"));
        defaultSources.add(new PointsSource("Skill Practice", "Practice a skill", 50, "Education"));

        savePointsSources(defaultSources);
    }

    // Custom Rewards Management
    public List<CustomReward> getCustomRewards() {
        String json = preferencesManager.getString(KEY_CUSTOM_REWARDS, "[]");
        Type listType = new TypeToken<List<CustomReward>>(){}.getType();
        return gson.fromJson(json, listType);
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
        rewards.removeIf(reward -> reward.getId().equals(rewardId));
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
        return gson.fromJson(json, listType);
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
        sources.removeIf(source -> source.getId().equals(sourceId));
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
        return gson.fromJson(json, listType);
    }

    public void saveEnhancedTransactions(List<EnhancedTransaction> transactions) {
        String json = gson.toJson(transactions);
        preferencesManager.putString(KEY_ENHANCED_TRANSACTIONS, json);
    }

    public void addTransaction(EnhancedTransaction transaction) {
        List<EnhancedTransaction> transactions = getEnhancedTransactions();
        transactions.add(0, transaction); // Add to beginning for newest first

        // Limit to last 1000 transactions to prevent memory issues
        if (transactions.size() > 1000) {
            transactions = transactions.subList(0, 1000);
        }

        saveEnhancedTransactions(transactions);
    }

    public void clearAllTransactions() {
        preferencesManager.putString(KEY_ENHANCED_TRANSACTIONS, "[]");
    }

    // Analytics Methods
    public int getTotalPointsEarned() {
        List<EnhancedTransaction> transactions = getEnhancedTransactions();
        int total = 0;
        for (EnhancedTransaction transaction : transactions) {
            if (transaction.isEarning()) {
                total += transaction.getPoints();
            }
        }
        return total;
    }

    public int getTotalPointsSpent() {
        List<EnhancedTransaction> transactions = getEnhancedTransactions();
        int total = 0;
        for (EnhancedTransaction transaction : transactions) {
            if (transaction.isSpending()) {
                total += transaction.getPoints();
            }
        }
        return total;
    }

    public int getTransactionCountByCategory(String category) {
        List<EnhancedTransaction> transactions = getEnhancedTransactions();
        int count = 0;
        for (EnhancedTransaction transaction : transactions) {
            if (category.equals(transaction.getCategory())) {
                count++;
            }
        }
        return count;
    }
}
