package com.rewardpoints.app.data.repository

import com.rewardpoints.app.data.local.db.dao.RewardDao
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.local.db.entity.RewardEntity
import com.rewardpoints.app.data.local.db.entity.TransactionEntity
import com.rewardpoints.app.domain.model.Reward
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RewardRepository(
    private val rewardDao: RewardDao,
    private val transactionDao: TransactionDao
) {
    val activeRewards: Flow<List<Reward>> = rewardDao.getAllActive().map { list ->
        list.map { it.toDomain() }
    }

    val allRewards: Flow<List<Reward>> = rewardDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getById(id: Long): Reward? = rewardDao.getById(id)?.toDomain()

    suspend fun createReward(reward: Reward): Long {
        return rewardDao.insert(reward.toEntity())
    }

    suspend fun updateReward(reward: Reward) {
        rewardDao.update(reward.toEntity())
    }

    suspend fun deleteReward(id: Long) {
        rewardDao.deleteById(id)
    }

    suspend fun redeemReward(reward: Reward, currentBalance: Int): Result<Unit> {
        if (currentBalance < reward.pointsCost) {
            return Result.failure(InsufficientPointsException(reward.pointsCost, currentBalance))
        }

        val transaction = TransactionEntity(
            type = TransactionType.REDEEM.name,
            source = TransactionSource.REWARD.name,
            description = "Redeemed: ${reward.name}",
            points = reward.pointsCost,
            statType = null,
            relatedId = reward.id.toString(),
            createdAt = System.currentTimeMillis()
        )
        transactionDao.insert(transaction)
        rewardDao.incrementRedeemed(reward.id)

        return Result.success(Unit)
    }

    private fun RewardEntity.toDomain(): Reward = Reward(
        id = id,
        name = name,
        description = description,
        pointsCost = pointsCost,
        category = category,
        emoji = emoji,
        isActive = isActive,
        createdAt = createdAt,
        timesRedeemed = timesRedeemed
    )

    private fun Reward.toEntity(): RewardEntity = RewardEntity(
        id = id,
        name = name,
        description = description,
        pointsCost = pointsCost,
        category = category,
        emoji = emoji,
        isActive = isActive,
        createdAt = createdAt,
        timesRedeemed = timesRedeemed
    )
}

class InsufficientPointsException(val required: Int, val available: Int) :
    Exception("Insufficient points: need $required, have $available")
