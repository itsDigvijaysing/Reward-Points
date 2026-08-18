package dev.statup.app.data.local.db.dao

import androidx.room.*
import dev.statup.app.data.local.db.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards WHERE isActive = 1 ORDER BY pointsCost ASC")
    fun getAllActive(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE id = :id")
    suspend fun getById(id: Long): RewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: RewardEntity): Long

    @Update
    suspend fun update(reward: RewardEntity)

    @Query("UPDATE rewards SET timesRedeemed = timesRedeemed + 1 WHERE id = :id")
    suspend fun incrementRedeemed(id: Long)

    @Delete
    suspend fun delete(reward: RewardEntity)

    @Query("DELETE FROM rewards WHERE id = :id")
    suspend fun deleteById(id: Long)
}
