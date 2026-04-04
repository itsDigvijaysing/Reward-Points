package com.rewardpoints.app.ui.components.rpg

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.ui.theme.*

@Composable
fun StatusWindow(
    playerName: String,
    stats: PlayerStats,
    availablePoints: Int = 0,
    hexagonStyle: HexagonStyle = HexagonStyle.SIMPLE,
    modifier: Modifier = Modifier,
    onHistoryClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(24.dp)
    var showStatBars by remember { mutableStateOf(false) }

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

        // Rank Badge
        RankBadge(rank = stats.rank)

        Spacer(modifier = Modifier.height(16.dp))

        // Player Info
        Text(
            text = "Player: $playerName",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Inter
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Total Points: ${stats.totalPointsEarned}",
            color = PointsGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hexagon Chart - clickable to toggle stat bars
        Box(
            modifier = Modifier.clickable { showStatBars = !showStatBars }
        ) {
            HexagonRadarChart(
                stats = stats,
                size = 160.dp,
                showLabels = true,
                style = hexagonStyle
            )
        }

        // Hint text - removed per user feedback
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

        // Footer Info - shows star lines progress
        StatusFooter(
            streak = stats.streak,
            starLines = stats.rankUpStreakCounter,
            availablePoints = availablePoints,
            onHistoryClick = onHistoryClick
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
    // Star lines visualization: ☆ for empty, ★ for filled
    val starProgress = buildString {
        repeat(starLines.coerceIn(0, 5)) { append("★") }
        repeat((5 - starLines).coerceAtLeast(0)) { append("☆") }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⭐",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rank Up",
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = Inter
            )
            Text(
                text = starProgress,
                color = PointsGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun FooterItem(
    emoji: String,
    label: String,
    value: String,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier.clickable { onClick() }
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
            color = if (highlight) PointsGold else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = Inter
        )
    }
}
