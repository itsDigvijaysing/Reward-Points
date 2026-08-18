package dev.statup.app.data.local.db.dao

import androidx.room.*
import dev.statup.app.data.local.db.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions ORDER BY createdAt DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE isDaily = 1")
    fun getDailyMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getById(id: Long): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: MissionEntity): Long

    @Update
    suspend fun update(mission: MissionEntity)

    @Query("UPDATE missions SET isCompletedToday = :completed, lastCompletedAt = :completedAt, streak = streak + 1 WHERE id = :id")
    suspend fun markCompleted(id: Long, completed: Boolean = true, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE missions SET isCompletedToday = 0")
    suspend fun resetDailyCompletions()

    @Delete
    suspend fun delete(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
