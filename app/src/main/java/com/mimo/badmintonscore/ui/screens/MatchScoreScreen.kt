package com.mimo.badmintonscore.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.mimo.badmintonscore.model.IntervalType
import com.mimo.badmintonscore.model.MatchType
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.components.ActiveShuttlecock
import com.mimo.badmintonscore.ui.components.ImpactEffect
import com.mimo.badmintonscore.ui.components.IntervalRestOverlay
import com.mimo.badmintonscore.ui.components.ServeAssistOverlay
import com.mimo.badmintonscore.ui.components.SmashAnimationOverlay
import kotlin.random.Random
import com.mimo.badmintonscore.ui.components.TopControlBar
import com.mimo.badmintonscore.ui.components.WinnerDialog
import com.mimo.badmintonscore.ui.theme.ServerGold
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    matchType: MatchType = MatchType.SINGLES,
    enableIntervalTimer: Boolean = true,
    isServeAssistantEnabled: Boolean = false,
    isServeDirectionReversed: Boolean = false,
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
                rightTeamName = initialRightName,
                matchType = matchType
            )
        )
    }

    // Top control bar visibility & auto-hide timer (starts collapsed as top gray rectangle)
    var isTopBarVisible by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun refreshInteraction() {
        lastInteractionTime = System.currentTimeMillis()
    }

    // Auto-hide after 3.5 seconds of inactivity
    LaunchedEffect(lastInteractionTime, isTopBarVisible) {
        if (isTopBarVisible) {
            delay(3500L)
            isTopBarVisible = false
        }
    }

    // Smash animation states & screen micro-shake (supports concurrent multiple shuttlecocks on rapid taps!)
    val activeShuttlecocks = remember { mutableStateListOf<ActiveShuttlecock>() }
    val activeImpacts = remember { mutableStateListOf<ImpactEffect>() }
    var shotCounter by remember { mutableLongStateOf(0L) }
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    suspend fun triggerScreenShake() {
        coroutineScope {
            launch {
                shakeOffsetX.animateTo(8f, tween(25))
                shakeOffsetX.animateTo(-7f, tween(25))
                shakeOffsetX.animateTo(5f, tween(25))
                shakeOffsetX.animateTo(-3f, tween(25))
                shakeOffsetX.animateTo(1.5f, tween(25))
                shakeOffsetX.animateTo(0f, tween(25))
            }
            launch {
                shakeOffsetY.animateTo(-6f, tween(25))
                shakeOffsetY.animateTo(6f, tween(25))
                shakeOffsetY.animateTo(-4f, tween(25))
                shakeOffsetY.animateTo(3f, tween(25))
                shakeOffsetY.animateTo(-1f, tween(25))
                shakeOffsetY.animateTo(0f, tween(25))
            }
        }
    }

    // Score bounce animation: Every click shrinks then enlarges/restores
    val leftScale = remember { Animatable(1f) }
    val rightScale = remember { Animatable(1f) }

    // Court swap notification banner text
    var activeSwapBanner by remember { mutableStateOf<String?>(null) }

    // In-flight pending points count (to prevent fast clicks from overshooting intervals or set wins)
    var inFlightLeftPoints by remember { mutableIntStateOf(0) }
    var inFlightRightPoints by remember { mutableIntStateOf(0) }

    // Clear in-flight states and projectiles whenever interval starts; if timer disabled, proceed immediately
    LaunchedEffect(matchState.pendingInterval) {
        if (matchState.pendingInterval != IntervalType.NONE) {
            activeShuttlecocks.clear()
            activeImpacts.clear()
            inFlightLeftPoints = 0
            inFlightRightPoints = 0

            if (!enableIntervalTimer) {
                val hint = if (matchState.pendingInterval == IntervalType.SET_BREAK) {
                    "双方交换场地，第 ${matchState.currentGameIndex + 1} 局开始！"
                } else if (matchState.isDecidingGame) {
                    "决胜局达到 ${matchState.intervalScore} 分，双方交换场地！"
                } else null

                matchState = matchState.proceedToNextGameOrSwap()
                if (hint != null) {
                    activeSwapBanner = hint
                }
            }
        }
    }

    // Auto-dismiss swap banner after 3.2 seconds
    LaunchedEffect(activeSwapBanner) {
        if (activeSwapBanner != null) {
            delay(3200L)
            activeSwapBanner = null
        }
    }

    // Main layout with screen shake container (control bar ONLY expands by clicking the top gray rectangle)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(shakeOffsetX.value.roundToInt(), shakeOffsetY.value.roundToInt()) }
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        fun canLaunchShot(): Boolean {
            if (matchState.isGameOver || matchState.currentSetWinner != null || matchState.pendingInterval != IntervalType.NONE) {
                return false
            }

            val currentProjectedLeft = matchState.leftScore + inFlightLeftPoints
            val currentProjectedRight = matchState.rightScore + inFlightRightPoints

            // 1. 如果已有飞行中的球已经达到本局获胜条件，禁止再发球（防止快速点击导致超过目标分甚至跳至下一局胜利）
            if (matchState.checkSetWinner(currentProjectedLeft, currentProjectedRight) ||
                matchState.checkSetWinner(currentProjectedRight, currentProjectedLeft)) {
                return false
            }

            // 2. 如果已有飞行中的球将触发本局技术暂停（如11分或8分），暂停前不得再发球
            if (!matchState.hasTriggeredIntervalInCurrentGame) {
                if (currentProjectedLeft >= matchState.intervalScore || currentProjectedRight >= matchState.intervalScore) {
                    return false
                }
            }

            return true
        }

        fun triggerSmashScore(scorerSide: TeamSide) {
            if (!canLaunchShot()) return

            // 记录该方有一颗待落地的计分球
            if (scorerSide == TeamSide.LEFT) {
                inFlightLeftPoints++
            } else {
                inFlightRightPoints++
            }

            // Tactile feedback on launch
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

            shotCounter++
            val shotId = System.currentTimeMillis() * 1000 + (shotCounter % 1000)
            val shotGameIndex = matchState.currentGameIndex
            val shotBeforeInterval = !matchState.hasTriggeredIntervalInCurrentGame
            val isLeft = scorerSide == TeamSide.LEFT
            val startX = if (isLeft) widthPx * 0.28f else widthPx * 0.72f
            val endX = if (isLeft) widthPx * 0.72f else widthPx * 0.28f
            // Slight trajectory variation for rapid taps so multiple concurrent shuttlecocks don't overlap completely
            val randomYOffset = (Random.nextFloat() - 0.5f) * heightPx * 0.10f
            val startY = heightPx * 0.48f + randomYOffset
            val endY = heightPx * 0.48f + (Random.nextFloat() - 0.5f) * heightPx * 0.10f
            val arcHeight = heightPx * (0.26f + Random.nextFloat() * 0.05f)

            // 1. Launch new shuttlecock projectile across court (concurrent with any already flying)
            val newShuttlecock = ActiveShuttlecock(
                id = shotId,
                scorerSide = scorerSide,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                arcPeakHeight = arcHeight
            )
            activeShuttlecocks.add(newShuttlecock)

            scope.launch {
                // Flight takes 360ms - 动画先播放，播放完毕后再计分
                delay(360L)
                activeShuttlecocks.removeAll { it.id == shotId }

                // 飞行落地扣减在途计数
                if (scorerSide == TeamSide.LEFT) {
                    inFlightLeftPoints = maxOf(0, inFlightLeftPoints - 1)
                } else {
                    inFlightRightPoints = maxOf(0, inFlightRightPoints - 1)
                }

                // 核心安全校验：若局次已改变、已在休息中、当局已有胜者、或该球发射于技术暂停前但暂停已被触发，坚决不加分！
                if (matchState.currentGameIndex != shotGameIndex ||
                    matchState.isGameOver ||
                    matchState.currentSetWinner != null ||
                    matchState.pendingInterval != IntervalType.NONE ||
                    (shotBeforeInterval && matchState.hasTriggeredIntervalInCurrentGame)) {
                    return@launch
                }

                // 2. Smash hits opponent side! (+1 badge appears on scorer/winner side)
                val newImpact = ImpactEffect(
                    id = shotId,
                    hitX = endX,
                    hitY = endY,
                    scorerX = startX,
                    scorerY = startY - heightPx * 0.12f,
                    scorerSide = scorerSide
                )
                activeImpacts.add(newImpact)

                // Strong tactile impact
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // 3. 动画落地后再真正增加比分
                matchState = matchState.addPoint(scorerSide)

                // 4. Trigger lively score scale bounce on scorer's side
                launch {
                    val scale = if (isLeft) leftScale else rightScale
                    scale.snapTo(1f)
                    scale.animateTo(0.85f, animationSpec = tween(durationMillis = 50))
                    scale.animateTo(1.22f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    scale.animateTo(1.0f, animationSpec = tween(durationMillis = 80))
                }

                // 5. Trigger lively screen micro-shake
                launch {
                    triggerScreenShake()
                }

                // Auto clear this impact after 450ms
                delay(450L)
                activeImpacts.removeAll { it.id == shotId }
            }
        }

        fun onLeftClick() {
            triggerSmashScore(TeamSide.LEFT)
        }

        fun onRightClick() {
            triggerSmashScore(TeamSide.RIGHT)
        }
        val blueGradient = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
        val redGradient = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
        val leftCourtColors = if (!matchState.isSidesSwapped) blueGradient else redGradient
        val rightCourtColors = if (!matchState.isSidesSwapped) redGradient else blueGradient

        // Split screen: Left half vs Right half (colors swap when teams swap sides)
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT SIDE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(colors = leftCourtColors)
                    )
                ) {
                    // Clickable area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                                playerName = if (matchState.matchType == MatchType.DOUBLES && matchState.servingSide == TeamSide.LEFT) matchState.currentServerPlayer else null,
                                receiverName = if (matchState.matchType == MatchType.DOUBLES && matchState.servingSide == TeamSide.RIGHT) matchState.currentReceiverPlayer else null,
                                onToggleServer = {
                                    refreshInteraction()
                                    matchState = matchState.toggleServer()
                                }
                            )
                        }
                    }
                }

            // NET / COURT DIVIDER
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0x88FFFFFF))
            )

            // RIGHT SIDE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(colors = rightCourtColors)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                        playerName = if (matchState.matchType == MatchType.DOUBLES && matchState.servingSide == TeamSide.RIGHT) matchState.currentServerPlayer else null,
                        receiverName = if (matchState.matchType == MatchType.DOUBLES && matchState.servingSide == TeamSide.LEFT) matchState.currentReceiverPlayer else null,
                        onToggleServer = {
                            refreshInteraction()
                            matchState = matchState.toggleServer()
                        }
                    )
                }
            }
        }
    }

        // SERVE ASSIST OVERLAY (Horizontal dividing line, dashed diagonal arrow & quadrants)
        ServeAssistOverlay(
            matchState = matchState,
            isServeAssistEnabled = isServeAssistantEnabled,
            isDirectionReversed = isServeDirectionReversed
        )

        // SMASH SHUTTLECOCK & IMPACT ANIMATION OVERLAY
        SmashAnimationOverlay(
            activeShuttlecocks = activeShuttlecocks,
            activeImpacts = activeImpacts
        )

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
                activeShuttlecocks.clear()
                activeImpacts.clear()
                inFlightLeftPoints = 0
                inFlightRightPoints = 0
                matchState = matchState.swapSides()
                activeSwapBanner = "双方已交换场地"
            },
            onUndo = {
                refreshInteraction()
                activeShuttlecocks.clear()
                activeImpacts.clear()
                inFlightLeftPoints = 0
                inFlightRightPoints = 0
                matchState = matchState.undo()
            },
            onReset = {
                refreshInteraction()
                activeShuttlecocks.clear()
                activeImpacts.clear()
                inFlightLeftPoints = 0
                inFlightRightPoints = 0
                matchState = matchState.reset()
            },
            onExit = {
                activeShuttlecocks.clear()
                activeImpacts.clear()
                inFlightLeftPoints = 0
                inFlightRightPoints = 0
                onExitToHome()
            },
            onExpandBar = {
                isTopBarVisible = true
                lastInteractionTime = System.currentTimeMillis()
            },
            onCollapseBar = {
                isTopBarVisible = false
            }
        )

        // COURT SWAP NOTIFICATION BANNER
        AnimatedVisibility(
            visible = activeSwapBanner != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = activeSwapBanner ?: "",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // FULLSCREEN EYE-CARE REST OVERLAY (TECHNICAL INTERVAL & SET BREAKS)
        if (enableIntervalTimer && matchState.pendingInterval != IntervalType.NONE) {
            IntervalRestOverlay(
                matchState = matchState,
                onDismiss = {
                    val hint = if (matchState.pendingInterval == IntervalType.SET_BREAK) {
                        "双方交换场地，第 ${matchState.currentGameIndex + 1} 局开始！"
                    } else if (matchState.isDecidingGame) {
                        "决胜局达到 ${matchState.intervalScore} 分，双方交换场地！"
                    } else null

                    activeShuttlecocks.clear()
                    activeImpacts.clear()
                    inFlightLeftPoints = 0
                    inFlightRightPoints = 0
                    matchState = matchState.dismissInterval()
                    if (hint != null) {
                        activeSwapBanner = hint
                    }
                }
            )
        }

        // WINNER POPUP DIALOG (Slight delay on match point so final winning smash & score update finishes cleanly)
        var showWinnerDialog by remember { mutableStateOf(false) }

        LaunchedEffect(matchState.isGameOver) {
            if (matchState.isGameOver) {
                delay(550L)
                showWinnerDialog = true
            } else {
                showWinnerDialog = false
            }
        }

        if (showWinnerDialog && matchState.isGameOver) {
            WinnerDialog(
                matchState = matchState,
                onRestart = {
                    showWinnerDialog = false
                    activeShuttlecocks.clear()
                    activeImpacts.clear()
                    inFlightLeftPoints = 0
                    inFlightRightPoints = 0
                    matchState = matchState.reset()
                },
                onSwapAndRestart = {
                    showWinnerDialog = false
                    activeShuttlecocks.clear()
                    activeImpacts.clear()
                    inFlightLeftPoints = 0
                    inFlightRightPoints = 0
                    matchState = matchState.swapSides().reset()
                },
                onBackToHome = {
                    showWinnerDialog = false
                    activeShuttlecocks.clear()
                    activeImpacts.clear()
                    inFlightLeftPoints = 0
                    inFlightRightPoints = 0
                    onExitToHome()
                }
            )
        }
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
    playerName: String? = null,
    receiverName: String? = null,
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
                // Shuttlecock icon + Court name (plus player name if doubles)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = "发球",
                        tint = ServerGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (playerName != null) "$playerName · " + (if (isRightCourt) "右半场发球" else "左半场发球") else if (isRightCourt) "右半场发球" else "左半场发球",
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
                    text = if (receiverName != null) "接发球方 · $receiverName" else "接发球方",
                    color = Color(0x66FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}
