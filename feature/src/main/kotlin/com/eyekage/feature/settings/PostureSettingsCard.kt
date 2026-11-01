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
import com.eyekage.core.model.PostureState
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
private val AmberAccent = Color(0xFFF59E0B)
private val ChipBackground = Color(0xFF1E1B4B)
private val ChipText = Color(0xFF94A3B8)

data class PostureTimingOption(
    val seconds: Int,
    val label: String
)

val SupportedPostureTimingOptions = listOf(
    PostureTimingOption(seconds = 15, label = "Strict (15s)"),
    PostureTimingOption(seconds = 30, label = "Standard (30s)"),
    PostureTimingOption(seconds = 45, label = "Gentle (45s)")
)

/**
 * AMOLED dark settings card providing opt-in desk posture reminder controls,
 * real-time posture status feedback, slouch detection sensitivity chips, and local privacy disclosures.
 */
@Composable
fun PostureSettingsCard(
    isEnabled: Boolean,
    slouchThresholdSec: Int,
    postureState: PostureState,
    onToggle: (Boolean) -> Unit,
    onSelectThreshold: (Int) -> Unit,
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
            PostureSettingsHeader(isEnabled = isEnabled, onToggle = onToggle)
            Spacer(modifier = Modifier.height(14.dp))
            PostureStatusPill(isEnabled = isEnabled, postureState = postureState)
            Spacer(modifier = Modifier.height(16.dp))
            PostureSensitivitySection(
                isEnabled = isEnabled,
                currentThresholdSec = slouchThresholdSec,
                onSelectThreshold = onSelectThreshold
            )
            Spacer(modifier = Modifier.height(16.dp))
            PosturePrivacyBadge()
        }
    }
}

@Composable
private fun PostureSettingsHeader(
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
                Text(text = "🧘", fontSize = 18.sp)
            }

            Column {
                Text(
                    text = "Desk Posture & Awareness",
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Ambient reminders when slouching at your desk",
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
private fun PostureStatusPill(
    isEnabled: Boolean,
    postureState: PostureState
) {
    val (pillBgColor, dotColor, statusText, textColor) = when {
        !isEnabled -> {
            Tuple4(
                Color.White.copy(alpha = INACTIVE_PILL_ALPHA),
                Color.Gray,
                "Posture Reminders Inactive / Off",
                Color.White.copy(alpha = STATUS_TEXT_ALPHA)
            )
        }
        postureState == PostureState.SLOUCHING_DOWN -> {
            Tuple4(
                AmberAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                AmberAccent,
                "▲ Slouching Detected",
                AmberAccent
            )
        }
        postureState == PostureState.LEANING_CLOSE -> {
            Tuple4(
                AmberAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                AmberAccent,
                "▲ Too Close (<30cm)",
                AmberAccent
            )
        }
        postureState == PostureState.TILTED_CURIOUS -> {
            Tuple4(
                CyanAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                CyanAccent,
                "● Tilted (Curious)",
                CyanAccent
            )
        }
        postureState == PostureState.TURNED_AWAY -> {
            Tuple4(
                Color.White.copy(alpha = INACTIVE_PILL_ALPHA),
                Color.Gray,
                "● Turned Away",
                Color.White.copy(alpha = STATUS_TEXT_ALPHA)
            )
        }
        else -> {
            Tuple4(
                EmeraldAccent.copy(alpha = ACTIVE_PILL_ALPHA),
                EmeraldAccent,
                "● Balanced Posture",
                EmeraldAccent
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = pillBgColor,
                shape = RoundedCornerShape(PILL_CORNER_RADIUS_DP.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(STATUS_DOT_SIZE_DP.dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
        Text(
            text = statusText,
            style = Typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun PostureSensitivitySection(
    isEnabled: Boolean,
    currentThresholdSec: Int,
    onSelectThreshold: (Int) -> Unit
) {
    Column {
        Text(
            text = "REMINDER TIMING",
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
            for (option in SupportedPostureTimingOptions) {
                val isSelected = option.seconds == currentThresholdSec
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
                            onSelectThreshold(option.seconds)
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
private fun PosturePrivacyBadge() {
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
            text = "100% On-Device Facial Geometry · Zero Photos Saved",
            style = Typography.labelSmall.copy(
                color = EmeraldAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
