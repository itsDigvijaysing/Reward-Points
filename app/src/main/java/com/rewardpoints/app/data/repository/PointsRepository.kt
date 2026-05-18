package com.rewardpoints.app.data.repository

import androidx.room.withTransaction
import com.rewardpoints.app.data.local.db.AppDatabase
import com.rewardpoints.app.data.local.db.dao.PlayerStatsDao
import com.rewardpoints.app.data.local.db.dao.StatMappingDao
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.local.db.entity.StatMappingEntity
import com.rewardpoints.app.data.local.db.entity.TransactionEntity
import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.domain.model.*
import com.rewardpoints.app.widget.StatsWidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PointsRepository(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val playerStatsDao: PlayerStatsDao,
    private val statMappingDao: StatMappingDao,
    private val userPreferences: UserPreferences,
    private val widgetUpdater: StatsWidgetUpdater? = null
) {
    val transactions: Flow<List<Transaction>> = transactionDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    val balanceFlow: Flow<Int> = transactionDao.getBalance()

    fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        transactionDao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    suspend fun getTotalEarned(): Int = transactionDao.getTotalEarned() ?: 0
    suspend fun getTotalRedeemed(): Int = transactionDao.getTotalRedeemed() ?: 0
    suspend fun getCurrentBalance(): Int = getTotalEarned() - getTotalRedeemed()
    suspend fun getTaskTransactionCount(): Int = transactionDao.getTaskTransactionCount()

    /**
     * Insert the transaction, increment totals + stat accumulator. All DB writes happen
     * inside a single Room transaction so concurrent earns can't tear a stat update
     * apart from its accumulator/total counterpart.
     */
    suspend fun addPoints(
        points: Int,
        type: TransactionType,
        source: TransactionSource,
        description: String? = null,
        statType: StatType? = null,
        relatedId: String? = null,
        externalId: String? = null
    ): Transaction = database.withTransaction {
        val transaction = TransactionEntity(
            type = type.name,
            source = source.name,
            description = description,
            points = points,
            statType = statType?.name,
            relatedId = relatedId,
            externalId = externalId,
            createdAt = System.currentTimeMillis()
        )
        val id = transactionDao.insert(transaction)

        if (type == TransactionType.EARN) {
            playerStatsDao.addPoints(points)
            if (statType != null) {
                updateStatAccumulator(statType, points)
            }
        }

        widgetUpdater?.refresh()
        transaction.copy(id = id).toDomain()
    }

    suspend fun earnPoints(
        points: Int,
        statType: StatType? = null,
        source: TransactionSource,
        description: String? = null,
        relatedId: String? = null,
        externalId: String? = null
    ): Transaction {
        return addPoints(points, TransactionType.EARN, source, description, statType, relatedId, externalId)
    }

    /**
     * Idempotent earn keyed by [externalId] — used by Todoist sync. Returns the new
     * transaction on first call, or null if a transaction with the same externalId
     * already exists (race winner / prior sync run). The unique index on
     * `transactions.externalId` plus `OnConflictStrategy.IGNORE` makes the check race-safe
     * even when two sync runs overlap.
     */
    suspend fun tryEarnExternalPoints(
        externalId: String,
        points: Int,
        statType: StatType? = null,
        source: TransactionSource,
        description: String? = null,
        relatedId: String? = externalId
    ): Transaction? = database.withTransaction {
        val transaction = TransactionEntity(
            type = TransactionType.EARN.name,
            source = source.name,
            description = description,
            points = points,
            statType = statType?.name,
            relatedId = relatedId,
            externalId = externalId,
            createdAt = System.currentTimeMillis()
        )
        val id = transactionDao.insertIgnore(transaction)
        if (id == -1L) return@withTransaction null

        playerStatsDao.addPoints(points)
        if (statType != null) {
            updateStatAccumulator(statType, points)
        }
        widgetUpdater?.refresh()
        transaction.copy(id = id).toDomain()
    }

    suspend fun redeemPoints(
        points: Int,
        description: String? = null,
        relatedId: String? = null
    ): Transaction = database.withTransaction {
        val transaction = TransactionEntity(
            type = TransactionType.REDEEM.name,
            source = TransactionSource.REWARD.name,
            description = description,
            points = points,
            statType = null,
            relatedId = relatedId,
            createdAt = System.currentTimeMillis()
        )
        val id = transactionDao.insert(transaction)
        widgetUpdater?.refresh()
        transaction.copy(id = id).toDomain()
    }

    private suspend fun updateStatAccumulator(statType: StatType, points: Int) {
        val stats = playerStatsDao.getStatsOnce() ?: return
        val currentAcc = when (statType) {
            StatType.STR -> stats.strPointsAcc
            StatType.INT -> stats.intPointsAcc
            StatType.WIS -> stats.wisPointsAcc
            StatType.DEX -> stats.dexPointsAcc
            StatType.CHA -> stats.chaPointsAcc
            StatType.VIT -> stats.vitPointsAcc
        }

        val newAcc = currentAcc + points
        val statGain = newAcc / PlayerStats.POINTS_PER_STAT
        val remainingAcc = newAcc % PlayerStats.POINTS_PER_STAT

        val currentStat = when (statType) {
            StatType.STR -> stats.strStat
            StatType.INT -> stats.intStat
            StatType.WIS -> stats.wisStat
            StatType.DEX -> stats.dexStat
            StatType.CHA -> stats.chaStat
            StatType.VIT -> stats.vitStat
        }

        val newStat = (currentStat + statGain).coerceAtMost(PlayerStats.MAX_STAT)

        val updatedStats = when (statType) {
            StatType.STR -> stats.copy(strStat = newStat, strPointsAcc = remainingAcc)
            StatType.INT -> stats.copy(intStat = newStat, intPointsAcc = remainingAcc)
            StatType.WIS -> stats.copy(wisStat = newStat, wisPointsAcc = remainingAcc)
            StatType.DEX -> stats.copy(dexStat = newStat, dexPointsAcc = remainingAcc)
            StatType.CHA -> stats.copy(chaStat = newStat, chaPointsAcc = remainingAcc)
            StatType.VIT -> stats.copy(vitStat = newStat, vitPointsAcc = remainingAcc)
        }

        playerStatsDao.update(updatedStats.copy(
            lastActivityAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun routeToStat(labels: List<String>): StatType {
        return routeToStatCached(labels, statMappingDao.getAllOnce())
    }

    /** Snapshot of stat mappings, suitable for reuse across a sync run. */
    suspend fun loadStatMappings(): List<StatMappingEntity> = statMappingDao.getAllOnce()

    /** Routing variant that reuses a pre-loaded mappings list to avoid N DB round trips. */
    suspend fun routeToStatCached(labels: List<String>, mappings: List<StatMappingEntity>): StatType {
        for (label in labels) {
            val mapping = mappings.find { it.sourceName.equals(label, ignoreCase = true) }
            if (mapping != null) {
                return StatType.fromString(mapping.statType) ?: getDefaultStat()
            }
        }
        return getDefaultStat()
    }

    private suspend fun getDefaultStat(): StatType {
        val defaultStatName = userPreferences.defaultStat.first()
        return StatType.fromString(defaultStatName) ?: StatType.INT
    }

    private fun TransactionEntity.toDomain(): Transaction = Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        source = TransactionSource.valueOf(source),
        description = description,
        points = points,
        statType = statType?.let { StatType.fromString(it) },
        relatedId = relatedId,
        createdAt = createdAt
    )
}
