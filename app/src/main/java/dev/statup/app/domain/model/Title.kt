package dev.statup.app.domain.model

data class Title(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String? = null,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val target: Int
) {
    val progressPercent: Float get() = (progress.toFloat() / target).coerceIn(0f, 1f)
}

object Titles {
    val ALL = listOf(
        Title("iron_will", "Iron Will", "Maintain a 30-day streak", "💪", target = 30),
        Title("unbreakable", "Unbreakable", "Maintain a 100-day streak", "🔥", target = 100),
        Title("scholar", "Scholar", "INT reaches 80", "📚", target = 80),
        Title("warrior_spirit", "Warrior Spirit", "STR reaches 80", "⚔️", target = 80),
        Title("renaissance", "Renaissance", "All stats above 50", "🌟", target = 6),
        Title("min_maxer", "Min-Maxer", "Any single stat reaches 100", "💯", target = 100),
        Title("balanced_build", "Balanced Build", "All stats within 10 points of each other", "⚖️", target = 1),
        Title("comeback_king", "Comeback King", "Recover from rank-down 3 times", "👑", target = 3),
        Title("generous_soul", "Generous Soul", "Redeem 50 rewards", "🎁", target = 50)
    )
}
