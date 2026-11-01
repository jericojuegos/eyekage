package com.eyekage

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyekage.feature.companion.CompanionScreen
import com.eyekage.feature.companion.CompanionViewModel
import com.eyekage.feature.settings.SettingsScreen
import com.eyekage.feature.settings.SettingsViewModel
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.EyeKageTheme

enum class AppScreen {
    COMPANION,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as EyeKageApplication).container

        setContent {
            EyeKageTheme {
                val companionViewModel: CompanionViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return CompanionViewModel(
                                preferencesRepository = appContainer.preferencesRepository,
                                faceObservationSource = appContainer.faceObservationSource,
                                memoryRepository = appContainer.memoryRepository,
                                personalityEngine = appContainer.personalityEngine,
                                voiceCommandSource = appContainer.voiceCommandSource
                            ) as T
                        }
                    }
                )

                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(
                                preferencesRepository = appContainer.preferencesRepository,
                                memoryRepository = appContainer.memoryRepository,
                                personalityEngine = appContainer.personalityEngine
                            ) as T
                        }
                    }
                )

                val companionUiState by companionViewModel.uiState.collectAsState()
                var currentScreen by remember { mutableStateOf(AppScreen.COMPANION) }

                BackHandler(enabled = currentScreen == AppScreen.SETTINGS) {
                    currentScreen = AppScreen.COMPANION
                }

                // Desk Mode: keep screen awake without requiring WAKE_LOCK permission
                if (companionUiState.isDeskModeEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { target ->
                        when (target) {
                            AppScreen.COMPANION -> {
                                CompanionScreen(
                                    uiState = companionUiState,
                                    onEvent = companionViewModel::onEvent,
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                val settingsUiState by settingsViewModel.uiState.collectAsState()
                                SettingsScreen(
                                    uiState = settingsUiState,
                                    onEvent = settingsViewModel::onEvent,
                                    onNavigateBack = { currentScreen = AppScreen.COMPANION }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
