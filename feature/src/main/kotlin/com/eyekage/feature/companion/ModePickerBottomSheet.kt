package com.eyekage.feature.companion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyekage.core.model.EyeModeSpec
import com.eyekage.core.modes.DefaultModeCatalog
import com.eyekage.ui.theme.DarkOutline
import com.eyekage.ui.theme.DarkSurface
import com.eyekage.ui.theme.DarkSurfaceVariant
import com.eyekage.ui.theme.Typography

/**
 * Resolves the string resource ID for an eye mode's display label.
 */
fun resolveModeLabelResId(labelKey: String): Int {
    return when (labelKey) {
        "mode_label_normal" -> com.eyekage.ui.R.string.mode_label_normal
        "mode_label_crimson_tracker" -> com.eyekage.ui.R.string.mode_label_crimson_tracker
        "mode_label_white_scan" -> com.eyekage.ui.R.string.mode_label_white_scan
        "mode_label_purple_orbit" -> com.eyekage.ui.R.string.mode_label_purple_orbit
        "reaction_label_awakening" -> com.eyekage.ui.R.string.reaction_label_awakening
        else -> com.eyekage.ui.R.string.mode_label_normal
    }
}

/**
 * Bottom sheet modal presenting selectable eye modes matching the dark glassmorphic styling
 * from design-reference/stitch-prompt.md.
 *
 * @param currentModeId Currently active neutral mode identifier.
 * @param onModeSelected Callback invoked when a mode card is tapped.
 * @param onDismissRequest Callback invoked when the sheet is dismissed.
 * @param modifier Layout modifier.
 * @param sheetState Bottom sheet state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModePickerBottomSheet(
    currentModeId: String,
    onModeSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(DarkOutline, RoundedCornerShape(2.dp))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Eye Modes",
                style = Typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = DefaultModeCatalog.getSelectableModes(),
                    key = { it.id.value }
                ) { mode ->
                    ModeChipCard(
                        mode = mode,
                        isSelected = mode.id.value == currentModeId,
                        onClick = {
                            onModeSelected(mode.id.value)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Interactive card chip representing a single selectable eye mode.
 */
@Composable
private fun ModeChipCard(
    mode: EyeModeSpec,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF22D3EE) else DarkOutline
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val primaryColor = Color(mode.primaryColorArgb)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .width(112.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            // Mode preview indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.20f),
                        shape = CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = primaryColor,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Localized display name
            Text(
                text = stringResource(id = resolveModeLabelResId(mode.labelKey)),
                style = Typography.labelLarge,
                color = if (isSelected) Color(0xFF22D3EE) else Color.White
            )
        }
    }
}
