package com.statup.app.data.local.db.dao

import androidx.room.*
import com.statup.app.data.local.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentByType(type: String, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE statType = :statType ORDER BY createdAt DESC")
    fun getByStatType(statType: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE createdAt >= :startTime AND createdAt < :endTime ORDER BY createdAt DESC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE externalId = :externalId LIMIT 1")
    suspend fun getByExternalId(externalId: String): TransactionEntity?

    @Query("SELECT SUM(points) FROM transactions WHERE type = 'EARN'")
    suspend fun getTotalEarned(): Int?

    @Query("SELECT SUM(points) FROM transactions WHERE type = 'REDEEM'")
    suspend fun getTotalRedeemed(): Int?

    @Query("SELECT IFNULL(SUM(CASE WHEN type = 'EARN' THEN points WHEN type = 'REDEEM' THEN -points ELSE 0 END), 0) FROM transactions")
    fun getBalance(): Flow<Int>

    @Query("SELECT SUM(points) FROM transactions WHERE type = 'EARN' AND createdAt >= :startTime AND createdAt < :endTime")
    suspend fun getEarnedInRange(startTime: Long, endTime: Long): Int?

    @Query("SELECT IFNULL(SUM(points), 0) FROM transactions WHERE type = 'EARN' AND createdAt >= :startTime AND createdAt < :endTime")
    fun observeEarnedInRange(startTime: Long, endTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    /**
     * Insert variant that returns -1 instead of replacing on a unique-constraint conflict
     * (i.e. a transaction with the same externalId already exists). Used by Todoist sync
     * so concurrent runs can't double-award the same completed task.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'EARN' AND (source = 'TODOIST' OR source = 'MISSION')")
    suspend fun getTaskTransactionCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE source = :source AND createdAt >= :startTime AND createdAt < :endTime")
    fun countBySourceInRange(source: String, startTime: Long, endTime: Long): Flow<Int>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
