package com.statup.app.ui.components.rpg

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.statup.app.domain.model.Rank
import com.statup.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RankUpAnimation(
    newRank: Rank,
    onDismiss: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(true) }

    // Auto dismiss after 3 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        showAnimation = false
        onDismiss()
    }

    if (showAnimation) {
        Dialog(
            onDismissRequest = {
                showAnimation = false
                onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                // Particle effects
                ParticleEffect(rankColor = newRank.color)

                // Main content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // "RANK UP!" text with glow
                    RankUpTitle()

                    Spacer(modifier = Modifier.height(32.dp))

                    // New rank badge with animation
                    AnimatedRankBadge(rank = newRank)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rank title
                    Text(
                        text = newRank.title,
                        color = newRank.color,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Keep up the great work!",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                }
            }
        }
    }
}

@Composable
private fun RankUpTitle() {
    val infiniteTransition = rememberInfiniteTransition(label = "title")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleGlow"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        Text(
            text = "⭐ RANK UP! ⭐",
            color = PointsGold.copy(alpha = glowAlpha * 0.5f),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Inter,
            modifier = Modifier
                .blur(16.dp)
                .scale(scale * 1.2f)
        )

        // Main text
        Text(
            text = "⭐ RANK UP! ⭐",
            color = PointsGold,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Inter,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun AnimatedRankBadge(rank: Rank) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeRotation"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badgeScale"
    )

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .clip(shape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        rank.color.copy(alpha = 0.4f),
                        rank.color.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            rank.color.copy(alpha = 0.3f),
                            rank.color.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.name,
                color = rank.color,
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Inter,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ParticleEffect(rankColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particlePulse"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = minOf(size.width, size.height) * 0.3f

        // Draw spinning stars
        for (i in 0 until 8) {
            val angle = (rotation + i * 45) * Math.PI / 180
            val radius = baseRadius * pulseScale
            val x = centerX + (radius * cos(angle)).toFloat()
            val y = centerY + (radius * sin(angle)).toFloat()

            // Draw star
            drawStar(
                center = Offset(x, y),
                outerRadius = 12f,
                innerRadius = 6f,
                color = rankColor.copy(alpha = 0.6f)
            )
        }

        // Inner ring of particles
        for (i in 0 until 12) {
            val angle = (-rotation * 0.5f + i * 30) * Math.PI / 180
            val radius = baseRadius * 0.5f * pulseScale
            val x = centerX + (radius * cos(angle)).toFloat()
            val y = centerY + (radius * sin(angle)).toFloat()

            drawCircle(
                color = PointsGold.copy(alpha = 0.4f),
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color,
    points: Int = 5
) {
    val path = Path()
    val angleStep = Math.PI / points

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(path, color)
}
