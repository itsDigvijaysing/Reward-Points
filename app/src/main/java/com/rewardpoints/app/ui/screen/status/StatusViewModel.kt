package com.rewardpoints.app.ui.screen.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.rpg.RankCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class StatusViewModel(
    private val playerRepository: PlayerRepository,
    private val pointsRepository: PointsRepository,
    private val rankCalculator: RankCalculator,
    private val achievementTracker: AchievementTracker,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    private val _rankUpEvent = MutableSharedFlow<Rank>()
    val rankUpEvent: SharedFlow<Rank> = _rankUpEvent.asSharedFlow()

    private var previousRank: Rank? = null

    init {
        viewModelScope.launch {
            combine(
                playerRepository.username,
                playerRepository.playerStats,
                userPreferences.hexagonStyle
            ) { username, stats, hexStyle ->
                // Check for rank up
                stats?.let {
                    if (previousRank != null && it.rank.order > previousRank!!.order) {
                        _rankUpEvent.emit(it.rank)
                    }
                    previousRank = it.rank
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        username = username,
                        stats = stats ?: PlayerStats(),
                        hexagonStyle = hexStyle,
                        isLoading = false
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            loadTodayPoints()
        }

        viewModelScope.launch {
            loadCurrentBalance()
        }
    }

    private suspend fun loadTodayPoints() {
        val todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val todayEnd = todayStart + 86400000 // 24 hours

        pointsRepository.transactions.collect { transactions ->
            val todayPoints = transactions
                .filter { it.createdAt >= todayStart && it.createdAt < todayEnd }
                .filter { it.type == TransactionType.EARN }
                .sumOf { it.points }

            _uiState.update { it.copy(todayPoints = todayPoints) }
        }
    }

    private suspend fun loadCurrentBalance() {
        pointsRepository.balanceFlow.collect { balance ->
            _uiState.update { it.copy(currentBalance = balance) }
        }
    }

    fun checkInMood(mood: String) {
        viewModelScope.launch {
            pointsRepository.addPoints(
                points = 2,
                type = TransactionType.EARN,
                source = TransactionSource.MOOD,
                description = "Mood check-in: $mood",
                statType = StatType.WIS,
                relatedId = null
            )
            achievementTracker.onMoodCheckedIn()
            achievementTracker.onPointsEarned(TransactionSource.MOOD)
        }
    }

    fun addManualPoints(points: Int, statType: StatType, description: String) {
        viewModelScope.launch {
            pointsRepository.addPoints(
                points = points,
                type = TransactionType.EARN,
                source = TransactionSource.MANUAL,
                description = description,
                statType = statType,
                relatedId = null
            )
            achievementTracker.onPointsEarned(TransactionSource.MANUAL)
        }
    }

    fun getDaysToRankUp(): Int {
        val stats = _uiState.value.stats
        return rankCalculator.getStreakDaysToNextRank(stats)
    }

    // For testing rank-up animation
    fun simulateRankUp() {
        viewModelScope.launch {
            val currentRank = _uiState.value.stats.rank
            currentRank.nextRank()?.let { newRank ->
                _rankUpEvent.emit(newRank)
            }
        }
    }
}

data class StatusUiState(
    val username: String = "Player",
    val stats: PlayerStats = PlayerStats(),
    val todayPoints: Int = 0,
    val currentBalance: Int = 0,
    val hexagonStyle: String = "simple",
    val isLoading: Boolean = true,
    val error: String? = null
)
