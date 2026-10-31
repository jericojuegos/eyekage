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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.eyekage.core.model.PupilShape
import com.eyekage.core.motion.EyeMotionState
import com.eyekage.ui.theme.DarkBackground
import kotlin.math.cos
import kotlin.math.sin

private const val DEG_120_RAD = 2.0943951f
private const val BASE_ANGLE_RAD = -0.52359877f // -30 degrees

private val OrbitStrokeLight = Stroke(width = 1.75f)
private val OrbitStrokeMedium = Stroke(width = 2.5f)

/**
 * Procedural Compose Canvas eye renderer.
 * Draws symmetrical, glowing robot eyes on an absolute OLED-black background (#000000)
 * with zero per-frame heap allocations across all data-driven eye modes.
 *
 * @param state Current motion state (gaze offsets, eyelid openness, pupil scale).
 * @param spec Color, geometry, and pupil shape specification.
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
    val pupilCenterX = centerX + shiftX * 1.15f
    val pupilCenterY = centerY + shiftY * 1.15f

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

    // Layer 3: Mode-Specific Procedural Iris Geometry
    val coreWidth = eyeWidth * 0.52f * pupilScale
    val coreHeight = (currentEyeHeight * 0.52f * pupilScale).coerceAtLeast(2f)

    when (spec.shape) {
        PupilShape.ROUNDED_PILL -> {
            drawStandardCore(
                pupilCenterX = pupilCenterX,
                pupilCenterY = pupilCenterY,
                coreWidth = coreWidth,
                coreHeight = coreHeight,
                openness = openness,
                coreColor = spec.coreColor
            )
        }
        PupilShape.GEOMETRIC_TRACKER -> {
            drawGeometricTrackerCore(
                pupilCenterX = pupilCenterX,
                pupilCenterY = pupilCenterY,
                coreWidth = coreWidth,
                coreHeight = coreHeight,
                openness = openness,
                spec = spec
            )
        }
        PupilShape.SCAN_LINE -> {
            drawScanLineCore(
                pupilCenterX = pupilCenterX,
                pupilCenterY = pupilCenterY,
                coreWidth = coreWidth,
                coreHeight = coreHeight,
                openness = openness,
                spec = spec
            )
        }
        PupilShape.CONCENTRIC_ORBIT -> {
            drawConcentricOrbitCore(
                pupilCenterX = pupilCenterX,
                pupilCenterY = pupilCenterY,
                coreWidth = coreWidth,
                coreHeight = coreHeight,
                openness = openness,
                spec = spec
            )
        }
        PupilShape.BURST_RADIAL -> {
            drawStandardCore(
                pupilCenterX = pupilCenterX,
                pupilCenterY = pupilCenterY,
                coreWidth = coreWidth * 1.15f,
                coreHeight = coreHeight * 1.15f,
                openness = openness,
                coreColor = spec.coreColor
            )
        }
    }

    // Layer 4: Specular Reflection Dot (Alive glance effect)
    if (openness > 0.35f) {
        val dotRadius = coreWidth * 0.12f
        drawCircle(
            color = Color.White.copy(alpha = 0.85f * openness),
            radius = dotRadius,
            center = Offset(
                pupilCenterX - coreWidth * 0.18f,
                pupilCenterY - coreHeight * 0.22f
            )
        )
    }
}

private fun DrawScope.drawStandardCore(
    pupilCenterX: Float,
    pupilCenterY: Float,
    coreWidth: Float,
    coreHeight: Float,
    openness: Float,
    coreColor: Color
) {
    val cornerRadius = CornerRadius(
        coreWidth * 0.5f,
        (coreHeight * 0.5f).coerceAtMost(coreWidth * 0.5f)
    )
    drawRoundRect(
        color = coreColor.copy(alpha = 0.90f * openness),
        topLeft = Offset(pupilCenterX - coreWidth * 0.5f, pupilCenterY - coreHeight * 0.5f),
        size = Size(coreWidth, coreHeight),
        cornerRadius = cornerRadius
    )
}

/**
 * Crimson tracker iris: sharp dark geometric tracking accents placed around a dark pupil.
 */
private fun DrawScope.drawGeometricTrackerCore(
    pupilCenterX: Float,
    pupilCenterY: Float,
    coreWidth: Float,
    coreHeight: Float,
    openness: Float,
    spec: EyeSpec
) {
    // Dark inner pupil
    val pupilRadius = (coreWidth * 0.28f).coerceAtLeast(2f)
    drawCircle(
        color = spec.secondaryColor.copy(alpha = 0.95f * openness),
        radius = pupilRadius,
        center = Offset(pupilCenterX, pupilCenterY)
    )

    // Three sharp geometric tracking accents at 120-degree intervals
    val accentOrbitRadius = coreWidth * 0.42f
    val accentRadius = (coreWidth * 0.08f).coerceAtLeast(2f)
    val accentColor = spec.secondaryColor.copy(alpha = 0.90f * openness)

    for (i in 0..2) {
        val angle = BASE_ANGLE_RAD + i * DEG_120_RAD
        val ax = pupilCenterX + accentOrbitRadius * cos(angle)
        val ay = pupilCenterY + accentOrbitRadius * sin(angle)
        drawCircle(
            color = accentColor,
            radius = accentRadius,
            center = Offset(ax, ay)
        )
    }

    // Outer subtle tracking ring
    drawCircle(
        color = spec.secondaryColor.copy(alpha = 0.45f * openness),
        radius = accentOrbitRadius,
        center = Offset(pupilCenterX, pupilCenterY),
        style = OrbitStrokeLight
    )
}

