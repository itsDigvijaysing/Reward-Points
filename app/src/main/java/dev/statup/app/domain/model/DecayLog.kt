package dev.statup.app.domain.model

data class DecayLog(
    val id: Long = 0,
    val strLost: Int = 0,
    val intLost: Int = 0,
    val wisLost: Int = 0,
    val dexLost: Int = 0,
    val chaLost: Int = 0,
    val vitLost: Int = 0,
    val idleHours: Int? = null,
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun totalLost(): Int = strLost + intLost + wisLost + dexLost + chaLost + vitLost
}
