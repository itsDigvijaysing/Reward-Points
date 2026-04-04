package com.rewardpoints.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.rewardpoints.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    val offset3 by infiniteTransition.animateFloat(
        initialValue = 90f,
        targetValue = 450f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background base
        drawRect(color = BackgroundBase)

        // Orb 1 - Violet (top-left region)
        val orb1X = width * 0.2f + cos(Math.toRadians(offset1.toDouble())).toFloat() * width * 0.1f
        val orb1Y = height * 0.25f + sin(Math.toRadians(offset1.toDouble())).toFloat() * height * 0.05f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrbViolet.copy(alpha = 0.3f),
                    OrbViolet.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(orb1X, orb1Y),
                radius = width * 0.5f
            ),
            radius = width * 0.5f,
            center = Offset(orb1X, orb1Y)
        )

        // Orb 2 - Blue (center-right)
        val orb2X = width * 0.8f + cos(Math.toRadians(offset2.toDouble())).toFloat() * width * 0.08f
        val orb2Y = height * 0.4f + sin(Math.toRadians(offset2.toDouble())).toFloat() * height * 0.06f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrbBlue.copy(alpha = 0.25f),
                    OrbBlue.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(orb2X, orb2Y),
                radius = width * 0.45f
            ),
            radius = width * 0.45f,
            center = Offset(orb2X, orb2Y)
        )

        // Orb 3 - Pink (bottom-left)
        val orb3X = width * 0.3f + cos(Math.toRadians(offset3.toDouble())).toFloat() * width * 0.12f
        val orb3Y = height * 0.75f + sin(Math.toRadians(offset3.toDouble())).toFloat() * height * 0.04f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrbPink.copy(alpha = 0.2f),
                    OrbPink.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(orb3X, orb3Y),
                radius = width * 0.4f
            ),
            radius = width * 0.4f,
            center = Offset(orb3X, orb3Y)
        )

        // Orb 4 - Teal (bottom-right, subtle)
        val orb4X = width * 0.75f
        val orb4Y = height * 0.85f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrbTeal.copy(alpha = 0.15f),
                    OrbTeal.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                center = Offset(orb4X, orb4Y),
                radius = width * 0.35f
            ),
            radius = width * 0.35f,
            center = Offset(orb4X, orb4Y)
        )
    }
}
