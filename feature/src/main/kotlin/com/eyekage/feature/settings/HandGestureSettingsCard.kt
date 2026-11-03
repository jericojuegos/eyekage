package com.eyekage.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eyekage.core.model.GestureSensitivity
import com.eyekage.core.model.HandGesture
import com.eyekage.core.model.HandObservation
import com.eyekage.ui.theme.DarkOutline
import com.eyekage.ui.theme.DarkSurface
import com.eyekage.ui.theme.Typography

private const val CARD_CORNER_RADIUS_DP = 16
private const val CHIP_CORNER_RADIUS_DP = 8
private const val PILL_CORNER_RADIUS_DP = 20
private const val SHIELD_CORNER_RADIUS_DP = 8
private const val ICON_CONTAINER_SIZE_DP = 36
private const val STATUS_DOT_SIZE_DP = 8

private const val ICON_ALPHA = 0.15f
private const val SUBTITLE_ALPHA = 0.6f
private const val STATUS_TEXT_ALPHA = 0.8f
private const val SHIELD_ALPHA = 0.12f
private const val ACTIVE_PILL_ALPHA = 0.12f
private const val INACTIVE_PILL_ALPHA = 0.05f
private const val SECTION_LABEL_ALPHA = 0.4f

private val CyanAccent = Color(0xFF22D3EE)
private val EmeraldAccent = Color(0xFF10B981)
private val ChipBackground = Color(0xFF1E1B4B)
private val ChipText = Color(0xFF94A3B8)

data class GestureSensitivityOption(
    val sensitivity: GestureSensitivity,
    val label: String
)

val SupportedGestureSensitivityOptions = listOf(
    GestureSensitivityOption(sensitivity = GestureSensitivity.LOW, label = "Low (Strict)"),
    GestureSensitivityOption(sensitivity = GestureSensitivity.NORMAL, label = "Normal (Balanced)"),
    GestureSensitivityOption(sensitivity = GestureSensitivity.HIGH, label = "High (Quick)")
)

/**
 * AMOLED dark settings card providing opt-in macroscopic hand gesture recognition controls,
 * real-time gesture status feedback, sensitivity selection chips, and on-device privacy disclosures.
 */
@Composable
fun HandGestureSettingsCard(
    isEnabled: Boolean,
    sensitivity: String,
    activeGesture: HandObservation,
    onToggle: (Boolean) -> Unit,
    onSelectSensitivity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            HandGestureSettingsHeader(isEnabled = isEnabled, onToggle = onToggle)
            Spacer(modifier = Modifier.height(14.dp))
            HandGestureStatusPill(isEnabled = isEnabled, activeGesture = activeGesture)
            Spacer(modifier = Modifier.height(16.dp))
            HandGestureSensitivitySection(
                isEnabled = isEnabled,
                currentSensitivity = sensitivity,
                onSelectSensitivity = onSelectSensitivity
            )
            Spacer(modifier = Modifier.height(16.dp))
            HandGesturePrivacyBadge()
        }
    }
}

@Composable
private fun HandGestureSettingsHeader(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1.0f)
        ) {
            Box(
                modifier = Modifier
                    .size(ICON_CONTAINER_SIZE_DP.dp)
                    .background(
                        color = CyanAccent.copy(alpha = ICON_ALPHA),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🖐️", fontSize = 18.sp)
            }

            Column {
                Text(
                    text = "Hand Gestures & Interactions",
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Interact with EyeKage using hand gestures in front of the camera",
                    style = Typography.bodySmall.copy(
                        color = Color.White.copy(alpha = SUBTITLE_ALPHA)
                    )
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = DarkOutline
            )
        )
    }
}

@Composable
private fun HandGestureStatusPill(
    isEnabled: Boolean,
    activeGesture: HandObservation
) {
    val pillData = when {
        !isEnabled -> {
            GesturePillData(
                Color.White.copy(alpha = INACTIVE_PILL_ALPHA),
                Color.Gray,
                "Gesture Recognition Inactive / Off",
                Color.White.copy(alpha = STATUS_TEXT_ALPHA)
            )
        }
        activeGesture.isGestureDetected -> {
            val gestureName = when (activeGesture.gesture) {
                HandGesture.WAVE -> "👋 Wave Detected"
                HandGesture.PEACE_SIGN -> "✌️ Peace Sign"
                HandGesture.THUMBS_UP -> "👍 Thumbs Up"
                HandGesture.CLOSED_FIST -> "✊ Focus Fist"
                HandGesture.FLAT_PALM_STOP -> "✋ Hold / Stop"
                HandGesture.NONE -> "Recognizing..."
            }
            val confidencePct = (activeGesture.confidence * 100).toInt()
            GesturePillData(
                EmeraldAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                EmeraldAccent,
                "● Active: $gestureName ($confidencePct%)",
                EmeraldAccent
            )
        }
        else -> {
            GesturePillData(
                CyanAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                CyanAccent,
                "● Ready for Gestures (Wave, Peace, Thumbs Up, Fist, Stop)",
                CyanAccent
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = pillData.bgColor,
                shape = RoundedCornerShape(PILL_CORNER_RADIUS_DP.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(STATUS_DOT_SIZE_DP.dp)
                .background(
                    color = pillData.dotColor,
                    shape = CircleShape
                )
        )
        Text(
            text = pillData.text,
            style = Typography.labelSmall.copy(
                color = pillData.textColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun HandGestureSensitivitySection(
    isEnabled: Boolean,
    currentSensitivity: String,
    onSelectSensitivity: (String) -> Unit
) {
    Column {
        Text(
            text = "GESTURE SENSITIVITY",
            style = Typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = SECTION_LABEL_ALPHA)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (option in SupportedGestureSensitivityOptions) {
                val isSelected = option.sensitivity.name.equals(currentSensitivity, ignoreCase = true)
                val chipBorder = if (isSelected) {
                    BorderStroke(1.dp, CyanAccent)
                } else {
                    BorderStroke(1.dp, DarkOutline)
                }
                val chipBg = if (isSelected) {
                    ChipBackground
                } else {
                    DarkSurface
                }
                val textColor = if (!isEnabled) {
                    ChipText.copy(alpha = 0.5f)
                } else if (isSelected) {
                    CyanAccent
                } else {
                    ChipText
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isEnabled) {
                            onSelectSensitivity(option.sensitivity.name)
                        },
                    shape = RoundedCornerShape(CHIP_CORNER_RADIUS_DP.dp),
                    color = chipBg,
                    border = chipBorder
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.label,
                            style = Typography.bodySmall.copy(
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandGesturePrivacyBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = EmeraldAccent.copy(alpha = SHIELD_ALPHA),
                shape = RoundedCornerShape(SHIELD_CORNER_RADIUS_DP.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = "🛡", fontSize = 14.sp)
        Text(
            text = "100% On-Device Gesture Recognition · Zero Handprints or Photos Saved",
            style = Typography.labelSmall.copy(
                color = EmeraldAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

private data class GesturePillData(
    val bgColor: Color,
    val dotColor: Color,
    val text: String,
    val textColor: Color
)
