package com.eyekage.feature.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eyekage.core.model.FaceObservation
import java.util.Locale

private const val HALF = 0.5f
private const val MIN_BOX_SIZE = 40f
private const val PERCENT_MULTIPLIER = 100f
private const val HUD_FONT_SIZE = 11
private const val BOX_ALPHA = 0.75f
private val BoundingBoxColor = Color(0xFF10B981)
private val ReticleColor = Color(0xFF22D3EE)
private val HudBackgroundColor = Color(0xCC0A0A12)

/**
 * Debug overlay displaying normalized face bounding box and coordinates as vector shapes.
 */
@Composable
fun DebugFaceOverlay(
    observation: FaceObservation,
    modifier: Modifier = Modifier
) {
    if (!observation.isFaceDetected) return

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            val faceCenterX = ((observation.normalizedX + 1.0f) * HALF) * canvasWidth
            val faceCenterY = ((observation.normalizedY + 1.0f) * HALF) * canvasHeight

            val boxWidth = (observation.boundingBoxWidth * HALF * canvasWidth).coerceAtLeast(MIN_BOX_SIZE)
            val boxHeight = (observation.boundingBoxHeight * HALF * canvasHeight).coerceAtLeast(MIN_BOX_SIZE)

            val strokeWidth = 2.dp.toPx()
            val stroke = Stroke(width = strokeWidth)

            // Draw bounding box wireframe
            drawRect(
                color = BoundingBoxColor.copy(alpha = BOX_ALPHA),
                topLeft = Offset(faceCenterX - boxWidth * HALF, faceCenterY - boxHeight * HALF),
                size = Size(boxWidth, boxHeight),
                style = stroke
            )

            // Center target reticle
            drawCircle(
                color = ReticleColor,
                radius = 6.dp.toPx(),
                center = Offset(faceCenterX, faceCenterY)
            )
        }

        // Numerical HUD badge at top-start
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(HudBackgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val hudText = String.format(
                Locale.US,
                "NORM: [%+.2f, %+.2f] | CONF: %.0f%%",
                observation.normalizedX,
                observation.normalizedY,
                observation.confidence * PERCENT_MULTIPLIER
            )
            Text(
                text = hudText,
                color = ReticleColor,
                fontSize = HUD_FONT_SIZE.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
