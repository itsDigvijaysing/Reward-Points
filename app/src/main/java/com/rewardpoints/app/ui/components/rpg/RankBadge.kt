package com.rewardpoints.app.ui.components.rpg

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.ui.theme.*

@Composable
fun RankBadge(
    rank: Rank,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    val glowColor = rank.color.copy(alpha = 0.3f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            rank.color.copy(alpha = 0.2f),
                            rank.color.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            rank.color.copy(alpha = 0.6f),
                            rank.color.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "RANK: ${rank.name}",
                color = rank.color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                letterSpacing = 2.sp
            )
        }

        if (showTitle) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rank.title,
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Inter
            )
        }
    }
}

@Composable
fun RankBadgeCompact(
    rank: Rank,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(rank.color.copy(alpha = 0.15f))
            .border(1.dp, rank.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.name,
            color = rank.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Inter
        )
    }
}
