package com.rewardpoints.app.ui.components.rpg

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.ui.components.glass.GlassButton
import com.rewardpoints.app.ui.components.glass.GlassCard
import com.rewardpoints.app.ui.theme.*

@Composable
fun StatusWindow(
    playerName: String,
    stats: PlayerStats,
    availablePoints: Int = 0,
    hexagonStyle: HexagonStyle = HexagonStyle.SIMPLE,
    modifier: Modifier = Modifier,
    equippedTitle: String? = null,
    onTitleClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(24.dp)
    var showStatBars by remember { mutableStateOf(false) }
    var showRankInfo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassHighlight,
                        GlassFill,
                        GlassFill
                    )
                )
            )
            .border(GlassTokens.BorderWidth, GlassBorder, shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        StatusHeader()

        Spacer(modifier = Modifier.height(20.dp))

        // Rank Badge — clickable for rank info
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showRankInfo = true }
        ) {
            RankBadge(rank = stats.rank, showTitle = false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player Name
        Text(
            text = playerName,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter
        )

        // Equipped achievement title — tap to change. Shows a subtle hint when none is
        // equipped so the feature is discoverable without cluttering the sheet.
        Text(
            text = equippedTitle?.let { "« $it »" } ?: "+ set title",
            color = if (equippedTitle != null) PointsGold else TextTertiary,
            fontSize = if (equippedTitle != null) 13.sp else 11.sp,
            fontWeight = if (equippedTitle != null) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = Inter,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTitleClick
                )
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "✨ ${stats.totalPointsEarned} pts",
            color = PointsGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hexagon Chart - clickable to toggle stat bars
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showStatBars = !showStatBars }
        ) {
            HexagonRadarChart(
                stats = stats,
                size = 160.dp,
                showLabels = true,
                style = hexagonStyle
            )
        }

        // Collapsible Stat Bars
        AnimatedVisibility(
            visible = showStatBars,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                StatBarsColumn(stats = stats, showFullNames = true)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer — compact row with streak, points, rank progress
        StatusFooter(
            streak = stats.streak,
            starLines = stats.rankUpStreakCounter,
            availablePoints = availablePoints,
            onHistoryClick = onHistoryClick
        )
    }

    // Rank Info Dialog
    if (showRankInfo) {
        RankInfoDialog(
            currentRank = stats.rank,
            starLines = stats.rankUpStreakCounter,
            onDismiss = { showRankInfo = false }
        )
    }
}

@Composable
private fun StatusHeader() {
    val headerShape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .clip(headerShape)
            .background(AccentPrimary.copy(alpha = 0.15f))
            .border(1.dp, AccentPrimary.copy(alpha = 0.3f), headerShape)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "S T A T U S",
            color = AccentPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun StatusFooter(
    streak: Int,
    starLines: Int,
    availablePoints: Int,
    onHistoryClick: () -> Unit = {}
) {
    // Star lines: compact inline display
    val filledLines = starLines.coerceIn(0, 5)
    val starDisplay = "★".repeat(filledLines) + "☆".repeat(5 - filledLines)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        FooterItem(
            emoji = "🔥",
            label = "Streak",
            value = "$streak days"
        )
        FooterItem(
            emoji = "✨",
            label = "Available",
            value = "$availablePoints pts",
            highlight = true,
            onClick = onHistoryClick
        )
        FooterItem(
            emoji = "⭐",
            label = "Rank Up",
            value = "$filledLines / 5"
        )
    }
}

@Composable
private fun FooterItem(
    emoji: String,
    label: String,
    value: String,
    highlight: Boolean = false,
    isStarLine: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        } else Modifier
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 10.sp,
            fontFamily = Inter
        )
        Text(
            text = value,
            color = if (highlight) PointsGold else if (isStarLine) PointsGold else TextPrimary,
            fontSize = if (isStarLine) 10.sp else 12.sp,
            fontWeight = if (highlight || isStarLine) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = Inter,
            letterSpacing = if (isStarLine) 1.sp else 0.sp
        )
    }
}

@Composable
private fun RankInfoDialog(
    currentRank: Rank,
    starLines: Int,
    onDismiss: () -> Unit
) {
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
                        text = "Rank System",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your progress is tracked by Star Lines (★). " +
                                "Each active day adds a line, each idle day removes one.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "★★★★★ = Rank Up!\n" +
                                "Lines below 0 = Rank Down",
                        color = PointsGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rank list
                    Rank.entries.reversed().forEach { rank ->
                        val isCurrent = rank == currentRank
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCurrent) "▸" else "  ",
                                color = rank.color,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rank ${rank.name}",
                                color = if (isCurrent) rank.color else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = Inter,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = rank.title,
                                color = if (isCurrent) TextPrimary else TextTertiary,
                                fontSize = 14.sp,
                                fontFamily = Inter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val linesDisplay = starLines.coerceIn(0, 5)
                    Text(
                        text = "Current: ${"★".repeat(linesDisplay)}${"☆".repeat(5 - linesDisplay)} (${starLines}/5)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassButton(
                        text = "Got it",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
