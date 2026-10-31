package com.eyekage.feature.companion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyekage.ui.R
import com.eyekage.ui.theme.CameraIndicatorActive
import com.eyekage.ui.theme.CameraIndicatorInactive
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.Typography

@Composable
fun CompanionScreen(
    uiState: CompanionUiState,
    onEvent: (CompanionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onEvent(CompanionUiEvent.OnScreenTapped)
            }
    ) {
        // Privacy Camera Active/Inactive Indicator (top-end)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(10.dp)
                .background(
                    color = if (uiState.isCameraTrackingActive) CameraIndicatorActive else CameraIndicatorInactive,
                    shape = CircleShape
                )
        )

        // Center companion faceplate label (placeholder for Wave 1 procedural Canvas eyes)
        Text(
            text = stringResource(id = R.string.app_name),
            style = Typography.headlineMedium,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
