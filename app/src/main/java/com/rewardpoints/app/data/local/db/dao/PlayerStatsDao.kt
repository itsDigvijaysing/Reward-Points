package com.rewardpoints.app.data.local.db.dao

import androidx.room.*
import com.rewardpoints.app.data.local.db.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun getStats(): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getStatsOnce(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: PlayerStatsEntity)

    @Update
    suspend fun update(stats: PlayerStatsEntity)

    @Query("UPDATE player_stats SET strStat = :value WHERE id = 1")
    suspend fun updateStr(value: Int)

    @Query("UPDATE player_stats SET intStat = :value WHERE id = 1")
    suspend fun updateInt(value: Int)

    @Query("UPDATE player_stats SET wisStat = :value WHERE id = 1")
    suspend fun updateWis(value: Int)

    @Query("UPDATE player_stats SET dexStat = :value WHERE id = 1")
    suspend fun updateDex(value: Int)

    @Query("UPDATE player_stats SET chaStat = :value WHERE id = 1")
    suspend fun updateCha(value: Int)

    @Query("UPDATE player_stats SET vitStat = :value WHERE id = 1")
    suspend fun updateVit(value: Int)

    @Query("UPDATE player_stats SET streak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END, lastActivityAt = :activityAt, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStreak(streak: Int, activityAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_stats SET rank = :rank, rankUpStreakCounter = 0, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateRank(rank: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_stats SET rankUpStreakCounter = :counter WHERE id = 1")
    suspend fun updateRankUpCounter(counter: Int)

    @Query("UPDATE player_stats SET totalPointsEarned = totalPointsEarned + :points WHERE id = 1")
    suspend fun addPoints(points: Int)

    @Query("DELETE FROM player_stats")
    suspend fun deleteAll()
}
