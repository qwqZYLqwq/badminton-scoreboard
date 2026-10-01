$root = $PSScriptRoot
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# 1. Color.kt
$colorKt = @"
package com.mimo.badmintonscore.ui.theme

import androidx.compose.ui.graphics.Color

val TeamBluePrimary = Color(0xFF1976D2)
val TeamBlueDark = Color(0xFF0D47A1)
val TeamBlueLight = Color(0xFF64B5F6)
val TeamBlueAccent = Color(0xFF00E5FF)

val TeamRedPrimary = Color(0xFFE53935)
val TeamRedDark = Color(0xFFB71C1C)
val TeamRedLight = Color(0xFFEF5350)
val TeamRedAccent = Color(0xFFFFD600)

val CourtBackground = Color(0xFF0F172A)
val SurfaceDark = Color(0xDD1E293B)
val TextLight = Color(0xFFF8FAFC)
val TextDim = Color(0xFF94A3B8)
val NetColor = Color(0x66FFFFFF)
"@
[System.IO.File]::WriteAllText((Join-Path $root "app\src\main\java\com\mimo\badmintonscore\ui\theme\Color.kt"), $colorKt, $utf8NoBom)

# 2. Theme.kt
$themeKt = @"
package com.mimo.badmintonscore.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFFF43F5E),
    tertiary = Color(0xFFFACC15),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BadmintonScoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
"@
[System.IO.File]::WriteAllText((Join-Path $root "app\src\main\java\com\mimo\badmintonscore\ui\theme\Theme.kt"), $themeKt, $utf8NoBom)

# 3. WinnerDialog.kt
$winnerKt = @"
package com.mimo.badmintonscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary

@Composable
fun WinnerDialog(
    matchState: MatchState,
    onRestart: () -> Unit,
    onSwapAndRestart: () -> Unit,
    onBackToHome: () -> Unit
) {
    if (matchState.winner == null) return

    val isLeftWinner = matchState.winner == TeamSide.LEFT
    val winnerName = if (isLeftWinner) matchState.leftTeamName else matchState.rightTeamName
    val winnerColor = if (isLeftWinner) TeamBluePrimary else TeamRedPrimary

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "\u6BD4\u8D5B\u7ED3\u675F\uFF01",
                    fontSize = 18.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "\uD83C\uDF89 " + winnerName + " \u83B7\u80DC\uFF01",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = winnerColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "`$matchState.leftScore",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeamBluePrimary
                        )
                        Text(
                            text = "  :  ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "`$matchState.rightScore",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeamRedPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFCBD5E1)
                        )
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u4E3B\u83DC\u5355", fontSize = 14.sp)
                    }

                    FilledTonalButton(
                        onClick = onSwapAndRestart,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u6362\u8FB9\u518D\u6218", fontSize = 14.sp)
                    }

                    Button(
                        onClick = onRestart,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u518D\u6765\u4E00\u5C40", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
"@
# Replace the `$matchState` placeholder
$winnerKt = $winnerKt.Replace("`$matchState.leftScore", "${matchState.leftScore}").Replace("`$matchState.rightScore", "${matchState.rightScore}")
[System.IO.File]::WriteAllText((Join-Path $root "app\src\main\java\com\mimo\badmintonscore\ui\components\WinnerDialog.kt"), $winnerKt, $utf8NoBom)

# 4. MainActivity.kt
$mainKt = @"
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
"@
[System.IO.File]::WriteAllText((Join-Path $root "app\src\main\java\com\mimo\badmintonscore\MainActivity.kt"), $mainKt, $utf8NoBom)

Write-Host "Sources restored successfully!"
