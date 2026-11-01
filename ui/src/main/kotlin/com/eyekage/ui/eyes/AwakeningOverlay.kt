package com.eyekage.ui.eyes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.eyekage.ui.theme.ReactionAwakeningPrimary
import com.eyekage.ui.theme.ReactionAwakeningSecondary
import kotlin.math.PI
import kotlin.math.sin

private const val MIN_ALPHA_THRESHOLD = 0.001f
private const val INNER_ALPHA_FACTOR = 0.85f
private const val OUTER_ALPHA_FACTOR = 0.50f
private const val AMBIENT_ALPHA_FACTOR = 0.20f
private const val REDUCED_STROKE_WIDTH = 6f
private const val REDUCED_ALPHA_FACTOR = 0.40f

private const val EYE_WIDTH_RATIO = 0.23f
private const val MIN_EYE_WIDTH = 80f
private const val MAX_EYE_WIDTH = 320f
private const val EYE_SPACING_RATIO = 0.14f
private const val MIN_EYE_SPACING = 40f
private const val MAX_EYE_SPACING = 200f
private const val HALF = 0.5f

/**
 * Procedural luminous amber energy burst overlay for the Awakening reaction (`reaction.awakening`).
 */
@Composable
fun AwakeningOverlay(
    progress: Float,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (progress <= 0.0f || progress >= 1.0f) return

    val peakAlpha = remember(progress) {
        sin(PI.toFloat() * progress.coerceIn(0.0f, 1.0f))
    }
    if (peakAlpha <= MIN_ALPHA_THRESHOLD) return

    val innerColor = remember(peakAlpha) {
        ReactionAwakeningSecondary.copy(alpha = INNER_ALPHA_FACTOR * peakAlpha)
    }
    val outerColor = remember(peakAlpha) {
        ReactionAwakeningPrimary.copy(alpha = OUTER_ALPHA_FACTOR * peakAlpha)
    }
    val gradientColors = remember(innerColor, outerColor) {
        listOf(innerColor, outerColor, Color.Transparent)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        if (reducedMotion) {
            drawReducedMotionBorder(peakAlpha)
        } else {
            drawExpandingBurst(progress, peakAlpha, gradientColors)
        }
    }
}

private fun DrawScope.drawReducedMotionBorder(peakAlpha: Float) {
    drawRect(
        color = ReactionAwakeningPrimary.copy(alpha = REDUCED_ALPHA_FACTOR * peakAlpha),
        style = Stroke(width = REDUCED_STROKE_WIDTH)
    )
}

private fun DrawScope.drawExpandingBurst(
    progress: Float,
    peakAlpha: Float,
    gradientColors: List<Color>
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val centerX = canvasWidth * HALF
    val centerY = canvasHeight * HALF

    val eyeWidth = (canvasWidth * EYE_WIDTH_RATIO).coerceIn(MIN_EYE_WIDTH, MAX_EYE_WIDTH)
    val eyeSpacing = (canvasWidth * EYE_SPACING_RATIO).coerceIn(MIN_EYE_SPACING, MAX_EYE_SPACING)

    val leftEyeCenterX = centerX - eyeSpacing * HALF - eyeWidth * HALF
    val rightEyeCenterX = centerX + eyeSpacing * HALF + eyeWidth * HALF
    val baseRadius = eyeWidth * (1.2f + 1.6f * progress)

    // Left eye bloom
    drawCircle(
        brush = Brush.radialGradient(
            colors = gradientColors,
            center = Offset(leftEyeCenterX, centerY),
            radius = baseRadius
        ),
        radius = baseRadius,
        center = Offset(leftEyeCenterX, centerY)
    )

    // Right eye bloom
    drawCircle(
        brush = Brush.radialGradient(
            colors = gradientColors,
            center = Offset(rightEyeCenterX, centerY),
            radius = baseRadius
        ),
        radius = baseRadius,
        center = Offset(rightEyeCenterX, centerY)
    )

    // Ambient center bloom
    val ambientRadius = (canvasWidth * 0.45f) * (0.8f + 0.4f * progress)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                ReactionAwakeningPrimary.copy(alpha = AMBIENT_ALPHA_FACTOR * peakAlpha),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = ambientRadius
        ),
        radius = ambientRadius,
        center = Offset(centerX, centerY)
    )
}
