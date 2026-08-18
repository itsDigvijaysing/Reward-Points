package com.statup.app.ui.screen.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statup.app.data.repository.AchievementRepository
import com.statup.app.domain.model.Achievement
import com.statup.app.domain.model.AchievementCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            achievementRepository.achievements.collect { achievements ->
                val unlocked = achievements.count { it.isUnlocked }
                val total = achievements.size

                _uiState.update {
                    it.copy(
                        achievements = achievements,
                        unlockedCount = unlocked,
                        totalCount = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createAchievement(
        name: String,
        description: String,
        emoji: String,
        category: AchievementCategory,
        target: Int,
        rewardPoints: Int
    ) {
        viewModelScope.launch {
            achievementRepository.createCustomAchievement(name, description, emoji, category, target, rewardPoints)
            hideCreateDialog()
        }
    }

    fun deleteAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementRepository.deleteAchievement(achievement.id)
        }
    }

    fun completeAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementRepository.unlockDirectly(achievement.id)
        }
    }
}

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false
) {
    val completionPercent: Float
        get() = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
}
