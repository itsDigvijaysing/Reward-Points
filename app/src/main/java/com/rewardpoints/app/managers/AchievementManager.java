package com.rewardpoints.app.managers;

import android.content.Context;
import android.widget.Toast;

import com.rewardpoints.app.models.Achievement;
import com.rewardpoints.app.models.EnhancedTransaction;
import com.rewardpoints.app.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    private Context context;
    private PreferencesManager preferencesManager;

    public AchievementManager(Context context) {
        this.context = context;
        this.preferencesManager = new PreferencesManager(context);
    }

    // Get user-created achievements (long-term goals)
    public List<Achievement> getUserAchievements() {
        return preferencesManager.getUserAchievements();
    }

    // Add a new user achievement
    public void addUserAchievement(Achievement achievement) {
        List<Achievement> achievements = preferencesManager.getUserAchievements();
        if (achievements == null) {
            achievements = new ArrayList<>();
        }
        achievements.add(achievement);
        preferencesManager.saveUserAchievements(achievements);
    }

    // Update an existing achievement
    public void updateUserAchievement(Achievement achievement) {
        List<Achievement> achievements = preferencesManager.getUserAchievements();
        if (achievements != null) {
            for (int i = 0; i < achievements.size(); i++) {
                if (achievements.get(i).getId().equals(achievement.getId())) {
                    achievements.set(i, achievement);
                    break;
                }
            }
            preferencesManager.saveUserAchievements(achievements);
        }
    }

    // Delete a user achievement
    public void deleteUserAchievement(String achievementId) {
        List<Achievement> achievements = preferencesManager.getUserAchievements();
        if (achievements != null) {
            achievements.removeIf(a -> a.getId().equals(achievementId));
            preferencesManager.saveUserAchievements(achievements);
        }
    }

    // Legacy method - still used for automatic achievement checking
    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // Points-based achievements
        achievements.add(new Achievement(
            "first_points",
            "First Steps",
            "Earn your first 10 points",
            10,
            "points",
            preferencesManager.getCounter() >= 10
        ));

        achievements.add(new Achievement(
            "hundred_points",
            "Century Club",
            "Earn 100 points",
            25,
            "points",
            preferencesManager.getCounter() >= 100
        ));

        achievements.add(new Achievement(
            "five_hundred_points",
            "Point Master",
            "Earn 500 points",
            50,
            "points",
            preferencesManager.getCounter() >= 500
        ));

        achievements.add(new Achievement(
            "thousand_points",
            "Elite Achiever",
            "Earn 1000 points",
            100,
            "points",
            preferencesManager.getCounter() >= 1000
        ));

        // Transaction-based achievements
        List<EnhancedTransaction> transactions = preferencesManager.getTransactions();
        int transactionCount = transactions != null ? transactions.size() : 0;

        achievements.add(new Achievement(
            "first_transaction",
            "Getting Started",
            "Complete your first transaction",
            15,
            "transactions",
            transactionCount >= 1
        ));

        achievements.add(new Achievement(
            "ten_transactions",
            "Active User",
            "Complete 10 transactions",
            30,
            "transactions",
            transactionCount >= 10
        ));

        // Daily streak achievements
        int currentStreak = preferencesManager.getDailyStreak();

        achievements.add(new Achievement(
            "three_day_streak",
            "Consistency",
            "Maintain a 3-day streak",
            40,
            "streak",
            currentStreak >= 3
        ));

        achievements.add(new Achievement(
            "week_streak",
            "Weekly Warrior",
            "Maintain a 7-day streak",
            75,
            "streak",
            currentStreak >= 7
        ));

        return achievements;
    }

    // Check for automatic achievements and award them
    public void checkAchievements(int currentPoints) {
        // DISABLED: Automatic achievement awarding to prevent unwanted points
        // This was causing points to be awarded automatically for system achievements
        // Users should only get points from their own custom achievements

        /* COMMENTED OUT - AUTOMATIC ACHIEVEMENT SYSTEM
        List<Achievement> systemAchievements = getAllAchievements();

        for (Achievement achievement : systemAchievements) {
            if (achievement.isUnlocked() && !preferencesManager.isAchievementAwarded(achievement.getId())) {
                // Award the achievement
                preferencesManager.setAchievementAwarded(achievement.getId(), true);

                // Award points
                currentPoints += achievement.getRewardPoints();
                preferencesManager.setCounter(currentPoints);

                // Record transaction
                preferencesManager.addTransaction(new EnhancedTransaction(
                    "EARN", "Achievement Unlocked", achievement.getName(),
                    achievement.getRewardPoints(), "Achievement"
                ));

                // Show notification
                if (context != null) {
                    Toast.makeText(context, "🏆 Achievement Unlocked: " + achievement.getName() +
                        " (+" + achievement.getRewardPoints() + " points)", Toast.LENGTH_LONG).show();
                }
            }
        }
        */
    }
}
