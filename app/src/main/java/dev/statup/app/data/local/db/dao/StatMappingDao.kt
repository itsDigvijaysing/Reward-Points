package dev.statup.app.data.local.db.dao

import androidx.room.*
import dev.statup.app.data.local.db.entity.StatMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatMappingDao {
    @Query("SELECT * FROM stat_mappings ORDER BY sourceName ASC")
    fun getAll(): Flow<List<StatMappingEntity>>

    @Query("SELECT * FROM stat_mappings")
    suspend fun getAllOnce(): List<StatMappingEntity>

    @Query("SELECT * FROM stat_mappings WHERE sourceType = :sourceType")
    fun getBySourceType(sourceType: String): Flow<List<StatMappingEntity>>

    @Query("SELECT * FROM stat_mappings WHERE sourceId = :sourceId AND sourceType = :sourceType")
    suspend fun getBySource(sourceId: String, sourceType: String): StatMappingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: StatMappingEntity): Long

    @Update
    suspend fun update(mapping: StatMappingEntity)

    @Delete
    suspend fun delete(mapping: StatMappingEntity)

    @Query("DELETE FROM stat_mappings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
