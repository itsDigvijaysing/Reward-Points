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
    val dayOfMonth: Int,
    val hasActivity: Boolean,
    val pointsEarned: Int,
    val isToday: Boolean = false,
    val isFuture: Boolean = false,
    val isCurrentMonth: Boolean = true
)

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val visibleTransactions: List<Transaction> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = true,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val calendarDays: List<DayStatus?> = emptyList(),
    val calendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val calendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val showCalendar: Boolean = false,
    val pageSize: Int = 50,
    val currentPage: Int = 1,
    val hasMore: Boolean = false
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

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    fun toggleCalendar() {
        _uiState.value = _uiState.value.copy(showCalendar = !_uiState.value.showCalendar)
    }

    fun previousMonth() {
        val state = _uiState.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.calendarYear)
            set(Calendar.MONTH, state.calendarMonth)
            add(Calendar.MONTH, -1)
        }
        val newMonth = cal.get(Calendar.MONTH)
        val newYear = cal.get(Calendar.YEAR)
        _uiState.value = state.copy(
            calendarMonth = newMonth,
            calendarYear = newYear,
            calendarDays = generateCalendarDays(allTransactions, newMonth, newYear)
        )
    }

    fun nextMonth() {
        val state = _uiState.value
        val now = Calendar.getInstance()
        // Don't go beyond current month
        if (state.calendarYear == now.get(Calendar.YEAR) && state.calendarMonth == now.get(Calendar.MONTH)) return

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.calendarYear)
            set(Calendar.MONTH, state.calendarMonth)
            add(Calendar.MONTH, 1)
        }
        val newMonth = cal.get(Calendar.MONTH)
        val newYear = cal.get(Calendar.YEAR)
        _uiState.value = state.copy(
            calendarMonth = newMonth,
            calendarYear = newYear,
            calendarDays = generateCalendarDays(allTransactions, newMonth, newYear)
        )
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            pointsRepository.transactions.collect { transactions ->
                val sorted = transactions.sortedByDescending { it.createdAt }
                allTransactions = sorted
                val earned = transactions
                    .filter { it.type == TransactionType.EARN }
                    .sumOf { it.points }
                val spent = transactions
                    .filter { it.type == TransactionType.REDEEM }
                    .sumOf { it.points }

                val (currentStreak, longestStreak) = calculateStreaks(transactions)
                val state = _uiState.value
                val calendarDays = generateCalendarDays(transactions, state.calendarMonth, state.calendarYear)

                val filtered = applyFilter(sorted, state.selectedFilter)
                val pageSize = state.pageSize
                _uiState.value = state.copy(
                    transactions = sorted,
                    filteredTransactions = filtered,
                    visibleTransactions = filtered.take(pageSize),
                    currentPage = 1,
                    hasMore = filtered.size > pageSize,
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

    /**
     * Generates a proper month calendar grid.
     * Returns a list of 42 slots (6 weeks x 7 days).
     * null = empty slot (padding before first day / after last day).
     */
    private fun generateCalendarDays(
        transactions: List<Transaction>,
        month: Int,
        year: Int
    ): List<DayStatus?> {
        val today = getDayStart(System.currentTimeMillis())

        // Group earn transactions by day
        val pointsByDay = transactions
            .filter { it.type == TransactionType.EARN }
            .groupBy { getDayStart(it.createdAt) }
            .mapValues { (_, txns) -> txns.sumOf { it.points } }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 6=Sat
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val slots = mutableListOf<DayStatus?>()

        // Leading empty slots
        repeat(firstDayOfWeek) { slots.add(null) }

        // Actual days
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = cal.timeInMillis
            val pointsEarned = pointsByDay[dayStart] ?: 0

            slots.add(
                DayStatus(
                    date = dayStart,
                    dayOfMonth = day,
                    hasActivity = pointsEarned > 0,
                    pointsEarned = pointsEarned,
                    isToday = dayStart == today,
                    isFuture = dayStart > today,
                    isCurrentMonth = true
                )
            )
        }

        // Trailing empty slots to complete the grid (up to 42 = 6 weeks)
        while (slots.size % 7 != 0) { slots.add(null) }

        return slots
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
        val filtered = applyFilter(_uiState.value.transactions, filter)
        val pageSize = _uiState.value.pageSize
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredTransactions = filtered,
            visibleTransactions = filtered.take(pageSize),
            currentPage = 1,
            hasMore = filtered.size > pageSize
        )
    }

    fun loadMore() {
        val state = _uiState.value
        val nextPage = state.currentPage + 1
        val visible = state.filteredTransactions.take(nextPage * state.pageSize)
        _uiState.value = state.copy(
            visibleTransactions = visible,
            currentPage = nextPage,
            hasMore = visible.size < state.filteredTransactions.size
        )
    }

    private fun applyFilter(transactions: List<Transaction>, filter: HistoryFilter): List<Transaction> {
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
