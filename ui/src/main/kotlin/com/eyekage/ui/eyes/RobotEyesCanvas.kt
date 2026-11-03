package com.eyekage.ui.eyes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.eyekage.core.model.MicroExpression
import com.eyekage.core.model.PupilShape
import com.eyekage.core.motion.EyeMotionState
import com.eyekage.ui.theme.DarkBackground
import kotlin.math.cos
import kotlin.math.sin

private const val DEG_120_RAD = 2.0943951f
private const val BASE_ANGLE_RAD = -0.52359877f // -30 degrees

private val OrbitStrokeLight = Stroke(width = 1.75f)
private val OrbitStrokeMedium = Stroke(width = 2.5f)

private const val RIGHT_TILT_RATIO = 0.4f
private const val HAPPY_ARCH_APEX_RATIO = 0.50f
private const val ARCH_WIDTH_RATIO = 0.55f
private const val BEZIER_CONTROL_MULTIPLIER = 2.0f
private const val BOTTOM_PADDING = 30f
private const val MIN_ARCH_OPENNESS = 0.1f

private data class EyeDrawParams(
    val centerX: Float,
    val centerY: Float,
    val eyeWidth: Float,
    val eyeHeight: Float,
    val shiftX: Float,
    val shiftY: Float,
    val openness: Float,
    val pupilScale: Float,
    val spec: EyeSpec,
    val bottomEyelidArch: Float = 0.0f,
    val archPath: Path? = null
)

/**
 * Procedural Compose Canvas eye renderer.
 * Draws symmetrical, glowing robot eyes on an absolute OLED-black background (#000000)
 * with zero per-frame heap allocations across all data-driven eye modes and personality micro-expressions.
 */
@Composable
fun RobotEyesCanvas(
    state: EyeMotionState,
    spec: EyeSpec = EyeSpec.Default,
    microExpression: MicroExpression = MicroExpression(),
    eyeTiltDegrees: Float = 0.0f,
    leftEyeOpennessMultiplier: Float = 1.0f,
    rightEyeOpennessMultiplier: Float = 1.0f,
    onPoke: ((Offset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gestureModifier = if (onPoke != null) {
        modifier.pointerInput(onPoke) {
            detectTapGestures { offset -> onPoke(offset) }
        }
    } else {
        modifier
    }

    val archPath = remember { Path() }

    Canvas(
        modifier = gestureModifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        val eyeWidth = (size.width * 0.23f).coerceIn(80f, 320f)
        val eyeHeight = eyeWidth * 1.38f
        val eyeSpacing = (size.width * 0.14f).coerceIn(40f, 200f)
        val centerY = size.height * 0.5f

        val leftEyeCenterX = size.width * 0.5f - eyeSpacing * 0.5f - eyeWidth * 0.5f
        val rightEyeCenterX = size.width * 0.5f + eyeSpacing * 0.5f + eyeWidth * 0.5f

        val shiftX = state.gazeX * eyeWidth * 0.22f
        val shiftY = state.gazeY * eyeHeight * 0.20f
        val baseOpenness = (state.eyelidOpenness * microExpression.eyelidOpennessMultiplier).coerceIn(0.0f, 1.0f)
        val leftOpenness = (baseOpenness * leftEyeOpennessMultiplier).coerceIn(0.0f, 1.0f)
        val rightOpenness = (baseOpenness * rightEyeOpennessMultiplier).coerceIn(0.0f, 1.0f)

        val leftParams = EyeDrawParams(
            centerX = leftEyeCenterX,
            centerY = centerY,
            eyeWidth = eyeWidth,
            eyeHeight = eyeHeight,
            shiftX = shiftX,
            shiftY = shiftY,
            openness = leftOpenness,
            pupilScale = state.pupilScale,
            spec = spec,
            bottomEyelidArch = microExpression.bottomEyelidArch,
            archPath = archPath
        )

        if (eyeTiltDegrees != 0.0f) {
            rotate(degrees = eyeTiltDegrees, pivot = Offset(size.width * 0.5f, centerY)) {
                drawOrientedEye(-microExpression.eyeTiltDegrees, leftParams)
                val rightParams = leftParams.copy(centerX = rightEyeCenterX, openness = rightOpenness)
                drawOrientedEye(microExpression.eyeTiltDegrees * RIGHT_TILT_RATIO, rightParams)
            }
        } else {
            drawOrientedEye(-microExpression.eyeTiltDegrees, leftParams)
            val rightParams = leftParams.copy(centerX = rightEyeCenterX, openness = rightOpenness)
            drawOrientedEye(microExpression.eyeTiltDegrees * RIGHT_TILT_RATIO, rightParams)
        }
    }
}

private fun DrawScope.drawOrientedEye(
    tiltDegrees: Float,
    params: EyeDrawParams
) {
    if (tiltDegrees != 0.0f) {
        rotate(degrees = tiltDegrees, pivot = Offset(params.centerX, params.centerY)) {
            drawEye(params)
        }
    } else {
        drawEye(params)
    }
}

private fun DrawScope.drawEye(params: EyeDrawParams) {
    if (params.openness < 0.04f) {
        val slitHeight = 4f
        val slitWidth = params.eyeWidth * 0.85f
        drawRoundRect(
            color = params.spec.primaryColor.copy(alpha = 0.45f),
            topLeft = Offset(params.centerX - slitWidth * 0.5f + params.shiftX, params.centerY - slitHeight * 0.5f + params.shiftY),
            size = Size(slitWidth, slitHeight),
            cornerRadius = CornerRadius(slitHeight * 0.5f, slitHeight * 0.5f)
        )
        return
    }

    val currentEyeHeight = params.eyeHeight * params.openness
    val eyeCornerRadius = CornerRadius(
        params.eyeWidth * 0.5f,
        (currentEyeHeight * 0.5f).coerceAtMost(params.eyeWidth * 0.5f)
    )

    // Outer Glow & Main Pill
    val glowRadius = params.eyeWidth * params.spec.glowRadiusMultiplier * params.openness
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(params.spec.glowColor.copy(alpha = params.spec.glowColor.alpha * params.openness), Color.Transparent),
            center = Offset(params.centerX + params.shiftX * 0.4f, params.centerY + params.shiftY * 0.4f),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(params.centerX + params.shiftX * 0.4f, params.centerY + params.shiftY * 0.4f)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(params.spec.primaryColor, params.spec.secondaryColor),
            startY = params.centerY - currentEyeHeight * 0.5f + params.shiftY,
            endY = params.centerY + currentEyeHeight * 0.5f + params.shiftY
        ),
        topLeft = Offset(params.centerX - params.eyeWidth * 0.5f + params.shiftX, params.centerY - currentEyeHeight * 0.5f + params.shiftY),
        size = Size(params.eyeWidth, currentEyeHeight),
        cornerRadius = eyeCornerRadius
    )

    EyeIrisCoreRenderer.drawCore(this, params, currentEyeHeight)
    drawSpecularDot(params, currentEyeHeight)
    drawBottomHappyArch(params, currentEyeHeight)
}

