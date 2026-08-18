package dev.statup.app.domain.model

data class Mission(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val pointsReward: Int,
    val statType: StatType,
    val isDaily: Boolean = true,
    val isCompletedToday: Boolean = false,
    val lastCompletedAt: Long? = null,
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
