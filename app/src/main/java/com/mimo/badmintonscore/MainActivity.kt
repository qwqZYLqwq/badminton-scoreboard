package com.mimo.badmintonscore

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mimo.badmintonscore.ui.screens.MatchScoreScreen
import com.mimo.badmintonscore.ui.screens.ModeSelectScreen
import com.mimo.badmintonscore.ui.theme.BadmintonScoreTheme

sealed class Screen {
    object ModeSelect : Screen()
    data class Match(val targetScore: Int, val leftName: String, val rightName: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        setContent {
            BadmintonScoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.ModeSelect) }

                    when (val screen = currentScreen) {
                        is Screen.ModeSelect -> {
                            ModeSelectScreen(
                                onSelectMode = { score, left, right ->
                                    currentScreen = Screen.Match(score, left, right)
                                }
                            )
                        }
                        is Screen.Match -> {
                            BackHandler {
                                currentScreen = Screen.ModeSelect
                            }

                            MatchScoreScreen(
                                targetScore = screen.targetScore,
                                initialLeftName = screen.leftName,
                                initialRightName = screen.rightName,
                                onExitToHome = {
                                    currentScreen = Screen.ModeSelect
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }
}