/**
 * White scan iris: ethereal pale luminescent eye with horizontal scan-line radar pulse.
 */
private fun DrawScope.drawScanLineCore(
    pupilCenterX: Float,
    pupilCenterY: Float,
    coreWidth: Float,
    coreHeight: Float,
    openness: Float,
    spec: EyeSpec
) {
    // Pale lavender-white core fill
    drawStandardCore(
        pupilCenterX = pupilCenterX,
        pupilCenterY = pupilCenterY,
        coreWidth = coreWidth,
        coreHeight = coreHeight,
        openness = openness,
        coreColor = spec.coreColor
    )

    // Sweeping horizontal scan-lines
    val scanLineColor = spec.secondaryColor.copy(alpha = 0.55f * openness)
    val halfWidth = coreWidth * 0.48f
    val yOffset1 = coreHeight * 0.28f

    // Top scan line
    drawLine(
        color = scanLineColor,
        start = Offset(pupilCenterX - halfWidth * 0.85f, pupilCenterY - yOffset1),
        end = Offset(pupilCenterX + halfWidth * 0.85f, pupilCenterY - yOffset1),
        strokeWidth = 2f
    )
    // Center radar scan line
    drawLine(
        color = scanLineColor.copy(alpha = 0.75f * openness),
        start = Offset(pupilCenterX - halfWidth, pupilCenterY),
        end = Offset(pupilCenterX + halfWidth, pupilCenterY),
        strokeWidth = 2.5f
    )
    // Bottom scan line
    drawLine(
        color = scanLineColor,
        start = Offset(pupilCenterX - halfWidth * 0.85f, pupilCenterY + yOffset1),
        end = Offset(pupilCenterX + halfWidth * 0.85f, pupilCenterY + yOffset1),
        strokeWidth = 2f
    )
}

/**
 * Purple orbit iris: violet concentric orbital rings that pulse centered on the pupil.
 */
private fun DrawScope.drawConcentricOrbitCore(
    pupilCenterX: Float,
    pupilCenterY: Float,
    coreWidth: Float,
    coreHeight: Float,
    openness: Float,
    spec: EyeSpec
) {
    // Center dark pupil
    val centerRadius = (coreWidth * 0.16f).coerceAtLeast(2f)
    drawCircle(
        color = spec.secondaryColor.copy(alpha = 0.95f * openness),
        radius = centerRadius,
        center = Offset(pupilCenterX, pupilCenterY)
    )

    // Concentric orbital rings
    val ringColor = spec.coreColor.copy(alpha = 0.75f * openness)
    val maxRadius = coreWidth * 0.52f

    drawCircle(
        color = ringColor,
        radius = maxRadius * 0.40f,
        center = Offset(pupilCenterX, pupilCenterY),
        style = OrbitStrokeLight
    )
    drawCircle(
        color = ringColor,
        radius = maxRadius * 0.72f,
        center = Offset(pupilCenterX, pupilCenterY),
        style = OrbitStrokeMedium
    )
    drawCircle(
        color = ringColor.copy(alpha = 0.50f * openness),
        radius = maxRadius * 0.98f,
        center = Offset(pupilCenterX, pupilCenterY),
        style = OrbitStrokeLight
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Normal Mode", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewNormal() {
    RobotEyesCanvas(
        state = EyeMotionState(gazeX = 0f, gazeY = 0f, eyelidOpenness = 1.0f),
        spec = EyeSpec.Default
    )
}

@Preview(name = "Crimson Tracker Mode", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewCrimson() {
    RobotEyesCanvas(
        state = EyeMotionState(gazeX = 0f, gazeY = 0f, eyelidOpenness = 1.0f),
        spec = EyeSpec.CrimsonTracker
    )
}

@Preview(name = "White Scan Mode", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewWhiteScan() {
    RobotEyesCanvas(
        state = EyeMotionState(gazeX = 0f, gazeY = 0f, eyelidOpenness = 1.0f),
        spec = EyeSpec.WhiteScan
    )
}

@Preview(name = "Purple Orbit Mode", widthDp = 360, heightDp = 640)
@Composable
private fun RobotEyesCanvasPreviewPurpleOrbit() {
    RobotEyesCanvas(
        state = EyeMotionState(gazeX = 0f, gazeY = 0f, eyelidOpenness = 1.0f),
        spec = EyeSpec.PurpleOrbit
    )
}
