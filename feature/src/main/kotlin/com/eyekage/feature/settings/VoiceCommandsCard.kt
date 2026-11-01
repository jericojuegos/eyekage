package com.eyekage.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
private const val ACTIVE_PILL_ALPHA = 0.10f
private const val INACTIVE_PILL_ALPHA = 0.05f
private const val SECTION_LABEL_ALPHA = 0.4f

private val AmberAccent = Color(0xFFF59E0B)
private val EmeraldAccent = Color(0xFF10B981)
private val ChipBackground = Color(0xFF1E1B4B)
private val ChipText = Color(0xFF94A3B8)
private val ChipHighlight = Color(0xFF22D3EE)

val SupportedPhrases = listOf(
    "EyeKage, awaken!",
    "Normal eyes",
    "Crimson tracker",
    "White scan",
    "Purple orbit",
    "Sleep",
    "Blink",
    "Look at me"
)

/**
 * AMOLED dark settings card providing opt-in voice command controls,
 * real-time microphone status feedback, supported command chips, and local privacy disclosures.
 */
@Composable
fun VoiceCommandsCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
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
            VoiceCommandsHeader(isEnabled = isEnabled, onToggle = onToggle)
            Spacer(modifier = Modifier.height(14.dp))
            MicrophoneStatusPill(isEnabled = isEnabled)
            Spacer(modifier = Modifier.height(16.dp))
            SupportedCommandsSection(isEnabled = isEnabled)
            Spacer(modifier = Modifier.height(16.dp))
            LocalPrivacyBadge()
        }
    }
}

@Composable
private fun VoiceCommandsHeader(
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
                        color = AmberAccent.copy(alpha = ICON_ALPHA),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙", fontSize = 18.sp)
            }

            Column {
                Text(
                    text = "Voice Commands & Audio",
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Control eyes & reactions hands-free with on-device speech",
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
                checkedTrackColor = AmberAccent,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = DarkOutline
            )
        )
    }
}

@Composable
private fun MicrophoneStatusPill(isEnabled: Boolean) {
    val pillBgColor = if (isEnabled) {
        AmberAccent.copy(alpha = ACTIVE_PILL_ALPHA)
    } else {
        Color.White.copy(alpha = INACTIVE_PILL_ALPHA)
    }
    val statusText = if (isEnabled) {
        "Microphone Active (On-Device Listening)"
    } else {
        "Microphone Inactive / Off"
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
                    color = if (isEnabled) AmberAccent else Color.Gray,
                    shape = CircleShape
                )
        )
        Text(
            text = statusText,
            style = Typography.labelSmall.copy(
                color = if (isEnabled) AmberAccent else Color.White.copy(alpha = STATUS_TEXT_ALPHA),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportedCommandsSection(isEnabled: Boolean) {
    Column {
        Text(
            text = "WHAT CAN YOU SAY?",
            style = Typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = SECTION_LABEL_ALPHA)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (phrase in SupportedPhrases) {
                Box(
                    modifier = Modifier
                        .background(
                            color = ChipBackground,
                            shape = RoundedCornerShape(CHIP_CORNER_RADIUS_DP.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "\"$phrase\"",
                        style = Typography.bodySmall.copy(
                            color = if (isEnabled) ChipHighlight else ChipText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalPrivacyBadge() {
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
            text = "100% On-Device · No Audio Stored · Zero Network Access",
            style = Typography.labelSmall.copy(
                color = EmeraldAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}
