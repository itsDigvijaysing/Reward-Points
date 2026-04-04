package com.rewardpoints.app.ui.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rewardpoints.app.domain.model.Transaction
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.ui.components.glass.*
import com.rewardpoints.app.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button and calendar toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "History",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.toggleCalendar() }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Toggle Calendar",
                    tint = if (uiState.showCalendar) AccentPrimary else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streak Calendar Section (collapsible)
        AnimatedVisibility(
            visible = uiState.showCalendar,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                StreakCalendar(
                    calendarDays = uiState.calendarDays,
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Stats summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${uiState.totalEarned}",
                        color = AccentSuccess,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Earned",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "-${uiState.totalSpent}",
                        color = AccentError,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Spent",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryFilter.entries) { filter ->
                FilterChip(
                    filter = filter,
                    isSelected = uiState.selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction list
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else if (uiState.filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                GlassCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontFamily = Inter
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun StreakCalendar(
    calendarDays: List<DayStatus>,
    currentStreak: Int,
    longestStreak: Int
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Streak stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥 $currentStreak",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStreak > 0) AccentWarning else TextSecondary
                    )
                    Text(
                        text = "Current Streak",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = Inter
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏆 $longestStreak",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary
                    )
                    Text(
                        text = "Longest Streak",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = Inter
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day of week labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid (4 weeks = 28 days) - using Column/Row for better control
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                calendarDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        week.forEach { day ->
                            Box(modifier = Modifier.weight(1f)) {
                                CalendarDay(dayStatus = day, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        // Fill remaining slots if week is incomplete
                        repeat(7 - week.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BackgroundSurface)
                )
                Text(
                    text = " No activity",
                    color = TextTertiary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(AccentSuccess)
                )
                Text(
                    text = " Points earned",
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    dayStatus: DayStatus,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    val dayNumber = dateFormat.format(Date(dayStatus.date))

    val backgroundColor = when {
        dayStatus.hasActivity && dayStatus.pointsEarned >= 10 -> AccentSuccess
        dayStatus.hasActivity -> AccentSuccess.copy(alpha = 0.6f)
        else -> BackgroundSurface.copy(alpha = 0.5f)
    }

    val borderColor = when {
        dayStatus.isToday -> AccentPrimary
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (dayStatus.isToday) {
                    Modifier.border(2.dp, borderColor, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber,
            color = if (dayStatus.hasActivity) BackgroundBase else TextTertiary,
            fontSize = 11.sp,
            fontWeight = if (dayStatus.isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FilterChip(
    filter: HistoryFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassButtonSmall(
        text = filter.label,
        onClick = onClick,
        primary = isSelected
    )
}

@Composable
private fun TransactionCard(transaction: Transaction) {
    val isEarn = transaction.type == TransactionType.EARN
    val pointsColor = if (isEarn) AccentSuccess else AccentError
    val pointsPrefix = if (isEarn) "+" else "-"

    val emoji = when (transaction.source) {
        TransactionSource.TODOIST -> "✅"
        TransactionSource.MANUAL -> "✏️"
        TransactionSource.MISSION -> "🎯"
        TransactionSource.MOOD -> "😊"
        TransactionSource.REWARD -> "🎁"
    }

    val statColor = transaction.statType?.let { stat ->
        when (stat) {
            StatType.STR -> StatStrength
            StatType.INT -> StatIntelligence
            StatType.WIS -> StatWisdom
            StatType.DEX -> StatDexterity
            StatType.CHA -> StatCharisma
            StatType.VIT -> StatVitality
        }
    }

    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.createdAt))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source emoji
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (statColor ?: AccentPrimary).copy(alpha = 0.2f),
                                (statColor ?: AccentPrimary).copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: transaction.source.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Inter,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontFamily = Inter
                    )
                    transaction.statType?.let { stat ->
                        Text(
                            text = " • ${stat.name}",
                            color = statColor ?: TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Points
            Text(
                text = "$pointsPrefix${transaction.points}",
                color = pointsColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
            )
        }
    }
}
