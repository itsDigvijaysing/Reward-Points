package com.rewardpoints.app.domain.model

import androidx.compose.ui.graphics.Color
import com.rewardpoints.app.ui.theme.*

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: AchievementCategory,
    val target: Int,
    val progress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val rewardPoints: Int = 0
) {
    val isNoGoal: Boolean get() = target == 0
    val progressPercent: Float get() = if (target <= 0) 0f else (progress.toFloat() / target).coerceIn(0f, 1f)
    val displayRewardPoints: Int get() = if (rewardPoints > 0) rewardPoints else when {
        isNoGoal -> 10
        target >= 100 -> 50
        target >= 50 -> 30
        target >= 25 -> 20
        target >= 10 -> 15
        else -> 10
    }
}

enum class AchievementCategory(val displayName: String, val color: Color, val emoji: String) {
    STREAK("Streak Master", AccentWarning, "🔥"),
    POINTS("Point Collector", PointsGold, "✨"),
    STATS("Stat Builder", AccentPrimary, "📊"),
    TASKS("Task Crusher", AccentSuccess, "✅"),
    RANK("Rank Climber", StatCHA, "⭐"),
    SPECIAL("Special", AccentSecondary, "🏆")
}

object Achievements {
    val ALL = listOf(
        // Streak
        Achievement("streak_7", "Week Warrior", "Maintain a 7-day streak", "🔥", AchievementCategory.STREAK, 7),
        Achievement("streak_30", "Monthly Master", "Maintain a 30-day streak", "🔥", AchievementCategory.STREAK, 30),

        // Points
        Achievement("points_100", "First Steps", "Earn 100 total points", "✨", AchievementCategory.POINTS, 100),
        Achievement("points_1000", "Thousand Club", "Earn 1,000 total points", "💰", AchievementCategory.POINTS, 1000),

        // Stats
        Achievement("stat_50", "Half Century", "Raise any stat to 50", "📊", AchievementCategory.STATS, 50),
        Achievement("stat_max", "Maxed Out", "Raise any stat to 100", "🌟", AchievementCategory.STATS, 100),

        // Tasks
        Achievement("tasks_10", "Task Beginner", "Complete 10 tasks", "✅", AchievementCategory.TASKS, 10),
        Achievement("tasks_100", "Task Centurion", "Complete 100 tasks", "🎯", AchievementCategory.TASKS, 100),

        // Rank
        Achievement("rank_b", "B-Rank Hunter", "Reach B Rank", "⭐", AchievementCategory.RANK, 3),
        Achievement("rank_s", "S-Rank Hunter", "Reach S Rank", "👑", AchievementCategory.RANK, 5),

        // Special
        Achievement("first_reward", "Treat Yourself", "Redeem your first reward", "🎁", AchievementCategory.SPECIAL, 1),
        Achievement("todoist_connect", "Connected", "Connect to Todoist", "📋", AchievementCategory.SPECIAL, 1)
    )

    fun getById(id: String): Achievement? = ALL.find { it.id == id }
}
