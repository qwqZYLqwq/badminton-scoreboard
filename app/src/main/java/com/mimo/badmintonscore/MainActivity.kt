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
        val totalGames: Int = 3,
        val enableIntervalTimer: Boolean = true,
        val isServeAssistantEnabled: Boolean = false,
        val isServeDirectionReversed: Boolean = false,
        val isHapticEnabled: Boolean = true,
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
                    var totalGames by rememberSaveable { mutableStateOf(matchPrefs.totalGames) }
                    var enableIntervalTimer by rememberSaveable { mutableStateOf(matchPrefs.enableIntervalTimer) }
                    var isServeAssistantEnabled by rememberSaveable { mutableStateOf(matchPrefs.isServeAssistantEnabled) }
                    var isServeDirectionReversed by rememberSaveable { mutableStateOf(matchPrefs.isServeDirectionReversed) }
                    var isHapticEnabled by rememberSaveable { mutableStateOf(matchPrefs.isHapticEnabled) }

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

                    // 启动行为：手动杀后台后重新打开软件，始终停留在首页（ModeSelect），并在主页下方提示上方展示恢复提示
                    // 仅当用户明确从通知栏点击前台常驻服务进入时，才直接跳转到比赛页面
                    var currentScreen by remember {
                        mutableStateOf<Screen>(
                            if (intent.getBooleanExtra(MatchKeepAliveService.EXTRA_FROM_NOTIFICATION, false) && activeMatchRecord != null) {
                                val record = activeMatchRecord!!
                                Screen.Match(
                                    targetScore = record.matchState.targetScore,
                                    leftName = record.matchState.leftTeamName,
                                    rightName = record.matchState.rightTeamName,
                                    matchType = record.matchState.matchType,
                                    totalGames = record.matchState.totalGames,
                                    enableIntervalTimer = record.enableIntervalTimer,
                                    isServeAssistantEnabled = record.isServeAssistantEnabled,
                                    isServeDirectionReversed = record.isServeDirectionReversed,
                                    isHapticEnabled = record.isHapticEnabled,
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
                                    initialTotalGames = totalGames,
                                    initialEnableIntervalTimer = enableIntervalTimer,
                                    initialServeAssistantEnabled = isServeAssistantEnabled,
                                    initialServeDirectionReversed = isServeDirectionReversed,
                                    initialHapticEnabled = isHapticEnabled,
                                    activeMatchRecord = activeMatchRecord,
                                    onResumeMatch = { record ->
                                        currentScreen = Screen.Match(
                                            targetScore = record.matchState.targetScore,
                                            leftName = record.matchState.leftTeamName,
                                            rightName = record.matchState.rightTeamName,
                                            matchType = record.matchState.matchType,
                                            totalGames = record.matchState.totalGames,
                                            enableIntervalTimer = record.enableIntervalTimer,
                                            isServeAssistantEnabled = record.isServeAssistantEnabled,
                                            isServeDirectionReversed = record.isServeDirectionReversed,
                                            isHapticEnabled = record.isHapticEnabled,
                                            initialMatchState = record.matchState
                                        )
                                    },
                                    onDiscardMatch = {
                                        ActiveMatchRepository.clearActiveMatch(context)
                                        MatchKeepAliveService.stop(context)
                                        activeMatchRecord = null
                                    },
                                    onSettingsChanged = { left, right, type, games, timer, assist, reversed, haptic ->
                                        leftTeamName = left
                                        rightTeamName = right
                                        matchType = type
                                        totalGames = games
                                        enableIntervalTimer = timer
                                        isServeAssistantEnabled = assist
                                        isServeDirectionReversed = reversed
                                        isHapticEnabled = haptic

                                        matchPrefs.leftTeamName = left
                                        matchPrefs.rightTeamName = right
                                        matchPrefs.matchType = type
                                        matchPrefs.totalGames = games
                                        matchPrefs.enableIntervalTimer = timer
                                        matchPrefs.isServeAssistantEnabled = assist
                                        matchPrefs.isServeDirectionReversed = reversed
                                        matchPrefs.isHapticEnabled = haptic
                                    },
                                    onSelectMode = { score, left, right, type, games, intervalTimer, assistEnabled, reversed, haptic ->
                                        leftTeamName = left
                                        rightTeamName = right
                                        matchType = type
                                        totalGames = games
                                        enableIntervalTimer = intervalTimer
                                        isServeAssistantEnabled = assistEnabled
                                        isServeDirectionReversed = reversed
                                        isHapticEnabled = haptic

                                        matchPrefs.leftTeamName = left
                                        matchPrefs.rightTeamName = right
                                        matchPrefs.matchType = type
                                        matchPrefs.totalGames = games
                                        matchPrefs.targetScore = score
                                        matchPrefs.enableIntervalTimer = intervalTimer
                                        matchPrefs.isServeAssistantEnabled = assistEnabled
                                        matchPrefs.isServeDirectionReversed = reversed
                                        matchPrefs.isHapticEnabled = haptic

                                        currentScreen = Screen.Match(
                                            targetScore = score,
                                            leftName = left,
                                            rightName = right,
                                            matchType = type,
                                            totalGames = games,
                                            enableIntervalTimer = intervalTimer,
                                            isServeAssistantEnabled = assistEnabled,
                                            isServeDirectionReversed = reversed,
                                            isHapticEnabled = haptic
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
                                    totalGames = screen.totalGames,
                                    enableIntervalTimer = screen.enableIntervalTimer,
                                    isServeAssistantEnabled = screen.isServeAssistantEnabled,
                                    isServeDirectionReversed = screen.isServeDirectionReversed,
                                    isHapticEnabled = screen.isHapticEnabled,
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
