package dev.statup.app.ui.components.rpg

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.statup.app.domain.model.PlayerStats
import dev.statup.app.domain.model.StatType
import dev.statup.app.ui.theme.*

@Composable
fun StatBarsColumn(
    stats: PlayerStats,
    modifier: Modifier = Modifier,
    showFullNames: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBar(StatType.STR, stats.strStat, showFullNames)
        StatBar(StatType.INT, stats.intStat, showFullNames)
        StatBar(StatType.WIS, stats.wisStat, showFullNames)
        StatBar(StatType.DEX, stats.dexStat, showFullNames)
        StatBar(StatType.CHA, stats.chaStat, showFullNames)
        StatBar(StatType.VIT, stats.vitStat, showFullNames)
    }
}

@Composable
private fun StatBar(
    statType: StatType,
    value: Int,
    showFullName: Boolean
) {
    val progress by animateFloatAsState(
        targetValue = value.toFloat() / PlayerStats.MAX_STAT,
        animationSpec = tween(500),
        label = "statProgress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stat name - uses weight for responsive sizing
        Text(
            text = if (showFullName) statType.displayName else statType.name,
            color = statType.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.25f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Progress bar - flexible width
        Box(
            modifier = Modifier
                .weight(0.6f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                statType.color.copy(alpha = 0.6f),
                                statType.color
                            )
                        )
                    )
            )
        }

        // Value display
        Text(
            text = "$value",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter,
            modifier = Modifier.weight(0.15f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
