package com.rewardpoints.app.data.local.db.dao

import androidx.room.*
import com.rewardpoints.app.data.local.db.entity.TitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM titles ORDER BY isUnlocked DESC, target ASC")
    fun getAll(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE isUnlocked = 1")
    fun getUnlocked(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE id = :id")
    suspend fun getById(id: String): TitleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(title: TitleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(titles: List<TitleEntity>)

    @Update
    suspend fun update(title: TitleEntity)

    @Query("UPDATE titles SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("UPDATE titles SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun unlock(id: String, unlockedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM titles WHERE id = :id")
    suspend fun deleteById(id: String)
}
