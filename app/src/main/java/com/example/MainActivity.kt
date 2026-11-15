package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.SpectrumBlocksScreen
import com.example.ui.theme.SpectrumBlocksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            SpectrumBlocksTheme {
                val gameViewModel: GameViewModel = viewModel()
                LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
                    gameViewModel.pauseGame()
                }
                SpectrumBlocksScreen(viewModel = gameViewModel)
            }
        }
    }
}
