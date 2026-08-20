package dev.statup.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.statup.app.data.repository.PlayerRepository
import dev.statup.app.data.repository.PointsRepository
import dev.statup.app.domain.model.PlayerStats
import dev.statup.app.domain.model.StatType
import dev.statup.app.domain.model.Transaction
import dev.statup.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

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
                // Three full-list scans + groupBy on every emission. With long history
                // this jank-frames on Main. Offload to Default.
                val computed = withContext(Dispatchers.Default) {
                    val earnTransactions = transactions.filter { it.type == TransactionType.EARN }
                    Aggregates(
                        statBreakdown = calculateStatBreakdown(earnTransactions),
                        weeklyData = calculateWeeklyData(earnTransactions),
                        topSources = calculateTopSources(earnTransactions),
                        earnCount = earnTransactions.size
                    )
                }
                _uiState.update {
                    it.copy(
                        statBreakdown = computed.statBreakdown,
                        weeklyData = computed.weeklyData,
                        topSources = computed.topSources,
                        totalTransactions = computed.earnCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    private data class Aggregates(
        val statBreakdown: Map<StatType, StatBreakdown>,
        val weeklyData: List<DayData>,
        val topSources: List<SourceData>,
        val earnCount: Int
    )

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

        val zone = ZoneId.systemDefault()
        return (0..6).map { dayOffset ->
            val date = sevenDaysAgo.plusDays(dayOffset.toLong())
            // Both bounds from LocalDate: a DST day is 23h or 25h, so start+24h would spill
            // into the next bar or clip an hour off this one.
            val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

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
    val source: dev.statup.app.domain.model.TransactionSource,
    val totalPoints: Int,
    val count: Int
)
