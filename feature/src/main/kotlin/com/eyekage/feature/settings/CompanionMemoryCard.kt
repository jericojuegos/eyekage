package com.eyekage.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eyekage.core.model.CompanionMemory
import com.eyekage.core.model.MoodState
import com.eyekage.ui.theme.DarkError
import com.eyekage.ui.theme.DarkOutline
import com.eyekage.ui.theme.DarkPrimary
import com.eyekage.ui.theme.DarkSurface
import com.eyekage.ui.theme.DarkSurfaceVariant
import com.eyekage.ui.theme.ReactionAwakeningPrimary
import com.eyekage.ui.theme.Typography

private const val CARD_CORNER_RADIUS_DP = 16
private const val STAT_CARD_CORNER_RADIUS_DP = 12
private const val ICON_CONTAINER_SIZE_DP = 36
private const val ICON_ALPHA = 0.15f
private const val SUBTITLE_ALPHA = 0.6f
private const val LABEL_ALPHA = 0.5f
private const val DIALOG_BODY_ALPHA = 0.8f
private const val CANCEL_BUTTON_ALPHA = 0.7f
private const val OUTLINE_BUTTON_BORDER_ALPHA = 0.4f
private const val MOOD_CHIP_CORNER_RADIUS_DP = 20
private const val MOOD_CHIP_BG_ALPHA = 0.12f
private const val MOOD_CHIP_BORDER_ALPHA = 0.4f
private const val MOOD_DOT_SIZE_DP = 8
private const val BOND_DOT_SIZE_DP = 6
private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_HOUR = 60

private val DrowsyMoodColor = Color(0xFF818CF8)
private val RestingMoodColor = Color(0xFF94A3B8)

data class CompanionMemoryCardState(
    val memory: CompanionMemory = CompanionMemory(),
    val currentMood: MoodState = MoodState.ALERT,
    val curiosityPercent: Int = 50,
    val playfulnessPercent: Int = 50
)

/**
 * Settings card displaying the companion robot's live personality metrics,
 * bond progression tier, lifetime interaction statistics, and factory reset action.
 */
@Composable
fun CompanionMemoryCard(
    state: CompanionMemoryCardState,
    onResetMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            MemoryCardHeader()
            Spacer(modifier = Modifier.height(16.dp))

            MoodAndBondRow(
                currentMood = state.currentMood,
                bondTier = state.memory.bondTier
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatsGrid(
                totalPokes = state.memory.totalPokes,
                totalGazeSeconds = state.memory.totalGazeSeconds,
                curiosityPercent = state.curiosityPercent,
                playfulnessPercent = state.playfulnessPercent
            )
            Spacer(modifier = Modifier.height(20.dp))

            ResetMemoryTriggerButton(onClick = { showResetDialog = true })
        }
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                onResetMemory()
                showResetDialog = false
            }
        )
    }
}

@Composable
private fun ResetMemoryTriggerButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(STAT_CARD_CORNER_RADIUS_DP.dp),
        border = BorderStroke(1.dp, DarkError.copy(alpha = OUTLINE_BUTTON_BORDER_ALPHA)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DarkError
        )
    ) {
        Text(
            text = "Reset Companion Memory",
            style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceVariant,
        title = {
            Text(
                text = "Reset Companion Memory?",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Text(
                text = "This will clear all accumulated interactions and reset bond progression to Tier 1. " +
                    "Your eye mode and preferences will be preserved.",
                style = Typography.bodyMedium,
                color = Color.White.copy(alpha = DIALOG_BODY_ALPHA)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Reset", color = DarkError, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color.White.copy(alpha = CANCEL_BUTTON_ALPHA))
            }
        }
    )
}

@Composable
private fun MemoryCardHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(ICON_CONTAINER_SIZE_DP.dp)
                .background(DarkPrimary.copy(alpha = ICON_ALPHA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🧠", fontSize = 18.sp)
        }

        Column {
            Text(
                text = "Companion Memory & Affinity",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Local on-device personality and bonding progression",
                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = SUBTITLE_ALPHA)
            )
        }
    }
}

@Composable
private fun MoodAndBondRow(
    currentMood: MoodState,
    bondTier: Int
) {
    val (moodColor, moodLabel) = when (currentMood) {
        MoodState.CURIOUS -> DarkPrimary to "Curious"
        MoodState.PLAYFUL -> ReactionAwakeningPrimary to "Playful"
        MoodState.ALERT -> DarkPrimary to "Alert"
        MoodState.DROWSY -> DrowsyMoodColor to "Drowsy"
        MoodState.RESTING -> RestingMoodColor to "Resting"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(MOOD_CHIP_CORNER_RADIUS_DP.dp),
            color = moodColor.copy(alpha = MOOD_CHIP_BG_ALPHA),
            border = BorderStroke(1.dp, moodColor.copy(alpha = MOOD_CHIP_BORDER_ALPHA))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(MOOD_DOT_SIZE_DP.dp)
                        .background(moodColor, CircleShape)
                )
                Text(
                    text = "Mood: $moodLabel",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = moodColor
                )
            }
        }

        BondTierIndicator(bondTier = bondTier)
    }
}

@Composable
private fun BondTierIndicator(bondTier: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Bond Tier $bondTier",
            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Spacer(modifier = Modifier.size(2.dp))
        for (i in 1..5) {
            Box(
                modifier = Modifier
                    .size(BOND_DOT_SIZE_DP.dp)
                    .background(
                        color = if (i <= bondTier) DarkPrimary else DarkOutline,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun StatsGrid(
    totalPokes: Int,
    totalGazeSeconds: Long,
    curiosityPercent: Int,
    playfulnessPercent: Int
) {
    val formattedDuration = formatGazeDuration(totalGazeSeconds)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "Total Interactions",
                value = "$totalPokes",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Gaze Time",
                value = formattedDuration,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "Curiosity Level",
                value = "$curiosityPercent%",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Playfulness",
                value = "$playfulnessPercent%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(STAT_CARD_CORNER_RADIUS_DP.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = Typography.bodyMedium.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = LABEL_ALPHA)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}

private fun formatGazeDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val remainingSeconds = totalSeconds % SECONDS_PER_MINUTE
    val hours = minutes / MINUTES_PER_HOUR
    val remainingMinutes = minutes % MINUTES_PER_HOUR

    return when {
        totalSeconds < SECONDS_PER_MINUTE -> "${totalSeconds}s"
        minutes < MINUTES_PER_HOUR -> "${minutes}m ${remainingSeconds}s"
        else -> "${hours}h ${remainingMinutes}m"
    }
}
