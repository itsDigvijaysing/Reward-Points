package com.rewardpoints.app.data.repository

import com.rewardpoints.app.data.local.db.dao.TitleDao
import com.rewardpoints.app.data.local.db.entity.TitleEntity
import com.rewardpoints.app.domain.model.Achievement
import com.rewardpoints.app.domain.model.AchievementCategory
import com.rewardpoints.app.domain.model.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepository(
    private val titleDao: TitleDao
) {
    val achievements: Flow<List<Achievement>> = titleDao.getAll().map { entities ->
        entities.map { entity ->
            val template = Achievements.getById(entity.id)
            Achievement(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                emoji = entity.emoji ?: template?.emoji ?: "🏆",
                category = template?.category ?: AchievementCategory.SPECIAL,
                target = entity.target,
                progress = entity.progress,
                isUnlocked = entity.isUnlocked,
                unlockedAt = entity.unlockedAt
            )
        }
    }

    val unlockedAchievements: Flow<List<Achievement>> = titleDao.getUnlocked().map { entities ->
        entities.map { entity ->
            val template = Achievements.getById(entity.id)
            Achievement(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                emoji = entity.emoji ?: template?.emoji ?: "🏆",
                category = template?.category ?: AchievementCategory.SPECIAL,
                target = entity.target,
                progress = entity.progress,
                isUnlocked = entity.isUnlocked,
                unlockedAt = entity.unlockedAt
            )
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
                        target = achievement.target
                    )
                )
            }
        }
    }

    suspend fun updateProgress(achievementId: String, progress: Int) {
        val achievement = titleDao.getById(achievementId) ?: return
        if (achievement.isUnlocked) return

        titleDao.updateProgress(achievementId, progress)

        if (progress >= achievement.target) {
            titleDao.unlock(achievementId)
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

    suspend fun unlockDirectly(achievementId: String) {
        titleDao.unlock(achievementId)
    }

    suspend fun createCustomAchievement(
        name: String,
        description: String,
        emoji: String,
        category: AchievementCategory,
        target: Int
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
                target = target
            )
        )
    }

    suspend fun deleteAchievement(achievementId: String) {
        titleDao.deleteById(achievementId)
    }

    suspend fun getAchievement(id: String): Achievement? {
        val entity = titleDao.getById(id) ?: return null
        val template = Achievements.getById(entity.id)
        return Achievement(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            emoji = entity.emoji ?: template?.emoji ?: "🏆",
            category = template?.category ?: AchievementCategory.SPECIAL,
            target = entity.target,
            progress = entity.progress,
            isUnlocked = entity.isUnlocked,
            unlockedAt = entity.unlockedAt
        )
    }
}
