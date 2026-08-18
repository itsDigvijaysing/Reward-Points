package dev.statup.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.statup.app.domain.model.PlayerStats

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val strStat: Int = PlayerStats.BASE_STAT,
    val intStat: Int = PlayerStats.BASE_STAT,
    val wisStat: Int = PlayerStats.BASE_STAT,
    val dexStat: Int = PlayerStats.BASE_STAT,
    val chaStat: Int = PlayerStats.BASE_STAT,
    val vitStat: Int = PlayerStats.BASE_STAT,
    val strPointsAcc: Int = 0,
    val intPointsAcc: Int = 0,
    val wisPointsAcc: Int = 0,
    val dexPointsAcc: Int = 0,
    val chaPointsAcc: Int = 0,
    val vitPointsAcc: Int = 0,
    val totalPointsEarned: Int = 0,
    val rank: String = "E",
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val rankUpStreakCounter: Int = 0,
    val rankDownBreakCounter: Int = 0,
    // Streak Freeze Shields owned (v5). Consumed automatically on idle days.
    val streakShields: Int = 0,
    val lastActivityAt: Long?,
    val updatedAt: Long
)
