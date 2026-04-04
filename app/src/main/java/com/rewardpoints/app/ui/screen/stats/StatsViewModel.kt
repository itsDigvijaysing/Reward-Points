package com.rewardpoints.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.domain.model.Transaction
import com.rewardpoints.app.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class StatsViewModel(
    private val playerRepository: PlayerRepository,
    private val pointsRepository: PointsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.playerStats.collect { stats ->
                _uiState.update { it.copy(stats = stats ?: PlayerStats()) }
            }
        }

        viewModelScope.launch {
            pointsRepository.transactions.collect { transactions ->
                val earnTransactions = transactions.filter { it.type == TransactionType.EARN }
                val statBreakdown = calculateStatBreakdown(earnTransactions)
                val weeklyData = calculateWeeklyData(earnTransactions)
                val topSources = calculateTopSources(earnTransactions)

                _uiState.update {
                    it.copy(
                        statBreakdown = statBreakdown,
                        weeklyData = weeklyData,
                        topSources = topSources,
                        totalTransactions = earnTransactions.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateStatBreakdown(transactions: List<Transaction>): Map<StatType, StatBreakdown> {
        return StatType.entries.associateWith { statType ->
            val statTransactions = transactions.filter { it.statType == statType }
            val totalPoints = statTransactions.sumOf { it.points }
            val count = statTransactions.size

            StatBreakdown(
                totalPoints = totalPoints,
                transactionCount = count,
                averagePoints = if (count > 0) totalPoints.toFloat() / count else 0f
            )
        }
    }

    private fun calculateWeeklyData(transactions: List<Transaction>): List<DayData> {
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(6)

        return (0..6).map { dayOffset ->
            val date = sevenDaysAgo.plusDays(dayOffset.toLong())
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = startOfDay + 86400000

            val dayTransactions = transactions.filter { it.createdAt in startOfDay until endOfDay }
            val totalPoints = dayTransactions.sumOf { it.points }

            DayData(
                date = date,
                points = totalPoints,
                transactionCount = dayTransactions.size
            )
        }
    }

    private fun calculateTopSources(transactions: List<Transaction>): List<SourceData> {
        return transactions
            .groupBy { it.source }
            .map { (source, txns) ->
                SourceData(
                    source = source,
                    totalPoints = txns.sumOf { it.points },
                    count = txns.size
                )
            }
            .sortedByDescending { it.totalPoints }
            .take(5)
    }

    fun selectStat(statType: StatType?) {
        _uiState.update { it.copy(selectedStat = statType) }
    }
}

data class StatsUiState(
    val stats: PlayerStats = PlayerStats(),
    val statBreakdown: Map<StatType, StatBreakdown> = emptyMap(),
    val weeklyData: List<DayData> = emptyList(),
    val topSources: List<SourceData> = emptyList(),
    val totalTransactions: Int = 0,
    val selectedStat: StatType? = null,
    val isLoading: Boolean = true
)

data class StatBreakdown(
    val totalPoints: Int,
    val transactionCount: Int,
    val averagePoints: Float
)

data class DayData(
    val date: LocalDate,
    val points: Int,
    val transactionCount: Int
)

data class SourceData(
    val source: com.rewardpoints.app.domain.model.TransactionSource,
    val totalPoints: Int,
    val count: Int
)
