package com.statup.app.data.local.db.dao

import androidx.room.*
import com.statup.app.data.local.db.entity.AiMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMemoryDao {
    @Query("SELECT * FROM ai_memory ORDER BY lastAccessedAt DESC")
    fun getAll(): Flow<List<AiMemoryEntity>>

    @Query("SELECT * FROM ai_memory WHERE category = :category ORDER BY lastAccessedAt DESC")
    fun getByCategory(category: String): Flow<List<AiMemoryEntity>>

    @Query("SELECT * FROM ai_memory ORDER BY lastAccessedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AiMemoryEntity>

    @Query("SELECT COUNT(*) FROM ai_memory")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: AiMemoryEntity): Long

    @Update
    suspend fun update(memory: AiMemoryEntity)

    @Query("UPDATE ai_memory SET lastAccessedAt = :accessedAt, accessCount = accessCount + 1 WHERE id = :id")
    suspend fun markAccessed(id: Long, accessedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM ai_memory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ai_memory WHERE id IN (SELECT id FROM ai_memory ORDER BY lastAccessedAt ASC LIMIT :count)")
    suspend fun deleteOldest(count: Int)

    @Query("DELETE FROM ai_memory")
    suspend fun deleteAll()
}
