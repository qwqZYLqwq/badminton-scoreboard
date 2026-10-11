package com.mimo.badmintonscore

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mimo.badmintonscore.model.ActiveMatchRecord
import com.mimo.badmintonscore.model.ActiveMatchRepository
import com.mimo.badmintonscore.model.MatchPreferences
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.MatchType
import com.mimo.badmintonscore.service.MatchKeepAliveService
import com.mimo.badmintonscore.ui.screens.MatchScoreScreen
import com.mimo.badmintonscore.ui.screens.ModeSelectScreen
import com.mimo.badmintonscore.ui.theme.BadmintonScoreTheme

sealed class Screen {
    object ModeSelect : Screen()
    data class Match(
        val targetScore: Int,
        val leftName: String,
        val rightName: String,
        val matchType: MatchType = MatchType.SINGLES,
        val enableIntervalTimer: Boolean = true,
        val isServeAssistantEnabled: Boolean = false,
        val isServeDirectionReversed: Boolean = false,
        val initialMatchState: MatchState? = null
    ) : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        setContent {
            BadmintonScoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val matchPrefs = remember { MatchPreferences(context) }

                    var leftTeamName by rememberSaveable { mutableStateOf(matchPrefs.leftTeamName) }
                    var rightTeamName by rememberSaveable { mutableStateOf(matchPrefs.rightTeamName) }
                    var matchType by rememberSaveable { mutableStateOf(matchPrefs.matchType) }
                    var enableIntervalTimer by rememberSaveable { mutableStateOf(matchPrefs.enableIntervalTimer) }
                    var isServeAssistantEnabled by rememberSaveable { mutableStateOf(matchPrefs.isServeAssistantEnabled) }
                    var isServeDirectionReversed by rememberSaveable { mutableStateOf(matchPrefs.isServeDirectionReversed) }

                    var activeMatchRecord by remember {
                        mutableStateOf(ActiveMatchRepository.getActiveMatch(context))
                    }

                    // Android 13+ 通知权限申请，保障前台保活服务通知正常展示
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { _ -> }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    // 如果检测到未完成的比赛或从通知栏点击进入，直接恢复至比赛页面
                    var currentScreen by remember {
                        mutableStateOf<Screen>(
                            if (activeMatchRecord != null) {
                                val record = activeMatchRecord!!
                                Screen.Match(
                                    targetScore = record.matchState.targetScore,
                                    leftName = record.matchState.leftTeamName,
                                    rightName = record.matchState.rightTeamName,
                                    matchType = record.matchState.matchType,
                                    enableIntervalTimer = record.enableIntervalTimer,
                                    isServeAssistantEnabled = record.isServeAssistantEnabled,
                                    isServeDirectionReversed = record.isServeDirectionReversed,
                                    initialMatchState = record.matchState
                                )
                            } else {
                                Screen.ModeSelect
                            }
                        )
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is Screen.Match) {
                                (slideInVertically(
                                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                                    initialOffsetY = { -it }
                                ) + fadeIn(animationSpec = tween(420)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(220))
                                )
                            } else {
                                (fadeIn(animationSpec = tween(280)))
                                .togetherWith(
                                    slideOutVertically(
                                        animationSpec = tween(380, easing = FastOutSlowInEasing),
                                        targetOffsetY = { -it }
                                    ) + fadeOut(animationSpec = tween(220))
                                )
                            }
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            is Screen.ModeSelect -> {
                                ModeSelectScreen(
                                    initialLeftName = leftTeamName,
                                    initialRightName = rightTeamName,
                                    initialMatchType = matchType,
                                    initialEnableIntervalTimer = enableIntervalTimer,
                                    initialServeAssistantEnabled = isServeAssistantEnabled,
                                    initialServeDirectionReversed = isServeDirectionReversed,
                                    activeMatchRecord = activeMatchRecord,
                                    onResumeMatch = { record ->
                                        currentScreen = Screen.Match(
                                            targetScore = record.matchState.targetScore,
                                            leftName = record.matchState.leftTeamName,
                                            rightName = record.matchState.rightTeamName,
                                            matchType = record.matchState.matchType,
                                            enableIntervalTimer = record.enableIntervalTimer,
                                            isServeAssistantEnabled = record.isServeAssistantEnabled,
                                            isServeDirectionReversed = record.isServeDirectionReversed,
                                            initialMatchState = record.matchState
                                        )
                                    },
                                    onDiscardMatch = {
                                        ActiveMatchRepository.clearActiveMatch(context)
                                        MatchKeepAliveService.stop(context)
                                        activeMatchRecord = null
                                    },
                                    onSettingsChanged = { left, right, type, timer, assist, reversed ->
                                        leftTeamName = left
                                        rightTeamName = right
                                        matchType = type
                                        enableIntervalTimer = timer
                                        isServeAssistantEnabled = assist
                                        isServeDirectionReversed = reversed

                                        matchPrefs.leftTeamName = left
                                        matchPrefs.rightTeamName = right
                                        matchPrefs.matchType = type
                                        matchPrefs.enableIntervalTimer = timer
                                        matchPrefs.isServeAssistantEnabled = assist
                                        matchPrefs.isServeDirectionReversed = reversed
                                    },
                                    onSelectMode = { score, left, right, type, intervalTimer, assistEnabled, reversed ->
                                        leftTeamName = left
                                        rightTeamName = right
                                        matchType = type
                                        enableIntervalTimer = intervalTimer
                                        isServeAssistantEnabled = assistEnabled
                                        isServeDirectionReversed = reversed

                                        matchPrefs.leftTeamName = left
                                        matchPrefs.rightTeamName = right
                                        matchPrefs.matchType = type
                                        matchPrefs.targetScore = score
                                        matchPrefs.enableIntervalTimer = intervalTimer
                                        matchPrefs.isServeAssistantEnabled = assistEnabled
                                        matchPrefs.isServeDirectionReversed = reversed

                                        currentScreen = Screen.Match(
                                            targetScore = score,
                                            leftName = left,
                                            rightName = right,
                                            matchType = type,
                                            enableIntervalTimer = intervalTimer,
                                            isServeAssistantEnabled = assistEnabled,
                                            isServeDirectionReversed = reversed
                                        )
                                    }
                                )
                            }
                            is Screen.Match -> {
                                MatchScoreScreen(
                                    targetScore = screen.targetScore,
                                    initialLeftName = screen.leftName,
                                    initialRightName = screen.rightName,
                                    matchType = screen.matchType,
                                    enableIntervalTimer = screen.enableIntervalTimer,
                                    isServeAssistantEnabled = screen.isServeAssistantEnabled,
                                    isServeDirectionReversed = screen.isServeDirectionReversed,
                                    initialMatchState = screen.initialMatchState,
                                    onExitToHome = {
                                        activeMatchRecord = ActiveMatchRepository.getActiveMatch(context)
                                        currentScreen = Screen.ModeSelect
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
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
