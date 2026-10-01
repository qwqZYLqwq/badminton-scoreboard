package com.mimo.badmintonscore.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.components.TopControlBar
import com.mimo.badmintonscore.ui.components.WinnerDialog
import com.mimo.badmintonscore.ui.theme.ServerGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MatchScoreScreen(
    targetScore: Int,
    initialLeftName: String,
    initialRightName: String,
    onExitToHome: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Request landscape orientation for match screen
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var matchState by remember {
        mutableStateOf(
            MatchState(
                targetScore = targetScore,
                leftTeamName = initialLeftName,
                rightTeamName = initialRightName
            )
        )
    }

    // Top control bar visibility & auto-hide timer
    var isTopBarVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun refreshInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        isTopBarVisible = true
    }

    // Auto-hide after 3.5 seconds of inactivity
    LaunchedEffect(lastInteractionTime, isTopBarVisible) {
        if (isTopBarVisible) {
            delay(3500L)
            isTopBarVisible = false
        }
    }

    // Score bounce animation: Every click shrinks then enlarges/restores
    val leftScale = remember { Animatable(1f) }
    val rightScale = remember { Animatable(1f) }

    fun onLeftClick() {
        if (matchState.isGameOver) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        matchState = matchState.addPoint(TeamSide.LEFT)
        refreshInteraction()

        scope.launch {
            leftScale.snapTo(1f)
            leftScale.animateTo(0.85f, animationSpec = tween(durationMillis = 60))
            leftScale.animateTo(1.16f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            leftScale.animateTo(1.0f, animationSpec = tween(durationMillis = 80))
        }
    }

    fun onRightClick() {
        if (matchState.isGameOver) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        matchState = matchState.addPoint(TeamSide.RIGHT)
        refreshInteraction()

        scope.launch {
            rightScale.snapTo(1f)
            rightScale.animateTo(0.85f, animationSpec = tween(durationMillis = 60))
            rightScale.animateTo(1.16f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            rightScale.animateTo(1.0f, animationSpec = tween(durationMillis = 80))
        }
    }

    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Detect swipe down anywhere to reveal top control bar
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    if (dragAmount.y > 15f) {
                        isTopBarVisible = true
                        lastInteractionTime = System.currentTimeMillis()
                    }
                }
            }
    ) {
        // Split screen: Left half vs Right half
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT SIDE: BLUE TEAM
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color.White)
                    ) {
                        onLeftClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 1. Team Name Badge
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = matchState.leftTeamName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Giant Score Number with shrink-then-expand bounce
                    Text(
                        text = matchState.leftScore.toString(),
                        fontSize = 130.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.scale(leftScale.value),
                        lineHeight = 130.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. 点击加分 / 加分赛提示
                    Text(
                        text = if (matchState.isDeuce) "加分赛 (封顶${matchState.capScore}分)" else "点击加分",
                        fontSize = 13.sp,
                        color = if (matchState.isDeuce) ServerGold else Color(0x99FFFFFF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Serving court indicator below "点击加分" (左单右双，0算双数)
                    ServingIndicator(
                        isServing = matchState.servingSide == TeamSide.LEFT && !matchState.isGameOver,
                        serverScore = matchState.leftScore,
                        isRightCourt = matchState.isServingFromRightCourt,
                        onToggleServer = {
                            refreshInteraction()
                            matchState = matchState.toggleServer()
                        }
                    )
                }
            }

            // NET / COURT DIVIDER
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0x88FFFFFF))
            )

            // RIGHT SIDE: RED TEAM
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color.White)
                    ) {
                        onRightClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 1. Team Name Badge
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = matchState.rightTeamName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Giant Score Number with shrink-then-expand bounce
                    Text(
                        text = matchState.rightScore.toString(),
                        fontSize = 130.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.scale(rightScale.value),
                        lineHeight = 130.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. 点击加分 / 加分赛提示
                    Text(
                        text = if (matchState.isDeuce) "加分赛 (封顶${matchState.capScore}分)" else "点击加分",
                        fontSize = 13.sp,
                        color = if (matchState.isDeuce) ServerGold else Color(0x99FFFFFF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Serving court indicator below "点击加分" (左单右双，0算双数)
                    ServingIndicator(
                        isServing = matchState.servingSide == TeamSide.RIGHT && !matchState.isGameOver,
                        serverScore = matchState.rightScore,
                        isRightCourt = matchState.isServingFromRightCourt,
                        onToggleServer = {
                            refreshInteraction()
                            matchState = matchState.toggleServer()
                        }
                    )
                }
            }
        }

        // TOP FLOATING CONTROL BAR (Auto-hiding, swipe down to expand)
        TopControlBar(
            isVisible = isTopBarVisible,
            matchState = matchState,
            onAddScore = { side ->
                if (side == TeamSide.LEFT) {
                    onLeftClick()
                } else {
                    onRightClick()
                }
            },
            onMinusScore = { side ->
                refreshInteraction()
                matchState = matchState.minusPoint(side)
            },
            onToggleServer = {
                refreshInteraction()
                matchState = matchState.toggleServer()
            },
            onSwapSides = {
                refreshInteraction()
                matchState = matchState.swapSides()
            },
            onUndo = {
                refreshInteraction()
                matchState = matchState.undo()
            },
            onReset = {
                refreshInteraction()
                matchState = matchState.reset()
            },
            onExit = {
                onExitToHome()
            },
            onExpandBar = {
                refreshInteraction()
            }
        )

        // WINNER POPUP DIALOG
        WinnerDialog(
            matchState = matchState,
            onRestart = {
                matchState = matchState.reset()
            },
            onSwapAndRestart = {
                matchState = matchState.swapSides().reset()
            },
            onBackToHome = {
                onExitToHome()
            }
        )
    }
}

/**
 * 羽毛球发球区指示器：
 * 按照“左单右双”原则（0分算双数）：
 * - 发球方得分为偶数（0, 2, 4...）时，在【右半场/右区】发球
 * - 发球方得分为奇数（1, 3, 5...）时，在【左半场/左区】发球
 */
@Composable
fun ServingIndicator(
    isServing: Boolean,
    serverScore: Int,
    isRightCourt: Boolean,
    onToggleServer: () -> Unit
) {
    if (isServing) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xDD0F172A),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleServer() }
                .border(1.dp, ServerGold.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shuttlecock icon + Court name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = "发球",
                        tint = ServerGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRightCourt) "右半场发球" else "左半场发球",
                        color = ServerGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mini Court diagram [左区 | 右区] highlighting the active box
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
                ) {
                    // Left Court box (highlighted if odd)
                    Box(
                        modifier = Modifier
                            .background(if (!isRightCourt) ServerGold else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "左区",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isRightCourt) Color(0xFF0F172A) else Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0xFF475569))
                    )

                    // Right Court box (highlighted if even, including 0)
                    Box(
                        modifier = Modifier
                            .background(if (isRightCourt) ServerGold else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "右区",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRightCourt) Color(0xFF0F172A) else Color(0xFF64748B)
                        )
                    }
                }

                // Rule explanation pill: e.g. "双数0分" or "单数1分"
                Text(
                    text = if (isRightCourt) "(双数${serverScore}分)" else "(单数${serverScore}分)",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        // Non-serving side: subtle placeholder so layout height stays stable
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0x22000000),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleServer() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "接发球方",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}
