package dev.statup.app.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.statup.app.ui.theme.*

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) GlassTokens.PressScale else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    val accentColor = AccentPrimary
    
    val backgroundBrush = when {
        !enabled -> Brush.verticalGradient(
            colors = listOf(
                TextTertiary.copy(alpha = 0.2f),
                TextTertiary.copy(alpha = 0.15f)
            )
        )
        primary -> Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.95f),
                accentColor.copy(alpha = 0.8f),
                accentColor.copy(alpha = 0.7f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                GlassFill.copy(alpha = 0.15f),
                GlassFill.copy(alpha = 0.1f)
            )
        )
    }

    val textColor = when {
        !enabled -> TextTertiary
        primary -> TextOnAccent
        else -> TextPrimary
    }

    val borderBrush = when {
        !enabled -> Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
        primary -> Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.3f),
                accentColor.copy(alpha = 0.5f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                GlassBorder.copy(alpha = 0.3f),
                GlassBorder.copy(alpha = 0.15f)
            )
        )
    }

    val shape = RoundedCornerShape(GlassTokens.ButtonRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .height(GlassTokens.ButtonHeight)
            .clip(shape)
            .background(brush = backgroundBrush)
            .then(
                if (primary && enabled) {
                    Modifier.drawBehind {
                        // Top shine effect
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height * 0.5f)
                            )
                        )
                    }
                } else Modifier
            )
            .border(1.dp, borderBrush, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                it()
            }
        }
    }
}

@Composable
fun GlassButtonSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) GlassTokens.PressScale else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    val backgroundBrush = when {
        !enabled -> Brush.verticalGradient(
            colors = listOf(
                TextTertiary.copy(alpha = 0.2f),
                TextTertiary.copy(alpha = 0.15f)
            )
        )
        primary -> Brush.verticalGradient(
            colors = listOf(
                AccentPrimary.copy(alpha = 0.9f),
                AccentPrimary.copy(alpha = 0.75f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                GlassFill.copy(alpha = 0.15f),
                GlassFill.copy(alpha = 0.1f)
            )
        )
    }

    val textColor = when {
        !enabled -> TextTertiary
        primary -> TextOnAccent
        else -> TextPrimary
    }

    val shape = RoundedCornerShape(GlassTokens.ButtonRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .height(GlassTokens.ButtonHeightSmall)
            .clip(shape)
            .background(brush = backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Inter
        )
    }
}

@Composable
fun GlassIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    size: Dp = 48.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) GlassTokens.PressScale else 1f,
        animationSpec = tween(100),
        label = "iconButtonScale"
    )

    val shape = RoundedCornerShape(size / 2)
    val bgColors = if (selected) {
        listOf(AccentPrimary.copy(alpha = 0.3f), AccentPrimary.copy(alpha = 0.15f))
    } else {
        listOf(GlassFill.copy(alpha = 0.2f), GlassFill.copy(alpha = 0.1f))
    }
    val borderColor = if (selected) AccentPrimary.copy(alpha = 0.4f) else GlassBorder.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clip(shape)
            .background(brush = Brush.radialGradient(colors = bgColors))
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
