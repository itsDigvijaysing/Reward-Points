package com.statup.app.data.repository

import androidx.room.withTransaction
import com.statup.app.data.local.db.AppDatabase
import com.statup.app.data.local.db.dao.TitleDao
import com.statup.app.data.local.db.entity.TitleEntity
import com.statup.app.domain.model.Achievement
import com.statup.app.domain.model.AchievementCategory
import com.statup.app.domain.model.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepository(
    private val database: AppDatabase,
    private val titleDao: TitleDao,
    /**
     * Optional points-award hook. Provided lazily as a suspend lambda to avoid a circular
     * Koin dependency (AchievementTracker → AchievementRepository → PointsRepository →
     * AchievementTracker). When non-null, [updateProgress] awards `rewardPoints` on first
     * unlock.
     */
    private val pointsAwarder: suspend (id: String, points: Int) -> Unit = { _, _ -> }
) {
    val achievements: Flow<List<Achievement>> = titleDao.getAll().map { entities ->
        entities.map { entity ->
            val template = Achievements.getById(entity.id)
            entity.toAchievement(template)
        }
    }

    val unlockedAchievements: Flow<List<Achievement>> = titleDao.getUnlocked().map { entities ->
        entities.map { entity ->
            val template = Achievements.getById(entity.id)
            entity.toAchievement(template)
        }
    }

    suspend fun initializeAchievements() {
        Achievements.ALL.forEach { achievement ->
            val existing = titleDao.getById(achievement.id)
            if (existing == null) {
                titleDao.insert(
                    TitleEntity(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        emoji = achievement.emoji,
                        isUnlocked = false,
                        unlockedAt = null,
                        progress = 0,
                        target = achievement.target,
                        rewardPoints = achievement.rewardPoints
                    )
                )
            }
        }
    }

    /**
     * Update progress for [achievementId]. If the new progress crosses the target the
     * achievement is unlocked and its `rewardPoints` are awarded via [pointsAwarder].
     *
     * The read-then-unlock-then-award sequence runs inside `database.withTransaction` so
     * two concurrent earns that both cross the threshold (e.g. Todoist sync + a manual
     * action in the same instant) can't both observe `isUnlocked=false` and double-award
     * the reward. The award call itself happens outside the transaction — `pointsAwarder`
     * goes through `PointsRepository.addPoints` which opens its own transaction; nested
     * Room transactions are safe but holding ours open across an unrelated insert is not
     * worth it.
     */
    suspend fun updateProgress(achievementId: String, progress: Int) {
        val unlockedNow: Int = database.withTransaction {
            val achievement = titleDao.getById(achievementId) ?: return@withTransaction 0
            if (achievement.isUnlocked) return@withTransaction 0

            titleDao.updateProgress(achievementId, progress)

            if (progress >= achievement.target) {
                titleDao.unlock(achievementId)
                if (achievement.rewardPoints > 0) achievement.rewardPoints
                    else Achievements.getById(achievementId)?.displayRewardPoints ?: 0
            } else 0
        }
        if (unlockedNow > 0) {
            pointsAwarder(achievementId, unlockedNow)
        }
    }

    suspend fun checkAndUnlock(achievementId: String): Boolean {
        val achievement = titleDao.getById(achievementId) ?: return false
        if (achievement.isUnlocked) return false

        if (achievement.progress >= achievement.target) {
            titleDao.unlock(achievementId)
            return true
        }
        return false
    }

    /**
     * Unlock [achievementId] directly (e.g. user taps "mark complete"). Awards its
     * `rewardPoints` on first unlock, consistent with [updateProgress]; the in-transaction
     * `isUnlocked` guard prevents a double-award if called again.
     */
    suspend fun unlockDirectly(achievementId: String) {
        val awardPoints: Int = database.withTransaction {
            val achievement = titleDao.getById(achievementId) ?: return@withTransaction 0
            if (achievement.isUnlocked) return@withTransaction 0
            titleDao.unlock(achievementId)
            if (achievement.rewardPoints > 0) achievement.rewardPoints
                else Achievements.getById(achievementId)?.displayRewardPoints ?: 0
        }
        if (awardPoints > 0) {
            pointsAwarder(achievementId, awardPoints)
        }
    }

    suspend fun createCustomAchievement(
        name: String,
        description: String,
        emoji: String,
        category: AchievementCategory,
        target: Int,
        rewardPoints: Int = 0
    ) {
        val id = "custom_${System.currentTimeMillis()}"
        titleDao.insert(
            TitleEntity(
                id = id,
                name = name,
                description = description,
                emoji = emoji,
                isUnlocked = false,
                unlockedAt = null,
                progress = 0,
                target = target,
                rewardPoints = rewardPoints
            )
        )
    }

    suspend fun deleteAchievement(achievementId: String) {
        titleDao.deleteById(achievementId)
    }

    suspend fun getAchievement(id: String): Achievement? {
        val entity = titleDao.getById(id) ?: return null
        val template = Achievements.getById(entity.id)
        return entity.toAchievement(template)
    }

    private fun TitleEntity.toAchievement(template: Achievement?): Achievement = Achievement(
        id = id,
        name = name,
        description = description,
        emoji = emoji ?: template?.emoji ?: "🏆",
        category = template?.category ?: AchievementCategory.SPECIAL,
        target = target,
        progress = progress,
        isUnlocked = isUnlocked,
        unlockedAt = unlockedAt,
        rewardPoints = rewardPoints
    )
}