private fun DrawScope.drawSpecularDot(params: EyeDrawParams, currentEyeHeight: Float) {
    if (params.openness > 0.35f) {
        val coreWidth = params.eyeWidth * 0.52f * params.pupilScale
        val coreHeight = (currentEyeHeight * 0.52f * params.pupilScale).coerceAtLeast(2f)
        val pupilCenterX = params.centerX + params.shiftX * 1.15f
        val pupilCenterY = params.centerY + params.shiftY * 1.15f
        val dotRadius = coreWidth * 0.12f
        drawCircle(
            color = Color.White.copy(alpha = 0.85f * params.openness),
            radius = dotRadius,
            center = Offset(pupilCenterX - coreWidth * 0.18f, pupilCenterY - coreHeight * 0.22f)
        )
    }
}

private fun DrawScope.drawBottomHappyArch(params: EyeDrawParams, currentEyeHeight: Float) {
    if (params.bottomEyelidArch > 0.0f && params.archPath != null && params.openness > MIN_ARCH_OPENNESS) {
        val archProgress = params.bottomEyelidArch.coerceIn(0.0f, 1.0f)
        val bottomY = params.centerY + currentEyeHeight * 0.5f + params.shiftY
        val archApexHeight = currentEyeHeight * HAPPY_ARCH_APEX_RATIO * archProgress
        val halfArchWidth = params.eyeWidth * ARCH_WIDTH_RATIO

        params.archPath.reset()
        params.archPath.moveTo(params.centerX - halfArchWidth + params.shiftX, bottomY)
        params.archPath.quadraticTo(
            params.centerX + params.shiftX,
            bottomY - archApexHeight * BEZIER_CONTROL_MULTIPLIER,
            params.centerX + halfArchWidth + params.shiftX,
            bottomY
        )
        params.archPath.lineTo(params.centerX + halfArchWidth + params.shiftX, bottomY + BOTTOM_PADDING)
        params.archPath.lineTo(params.centerX - halfArchWidth + params.shiftX, bottomY + BOTTOM_PADDING)
        params.archPath.close()

        drawPath(path = params.archPath, color = DarkBackground)
    }
}

private object EyeIrisCoreRenderer {
    fun drawCore(
        scope: DrawScope,
        params: EyeDrawParams,
        currentEyeHeight: Float
    ) {
        val coreWidth = params.eyeWidth * 0.52f * params.pupilScale
        val coreHeight = (currentEyeHeight * 0.52f * params.pupilScale).coerceAtLeast(2f)
        val pupilCenterX = params.centerX + params.shiftX * 1.15f
        val pupilCenterY = params.centerY + params.shiftY * 1.15f

        when (params.spec.shape) {
            PupilShape.ROUNDED_PILL -> drawStandardCore(scope, pupilCenterX, pupilCenterY, coreWidth, coreHeight, params.openness, params.spec.coreColor)
            PupilShape.GEOMETRIC_TRACKER -> drawGeometricTrackerCore(scope, pupilCenterX, pupilCenterY, coreWidth, params.openness, params.spec)
            PupilShape.SCAN_LINE -> drawScanLineCore(scope, pupilCenterX, pupilCenterY, coreWidth, coreHeight, params.openness, params.spec)
            PupilShape.CONCENTRIC_ORBIT -> drawConcentricOrbitCore(scope, pupilCenterX, pupilCenterY, coreWidth, params.openness, params.spec)
            PupilShape.BURST_RADIAL -> drawStandardCore(scope, pupilCenterX, pupilCenterY, coreWidth * 1.15f, coreHeight * 1.15f, params.openness, params.spec.coreColor)
        }
    }

