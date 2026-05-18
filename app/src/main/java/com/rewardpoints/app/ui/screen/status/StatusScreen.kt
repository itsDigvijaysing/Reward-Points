package com.rewardpoints.app.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.ui.components.glass.*
import com.rewardpoints.app.ui.components.rpg.RankUpAnimation
import com.rewardpoints.app.ui.components.rpg.StatusWindow
import com.rewardpoints.app.ui.navigation.Routes
import com.rewardpoints.app.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun StatusScreen(
    navController: NavController,
    viewModel: StatusViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMoodDialog by remember { mutableStateOf(false) }
    var showAddPointsDialog by remember { mutableStateOf(false) }
    var showRankUpAnimation by remember { mutableStateOf<Rank?>(null) }

    // Listen for rank-up events
    LaunchedEffect(Unit) {
        viewModel.rankUpEvent.collect { newRank ->
            showRankUpAnimation = newRank
        }
    }

    // Show rank-up animation
    showRankUpAnimation?.let { rank ->
        RankUpAnimation(
            newRank = rank,
            onDismiss = { showRankUpAnimation = null }
        )
    }

    val hexStyle = when (uiState.hexagonStyle) {
        "glow" -> com.rewardpoints.app.ui.components.rpg.HexagonStyle.GLOW
        else -> com.rewardpoints.app.ui.components.rpg.HexagonStyle.SIMPLE
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Window (includes points now)
        StatusWindow(
            playerName = uiState.username,
            stats = uiState.stats,
            availablePoints = uiState.currentBalance,
            hexagonStyle = hexStyle,
            onHistoryClick = { navController.navigate(Routes.HISTORY) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                emoji = if (uiState.hasCheckedInMoodToday) "✅" else "😊",
                label = "Mood",
                sublabel = if (uiState.hasCheckedInMoodToday) "Done today" else "+2 pts",
                modifier = Modifier.weight(1f),
                enabled = !uiState.hasCheckedInMoodToday,
                onClick = { if (!uiState.hasCheckedInMoodToday) showMoodDialog = true }
            )
            QuickActionCard(
                emoji = "➕",
                label = "Add",
                sublabel = "Points",
                modifier = Modifier.weight(1f),
                onClick = { showAddPointsDialog = true }
            )
            QuickActionCard(
                emoji = "📜",
                label = "History",
                sublabel = "View all",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.HISTORY) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                emoji = "📊",
                label = "Stats",
                sublabel = "Details",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.FULL_STATS) }
            )
            QuickActionCard(
                emoji = "🏆",
                label = "Achieve",
                sublabel = "Titles",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.ACHIEVEMENTS) }
            )
        }
    }

    // Mood Check-in Dialog
    if (showMoodDialog) {
        MoodCheckInDialog(
            onDismiss = { showMoodDialog = false },
            onMoodSelected = { mood ->
                viewModel.checkInMood(mood)
                showMoodDialog = false
            }
        )
    }

    // Add Points Dialog
    if (showAddPointsDialog) {
        AddPointsDialog(
            onDismiss = { showAddPointsDialog = false },
            onAdd = { points, stat, description ->
                viewModel.addManualPoints(points, stat, description)
                showAddPointsDialog = false
            }
        )
    }
}

@Composable
private fun QuickActionCard(
    emoji: String,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val cardAlpha = if (enabled) 1f else 0.55f
    GlassCard(
        modifier = modifier.alpha(cardAlpha),
        onClick = if (enabled) onClick else null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
            )
            Text(
                text = sublabel,
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = Inter
            )
        }
    }
}

@Composable
private fun MoodCheckInDialog(
    onDismiss: () -> Unit,
    onMoodSelected: (String) -> Unit
) {
    val moods = listOf(
        "😊" to "Happy",
        "😌" to "Calm",
        "😤" to "Motivated",
        "😢" to "Sad",
        "😴" to "Tired",
        "😎" to "Focused"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "How are you feeling?",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Earn +2 WIS points",
                        color = AccentPrimary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3x2 Grid for moods
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        items(moods) { (emoji, label) ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlassIconButton(
                                    icon = { Text(text = emoji, fontSize = 28.sp) },
                                    onClick = { onMoodSelected(label) },
                                    size = 56.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = Inter,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        primary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPointsDialog(
    onDismiss: () -> Unit,
    onAdd: (points: Int, stat: StatType, description: String) -> Unit
) {
    var points by remember { mutableStateOf("4") }
    var selectedStat by remember { mutableStateOf(StatType.STR) }
    var description by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Points",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )
                    Text(
                        text = "Manually add reward points",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Points selector - 4 in row
                    Text(
                        text = "Points Amount:",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1", "2", "3", "4").forEach { p ->
                            GlassButtonSmall(
                                text = "+$p",
                                onClick = { points = p },
                                primary = points == p,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stat selector - 3x2 Grid
                    Text(
                        text = "Target Stat:",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(StatType.entries.toList()) { stat ->
                            val isSelected = selectedStat == stat
                            GlassButtonSmall(
                                text = stat.name,
                                onClick = { selectedStat = stat },
                                primary = isSelected,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    GlassTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            primary = false,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Add +${points}",
                            onClick = {
                                onAdd(
                                    points.toIntOrNull() ?: 4,
                                    selectedStat,
                                    description.ifBlank { "Manual entry" }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
