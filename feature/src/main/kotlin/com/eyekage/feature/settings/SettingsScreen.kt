package com.eyekage.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eyekage.core.model.EyeModeSpec
import com.eyekage.feature.companion.resolveModeLabelResId
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.DarkOutline
import com.eyekage.ui.theme.DarkPrimary
import com.eyekage.ui.theme.DarkSurface
import com.eyekage.ui.theme.DarkSurfaceVariant
import com.eyekage.ui.theme.Typography

private const val CARD_CORNER_RADIUS_DP = 16
private const val DROPDOWN_CORNER_RADIUS_DP = 10
private const val MODE_INDICATOR_SIZE_DP = 10
private const val DROPDOWN_MENU_WIDTH_RATIO = 0.85f
private const val FOOTER_TEXT_ALPHA = 0.4f
private const val SUBTITLE_TEXT_ALPHA = 0.6f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EyeKage Settings",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CompanionPreferencesCard(uiState = uiState, onEvent = onEvent)
            DataPrivacyDisclosureCard()
            SettingsFooter()
        }
    }
}

@Composable
private fun SettingsFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "EyeKage v1.0.0-alpha · 100% Offline AI Companion",
            style = Typography.bodyMedium.copy(fontSize = 11.sp),
            color = Color.White.copy(alpha = FOOTER_TEXT_ALPHA)
        )
    }
}

@Composable
private fun CompanionPreferencesCard(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
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
                .padding(20.dp)
        ) {
            Text(
                text = "Companion Preferences",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            PreferencesSwitchGroup(uiState = uiState, onEvent = onEvent)
            Spacer(modifier = Modifier.height(20.dp))
            ReducedMotionSegmentedControl(
                selectedOption = uiState.reducedMotionOverride,
                onOptionSelected = { onEvent(SettingsUiEvent.SetReducedMotion(it)) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            DefaultModeSelector(
                selectedModeId = uiState.defaultModeId,
                availableModes = uiState.availableModes,
                onModeSelected = { onEvent(SettingsUiEvent.SetDefaultMode(it)) }
            )
        }
    }
}

@Composable
private fun PreferencesSwitchGroup(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PreferenceSwitchRow(
            title = "Desk Companion Mode",
            subtitle = "Keep screen awake while EyeKage is active",
            checked = uiState.isDeskModeEnabled,
            onCheckedChange = { onEvent(SettingsUiEvent.SetDeskModeEnabled(it)) }
        )
        PreferenceSwitchRow(
            title = "Camera Vision",
            subtitle = "Enable front camera for real-time gaze tracking",
            checked = uiState.isCameraEnabled,
            onCheckedChange = { onEvent(SettingsUiEvent.SetCameraEnabled(it)) }
        )
        PreferenceSwitchRow(
            title = "Haptic Feedback",
            subtitle = "Subtle touch pulses on poke and interactions",
            checked = uiState.isHapticsEnabled,
            onCheckedChange = { onEvent(SettingsUiEvent.SetHapticsEnabled(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReducedMotionSegmentedControl(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Text(
        text = "Reduced Motion",
        style = Typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))

    val motionOptions = listOf("SYSTEM", "ON", "OFF")
    val selectedIndex = motionOptions.indexOf(selectedOption).let {
        if (it >= 0) it else 0
    }

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        motionOptions.forEachIndexed { index, option ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = motionOptions.size),
                onClick = { onOptionSelected(option) },
                selected = index == selectedIndex,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = DarkPrimary,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurfaceVariant,
                    inactiveContentColor = Color.White.copy(alpha = 0.8f),
                    activeBorderColor = DarkPrimary,
                    inactiveBorderColor = DarkOutline
                )
            ) {
                Text(
                    text = option,
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                )
            }
        }
    }
}

@Composable
private fun DefaultModeSelector(
    selectedModeId: String,
    availableModes: List<EyeModeSpec>,
    onModeSelected: (String) -> Unit
) {
    Text(
        text = "Default Eye Mode",
        style = Typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))
    DefaultModeDropdown(
        selectedModeId = selectedModeId,
        availableModes = availableModes,
        onModeSelected = onModeSelected
    )
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = Color.White.copy(alpha = SUBTITLE_TEXT_ALPHA)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DarkPrimary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = DarkSurfaceVariant,
                uncheckedBorderColor = DarkOutline
            )
        )
    }
}

@Composable
private fun DefaultModeDropdown(
    selectedModeId: String,
    availableModes: List<EyeModeSpec>,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSpec = availableModes.find { it.id.value == selectedModeId } ?: availableModes.firstOrNull()
    val selectedLabel = if (selectedSpec != null) {
        stringResource(resolveModeLabelResId(selectedSpec.labelKey))
    } else {
        selectedModeId
    }

    Box(modifier = modifier.fillMaxWidth()) {
        DefaultModeTriggerSurface(
            selectedLabel = selectedLabel,
            specColor = selectedSpec?.let { Color(it.primaryColorArgb) } ?: DarkPrimary,
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurfaceVariant).fillMaxWidth(DROPDOWN_MENU_WIDTH_RATIO)
        ) {
            availableModes.forEach { modeSpec ->
                DefaultModeMenuItem(
                    modeSpec = modeSpec,
                    isSelected = modeSpec.id.value == selectedModeId,
                    onSelect = {
                        onModeSelected(modeSpec.id.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DefaultModeTriggerSurface(
    selectedLabel: String,
    specColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(DROPDOWN_CORNER_RADIUS_DP.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(MODE_INDICATOR_SIZE_DP.dp).background(specColor, CircleShape))
                Text(
                    text = selectedLabel,
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
            Text(text = "▼", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun DefaultModeMenuItem(
    modeSpec: EyeModeSpec,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val label = stringResource(resolveModeLabelResId(modeSpec.labelKey))
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(MODE_INDICATOR_SIZE_DP.dp)
                        .background(Color(modeSpec.primaryColorArgb), CircleShape)
                )
                Text(
                    text = label,
                    style = Typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) DarkPrimary else Color.White
                    )
                )
            }
        },
        onClick = onSelect
    )
}
