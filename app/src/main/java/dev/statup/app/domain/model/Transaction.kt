package dev.statup.app.domain.model

enum class TransactionType {
    EARN, REDEEM
}

enum class TransactionSource {
    TODOIST, MANUAL, MISSION, MOOD, REWARD
}

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val source: TransactionSource,
    val description: String? = null,
    val points: Int,
    val statType: StatType? = null,
    val relatedId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
