package com.eyekage

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eyekage.feature.companion.CompanionScreen
import com.eyekage.feature.companion.CompanionViewModel
import com.eyekage.ui.theme.DarkBackground
import com.eyekage.ui.theme.EyeKageTheme

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
                                faceObservationSource = appContainer.faceObservationSource
                            ) as T
                        }
                    }
                )

                val uiState by companionViewModel.uiState.collectAsState()

                // Desk Mode: keep screen awake without requiring WAKE_LOCK permission
                if (uiState.isDeskModeEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    CompanionScreen(
                        uiState = uiState,
                        onEvent = companionViewModel::onEvent
                    )
                }
            }
        }
    }
}
