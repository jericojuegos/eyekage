package com.eyekage.ui.eyes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.eyekage.core.motion.EyeMotionState
import com.eyekage.ui.theme.DarkBackground

/**
 * Procedural Compose Canvas eye renderer.
 * Draws symmetrical, glowing robot eyes on an absolute OLED-black background (#000000)
 * with zero per-frame heap allocations.
 *
 * @param state Current motion state (gaze offsets, eyelid openness, pupil scale).
 * @param spec Color and geometry styling specification.
 * @param modifier Composable layout modifier.
 */
@Composable
fun RobotEyesCanvas(
    state: EyeMotionState,
    spec: EyeSpec = EyeSpec.Default,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // Calculate layout proportions
        val eyeWidth = (canvasWidth * 0.23f).coerceIn(80f, 320f)
        val eyeHeight = eyeWidth * 1.38f
        val eyeSpacing = (canvasWidth * 0.14f).coerceIn(40f, 200f)

        val centerX = canvasWidth * 0.5f
        val centerY = canvasHeight * 0.5f

        val leftEyeCenterX = centerX - eyeSpacing * 0.5f - eyeWidth * 0.5f
        val rightEyeCenterX = centerX + eyeSpacing * 0.5f + eyeWidth * 0.5f

        // Gaze offsets (scaled to eye proportions)
        val maxShiftX = eyeWidth * 0.22f
        val maxShiftY = eyeHeight * 0.20f
        val shiftX = state.gazeX * maxShiftX
        val shiftY = state.gazeY * maxShiftY

        val openness = state.eyelidOpenness.coerceIn(0.0f, 1.0f)

        // Draw left and right eyes symmetrically
        drawEye(
            centerX = leftEyeCenterX,
            centerY = centerY,
            eyeWidth = eyeWidth,
            eyeHeight = eyeHeight,
            shiftX = shiftX,
            shiftY = shiftY,
            openness = openness,
            pupilScale = state.pupilScale,
            spec = spec
        )

        drawEye(
            centerX = rightEyeCenterX,
            centerY = centerY,
            eyeWidth = eyeWidth,
            eyeHeight = eyeHeight,
            shiftX = shiftX,
            shiftY = shiftY,
            openness = openness,
            pupilScale = state.pupilScale,
            spec = spec
        )
    }
}

/**
 * Procedural rendering of a single robot eye.
 * Designed to avoid object allocations on the render thread.
 */
private fun DrawScope.drawEye(
    centerX: Float,
    centerY: Float,
    eyeWidth: Float,
    eyeHeight: Float,
    shiftX: Float,
    shiftY: Float,
    openness: Float,
    pupilScale: Float,
    spec: EyeSpec
) {
    if (openness < 0.04f) {
        // Closed eye / slit state during full blink closure
        val slitHeight = 4f
        val slitWidth = eyeWidth * 0.85f
        drawRoundRect(
            color = spec.primaryColor.copy(alpha = 0.45f),
            topLeft = Offset(centerX - slitWidth * 0.5f + shiftX, centerY - slitHeight * 0.5f + shiftY),
            size = Size(slitWidth, slitHeight),
            cornerRadius = CornerRadius(slitHeight * 0.5f, slitHeight * 0.5f)
        )
        return
    }

    val currentEyeHeight = eyeHeight * openness
    val eyeCornerRadius = CornerRadius(
        eyeWidth * 0.5f,
        (currentEyeHeight * 0.5f).coerceAtMost(eyeWidth * 0.5f)
    )

    // Layer 1: Outer Radial Glow
    val glowRadius = eyeWidth * spec.glowRadiusMultiplier * openness
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(spec.glowColor.copy(alpha = spec.glowColor.alpha * openness), Color.Transparent),
            center = Offset(centerX + shiftX * 0.4f, centerY + shiftY * 0.4f),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(centerX + shiftX * 0.4f, centerY + shiftY * 0.4f)
    )

    // Layer 2: Main Eye Pill (Iris gradient)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(spec.primaryColor, spec.secondaryColor),
            startY = centerY - currentEyeHeight * 0.5f + shiftY,
            endY = centerY + currentEyeHeight * 0.5f + shiftY
        ),
        topLeft = Offset(centerX - eyeWidth * 0.5f + shiftX, centerY - currentEyeHeight * 0.5f + shiftY),
        size = Size(eyeWidth, currentEyeHeight),
        cornerRadius = eyeCornerRadius
    )

    // Layer 3: Luminous Core / Pupil (Bright center)
    val coreWidth = eyeWidth * 0.52f * pupilScale
    val coreHeight = (currentEyeHeight * 0.52f * pupilScale).coerceAtLeast(2f)
    val coreCornerRadius = CornerRadius(
        coreWidth * 0.5f,
        (coreHeight * 0.5f).coerceAtMost(coreWidth * 0.5f)
    )

    drawRoundRect(
        color = spec.coreColor.copy(alpha = 0.90f * openness),
        topLeft = Offset(centerX - coreWidth * 0.5f + shiftX * 1.15f, centerY - coreHeight * 0.5f + shiftY * 1.15f),
        size = Size(coreWidth, coreHeight),
        cornerRadius = coreCornerRadius
    )

    // Layer 4: Specular Reflection Dot (Alive glance effect)
    if (openness > 0.35f) {
        val dotRadius = coreWidth * 0.12f
        drawCircle(
            color = Color.White.copy(alpha = 0.85f * openness),
            radius = dotRadius,
            center = Offset(
                centerX - coreWidth * 0.18f + shiftX * 1.15f,
                centerY - coreHeight * 0.22f + shiftY * 1.15f
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Default Open Eyes", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewOpen() {
    RobotEyesCanvas(
        state = EyeMotionState(
            gazeX = 0f,
            gazeY = 0f,
            eyelidOpenness = 1.0f
        ),
        spec = EyeSpec.Default
    )
}

@Preview(name = "Eyes Glancing Right", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewGlance() {
    RobotEyesCanvas(
        state = EyeMotionState(
            gazeX = 0.7f,
            gazeY = -0.3f,
            eyelidOpenness = 1.0f
        ),
        spec = EyeSpec.CrimsonTracker
    )
}

@Preview(name = "Blinking Eyes", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewBlink() {
    RobotEyesCanvas(
        state = EyeMotionState(
            gazeX = 0f,
            gazeY = 0f,
            eyelidOpenness = 0.0f
        ),
        spec = EyeSpec.Default
    )
}
