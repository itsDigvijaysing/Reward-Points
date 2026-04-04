package com.rewardpoints.app.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.Transaction
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayStatus(
    val date: Long,
    val hasActivity: Boolean,
    val pointsEarned: Int,
    val isToday: Boolean = false
)

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = true,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val calendarDays: List<DayStatus> = emptyList(),
    val showCalendar: Boolean = false
)

enum class HistoryFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    WEEK("Week"),
    EARNED("Earned"),
    SPENT("Spent")
}

class HistoryViewModel(
    private val pointsRepository: PointsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun toggleCalendar() {
        _uiState.value = _uiState.value.copy(showCalendar = !_uiState.value.showCalendar)
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            pointsRepository.transactions.collect { transactions ->
                val sorted = transactions.sortedByDescending { it.createdAt }
                val earned = transactions
                    .filter { it.type == TransactionType.EARN }
                    .sumOf { it.points }
                val spent = transactions
                    .filter { it.type == TransactionType.REDEEM }
                    .sumOf { it.points }

                // Calculate streak data
                val (currentStreak, longestStreak) = calculateStreaks(transactions)
                val calendarDays = generateCalendarDays(transactions)

                _uiState.value = _uiState.value.copy(
                    transactions = sorted,
                    filteredTransactions = applyFilter(sorted, _uiState.value.selectedFilter),
                    isLoading = false,
                    totalEarned = earned,
                    totalSpent = spent,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    calendarDays = calendarDays
                )
            }
        }
    }

    private fun calculateStreaks(transactions: List<Transaction>): Pair<Int, Int> {
        if (transactions.isEmpty()) return Pair(0, 0)

        // Get unique activity days (days with earnings)
        val activityDays = transactions
            .filter { it.type == TransactionType.EARN }
            .map { getDayStart(it.createdAt) }
            .distinct()
            .sortedDescending()

        if (activityDays.isEmpty()) return Pair(0, 0)

        val today = getDayStart(System.currentTimeMillis())
        val yesterday = today - (24 * 60 * 60 * 1000L)

        // Calculate current streak
        var currentStreak = 0
        var checkDate = if (activityDays.contains(today)) today else yesterday

        for (day in activityDays) {
            if (day == checkDate) {
                currentStreak++
                checkDate -= (24 * 60 * 60 * 1000L)
            } else if (day < checkDate) {
                break
            }
        }

        // If current streak didn't start today or yesterday, it's broken
        if (!activityDays.contains(today) && !activityDays.contains(yesterday)) {
            currentStreak = 0
        }

        // Calculate longest streak
        var longestStreak = 0
        var streak = 0
        var prevDay: Long? = null

        for (day in activityDays.sortedDescending()) {
            if (prevDay == null || prevDay - day == 24 * 60 * 60 * 1000L) {
                streak++
            } else {
                longestStreak = maxOf(longestStreak, streak)
                streak = 1
            }
            prevDay = day
        }
        longestStreak = maxOf(longestStreak, streak)

        return Pair(currentStreak, longestStreak)
    }

    private fun generateCalendarDays(transactions: List<Transaction>): List<DayStatus> {
        val today = getDayStart(System.currentTimeMillis())
        val days = mutableListOf<DayStatus>()

        // Group transactions by day
        val pointsByDay = transactions
            .filter { it.type == TransactionType.EARN }
            .groupBy { getDayStart(it.createdAt) }
            .mapValues { (_, txns) -> txns.sumOf { it.points } }

        // Generate last 28 days (4 weeks)
        for (i in 27 downTo 0) {
            val dayStart = today - (i * 24 * 60 * 60 * 1000L)
            val pointsEarned = pointsByDay[dayStart] ?: 0
            days.add(
                DayStatus(
                    date = dayStart,
                    hasActivity = pointsEarned > 0,
                    pointsEarned = pointsEarned,
                    isToday = dayStart == today
                )
            )
        }

        return days
    }

    private fun getDayStart(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredTransactions = applyFilter(_uiState.value.transactions, filter)
        )
    }

    private fun applyFilter(transactions: List<Transaction>, filter: HistoryFilter): List<Transaction> {
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekStart = todayStart - (7 * 24 * 60 * 60 * 1000L)

        return when (filter) {
            HistoryFilter.ALL -> transactions
            HistoryFilter.TODAY -> transactions.filter { it.createdAt >= todayStart }
            HistoryFilter.WEEK -> transactions.filter { it.createdAt >= weekStart }
            HistoryFilter.EARNED -> transactions.filter { it.type == TransactionType.EARN }
            HistoryFilter.SPENT -> transactions.filter { it.type == TransactionType.REDEEM }
        }
    }
}
