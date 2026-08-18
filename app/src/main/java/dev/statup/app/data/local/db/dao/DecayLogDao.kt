package dev.statup.app.data.local.db.dao

import androidx.room.*
import dev.statup.app.data.local.db.entity.DecayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DecayLogDao {
    @Query("SELECT * FROM decay_log ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DecayLogEntity>>

    @Query("SELECT * FROM decay_log ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DecayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DecayLogEntity): Long

    @Query("DELETE FROM decay_log")
    suspend fun deleteAll()
}