    private fun drawStandardCore(
        scope: DrawScope,
        pupilCenterX: Float,
        pupilCenterY: Float,
        coreWidth: Float,
        coreHeight: Float,
        openness: Float,
        coreColor: Color
    ) {
        val cornerRadius = CornerRadius(coreWidth * 0.5f, (coreHeight * 0.5f).coerceAtMost(coreWidth * 0.5f))
        scope.drawRoundRect(
            color = coreColor.copy(alpha = 0.90f * openness),
            topLeft = Offset(pupilCenterX - coreWidth * 0.5f, pupilCenterY - coreHeight * 0.5f),
            size = Size(coreWidth, coreHeight),
            cornerRadius = cornerRadius
        )
    }

    private fun drawGeometricTrackerCore(
        scope: DrawScope,
        pupilCenterX: Float,
        pupilCenterY: Float,
        coreWidth: Float,
        openness: Float,
        spec: EyeSpec
    ) {
        scope.drawCircle(
            color = spec.secondaryColor.copy(alpha = 0.95f * openness),
            radius = (coreWidth * 0.28f).coerceAtLeast(2f),
            center = Offset(pupilCenterX, pupilCenterY)
        )
        val accentOrbitRadius = coreWidth * 0.42f
        val accentRadius = (coreWidth * 0.08f).coerceAtLeast(2f)
        val accentColor = spec.secondaryColor.copy(alpha = 0.90f * openness)
        for (i in 0..2) {
            val angle = BASE_ANGLE_RAD + i * DEG_120_RAD
            val ax = pupilCenterX + accentOrbitRadius * cos(angle)
            val ay = pupilCenterY + accentOrbitRadius * sin(angle)
            scope.drawCircle(color = accentColor, radius = accentRadius, center = Offset(ax, ay))
        }
        scope.drawCircle(
            color = spec.secondaryColor.copy(alpha = 0.45f * openness),
            radius = accentOrbitRadius,
            center = Offset(pupilCenterX, pupilCenterY),
            style = OrbitStrokeLight
        )
    }

    private fun drawScanLineCore(
        scope: DrawScope,
        pupilCenterX: Float,
        pupilCenterY: Float,
        coreWidth: Float,
        coreHeight: Float,
        openness: Float,
        spec: EyeSpec
    ) {
        drawStandardCore(scope, pupilCenterX, pupilCenterY, coreWidth, coreHeight, openness, spec.coreColor)
        val scanLineColor = spec.secondaryColor.copy(alpha = 0.55f * openness)
        val halfWidth = coreWidth * 0.48f
        val yOffset1 = coreHeight * 0.28f
        scope.drawLine(color = scanLineColor, start = Offset(pupilCenterX - halfWidth * 0.85f, pupilCenterY - yOffset1), end = Offset(pupilCenterX + halfWidth * 0.85f, pupilCenterY - yOffset1), strokeWidth = 2f)
        scope.drawLine(color = scanLineColor.copy(alpha = 0.75f * openness), start = Offset(pupilCenterX - halfWidth, pupilCenterY), end = Offset(pupilCenterX + halfWidth, pupilCenterY), strokeWidth = 2.5f)
        scope.drawLine(color = scanLineColor, start = Offset(pupilCenterX - halfWidth * 0.85f, pupilCenterY + yOffset1), end = Offset(pupilCenterX + halfWidth * 0.85f, pupilCenterY + yOffset1), strokeWidth = 2f)
    }

    private fun drawConcentricOrbitCore(
        scope: DrawScope,
        pupilCenterX: Float,
        pupilCenterY: Float,
        coreWidth: Float,
        openness: Float,
        spec: EyeSpec
    ) {
        scope.drawCircle(
            color = spec.secondaryColor.copy(alpha = 0.95f * openness),
            radius = (coreWidth * 0.16f).coerceAtLeast(2f),
            center = Offset(pupilCenterX, pupilCenterY)
        )
        val ringColor = spec.coreColor.copy(alpha = 0.75f * openness)
        val maxRadius = coreWidth * 0.52f
        scope.drawCircle(color = ringColor, radius = maxRadius * 0.40f, center = Offset(pupilCenterX, pupilCenterY), style = OrbitStrokeLight)
        scope.drawCircle(color = ringColor, radius = maxRadius * 0.72f, center = Offset(pupilCenterX, pupilCenterY), style = OrbitStrokeMedium)
        scope.drawCircle(color = ringColor.copy(alpha = 0.50f * openness), radius = maxRadius * 0.98f, center = Offset(pupilCenterX, pupilCenterY), style = OrbitStrokeLight)
    }
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
