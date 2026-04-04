package com.rewardpoints.app.ui.screen.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.data.repository.RewardRepository
import com.rewardpoints.app.domain.model.Reward
import com.rewardpoints.app.rpg.AchievementTracker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val rewardRepository: RewardRepository,
    private val pointsRepository: PointsRepository,
    private val achievementTracker: AchievementTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rewardRepository.activeRewards.collect { rewards ->
                _uiState.update { it.copy(rewards = rewards, isLoading = false) }
            }
        }

        viewModelScope.launch {
            pointsRepository.balanceFlow.collect { balance ->
                _uiState.update { it.copy(currentBalance = balance) }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createReward(name: String, description: String?, cost: Int, emoji: String, category: String?) {
        viewModelScope.launch {
            val reward = Reward(
                name = name,
                description = description,
                pointsCost = cost,
                emoji = emoji,
                category = category ?: "General"
            )
            rewardRepository.createReward(reward)
            hideCreateDialog()
        }
    }

    fun redeemReward(reward: Reward) {
        viewModelScope.launch {
            val result = rewardRepository.redeemReward(reward, _uiState.value.currentBalance)
            result.onSuccess {
                achievementTracker.onRewardRedeemed()
                _uiState.update {
                    it.copy(redeemSuccess = reward.name)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun deleteReward(reward: Reward) {
        viewModelScope.launch {
            rewardRepository.deleteReward(reward.id)
        }
    }

    fun clearRedeemSuccess() {
        _uiState.update { it.copy(redeemSuccess = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class RewardsUiState(
    val rewards: List<Reward> = emptyList(),
    val currentBalance: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val redeemSuccess: String? = null,
    val showCreateDialog: Boolean = false
)
