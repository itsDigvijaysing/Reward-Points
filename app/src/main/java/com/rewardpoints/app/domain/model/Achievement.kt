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
    val unlockedAt: Long? = null
) {
    val progressPercent: Float get() = (progress.toFloat() / target).coerceIn(0f, 1f)
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
        // Streak achievements
        Achievement("streak_3", "Getting Started", "Maintain a 3-day streak", "🔥", AchievementCategory.STREAK, 3),
        Achievement("streak_7", "Week Warrior", "Maintain a 7-day streak", "🔥", AchievementCategory.STREAK, 7),
        Achievement("streak_14", "Fortnight Fighter", "Maintain a 14-day streak", "🔥", AchievementCategory.STREAK, 14),
        Achievement("streak_30", "Monthly Master", "Maintain a 30-day streak", "🔥", AchievementCategory.STREAK, 30),
        Achievement("streak_100", "Centurion", "Maintain a 100-day streak", "💯", AchievementCategory.STREAK, 100),

        // Points achievements
        Achievement("points_100", "First Steps", "Earn 100 total points", "✨", AchievementCategory.POINTS, 100),
        Achievement("points_500", "Point Hoarder", "Earn 500 total points", "✨", AchievementCategory.POINTS, 500),
        Achievement("points_1000", "Thousand Club", "Earn 1,000 total points", "💰", AchievementCategory.POINTS, 1000),
        Achievement("points_5000", "Point Master", "Earn 5,000 total points", "💎", AchievementCategory.POINTS, 5000),

        // Stat achievements
        Achievement("stat_20", "Stat Starter", "Raise any stat to 20", "📊", AchievementCategory.STATS, 20),
        Achievement("stat_50", "Half Century", "Raise any stat to 50", "📊", AchievementCategory.STATS, 50),
        Achievement("stat_max", "Maxed Out", "Raise any stat to 100", "🌟", AchievementCategory.STATS, 100),
        Achievement("balanced", "Balance Master", "Get all stats to 25+", "⚖️", AchievementCategory.STATS, 25),

        // Task achievements
        Achievement("tasks_10", "Task Beginner", "Complete 10 tasks", "✅", AchievementCategory.TASKS, 10),
        Achievement("tasks_50", "Task Veteran", "Complete 50 tasks", "✅", AchievementCategory.TASKS, 50),
        Achievement("tasks_100", "Task Centurion", "Complete 100 tasks", "🎯", AchievementCategory.TASKS, 100),
        Achievement("tasks_500", "Task Legend", "Complete 500 tasks", "👑", AchievementCategory.TASKS, 500),

        // Rank achievements
        Achievement("rank_d", "D-Rank Hunter", "Reach D Rank", "⭐", AchievementCategory.RANK, 1),
        Achievement("rank_c", "C-Rank Hunter", "Reach C Rank", "⭐", AchievementCategory.RANK, 2),
        Achievement("rank_b", "B-Rank Hunter", "Reach B Rank", "⭐", AchievementCategory.RANK, 3),
        Achievement("rank_a", "A-Rank Hunter", "Reach A Rank", "⭐", AchievementCategory.RANK, 4),
        Achievement("rank_s", "S-Rank Hunter", "Reach S Rank", "👑", AchievementCategory.RANK, 5),

        // Special achievements
        Achievement("mood_7", "Mood Tracker", "Log mood 7 days in a row", "😊", AchievementCategory.SPECIAL, 7),
        Achievement("first_reward", "Treat Yourself", "Redeem your first reward", "🎁", AchievementCategory.SPECIAL, 1),
        Achievement("todoist_connect", "Connected", "Connect to Todoist", "📋", AchievementCategory.SPECIAL, 1)
    )

    fun getById(id: String): Achievement? = ALL.find { it.id == id }
}
