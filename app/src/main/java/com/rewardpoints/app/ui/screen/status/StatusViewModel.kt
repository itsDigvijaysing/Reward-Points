package com.rewardpoints.app.ui.screen.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.rpg.RankCalculator
import com.rewardpoints.app.rpg.StatsEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class StatusViewModel(
    private val playerRepository: PlayerRepository,
    private val pointsRepository: PointsRepository,
    private val rankCalculator: RankCalculator,
    private val achievementTracker: AchievementTracker,
    private val userPreferences: UserPreferences,
    private val transactionDao: TransactionDao
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
                    previousRank?.let { prev ->
                        if (it.rank.order > prev.order) {
                            _rankUpEvent.emit(it.rank)
                        }
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

        viewModelScope.launch {
            observeMoodCheckedInToday()
        }
    }

    private suspend fun observeMoodCheckedInToday() {
        val (start, end) = todayMillisRange()
        transactionDao.countBySourceInRange(TransactionSource.MOOD.name, start, end)
            .collect { count ->
                _uiState.update { it.copy(hasCheckedInMoodToday = count > 0) }
            }
    }

    private fun todayMillisRange(): Pair<Long, Long> {
        val start = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return start to (start + 86_400_000L)
    }

    private suspend fun loadTodayPoints() {
        val (start, end) = todayMillisRange()
        // SQL-side SUM — was filtering the entire transactions list in memory on every emission,
        // which scaled O(N) with history. Now O(1) DB-side aggregate.
        transactionDao.observeEarnedInRange(start, end).collect { todayPoints ->
            _uiState.update { it.copy(todayPoints = todayPoints) }
        }
    }

    private suspend fun loadCurrentBalance() {
        pointsRepository.balanceFlow.collect { balance ->
            _uiState.update { it.copy(currentBalance = balance) }
        }
    }

    fun checkInMood(mood: String) {
        // Guard: only one mood check-in per local day.
        if (_uiState.value.hasCheckedInMoodToday) return
        viewModelScope.launch {
            // Re-check inside the coroutine to close a race against a concurrent first-tap.
            val (start, end) = todayMillisRange()
            val alreadyToday = transactionDao.countBySourceInRange(TransactionSource.MOOD.name, start, end).first() > 0
            if (alreadyToday) return@launch

            pointsRepository.addPoints(
                points = StatsEngine.MOOD_POINTS,
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

}

data class StatusUiState(
    val username: String = "Player",
    val stats: PlayerStats = PlayerStats(),
    val todayPoints: Int = 0,
    val currentBalance: Int = 0,
    val hexagonStyle: String = "simple",
    val hasCheckedInMoodToday: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
