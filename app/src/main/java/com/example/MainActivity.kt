package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.AppVisualTheme
import com.example.game.GameViewModel
import com.example.ui.SpectrumBlocksApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
            val isLight = uiState.statsAndSettings.visualTheme == AppVisualTheme.DAYLIGHT_CERAMIC

            androidx.compose.runtime.SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (isLight) {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isLight) {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    }
                )
            }

            LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
                gameViewModel.pauseGame()
            }
            SpectrumBlocksApp(viewModel = gameViewModel)
        }
    }
}
