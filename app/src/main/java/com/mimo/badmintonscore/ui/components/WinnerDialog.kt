package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide

// Neo-Pop Arcade Palette
private val PopCreamBg = Color(0xFFFBF7EE)
private val PopDarkBorder = Color(0xFF1E1B18)
private val PopYellow = Color(0xFFFFEB3B)
private val PopBlue = Color(0xFF2563EB)
private val PopRed = Color(0xFFDC2626)
private val PopSubtitlePurple = Color(0xFF6366F1)
private val PopCardSkyBlue = Color(0xFFBAE6FD)

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
    val winnerColor = if (isLeftWinner) PopBlue else PopRed

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(540.dp)
                .wrapContentHeight()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Solid black 3D shadow block behind dialog
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(PopDarkBorder, RoundedCornerShape(26.dp))
            )

            // 2. Dialog surface with Neo-Pop styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .border(3.2.dp, PopDarkBorder, RoundedCornerShape(26.dp)),
                color = PopCreamBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy Mascot Badge with 3D shadow
                    Box(modifier = Modifier.size(60.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(PopDarkBorder, RoundedCornerShape(20.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(-2f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PopYellow)
                                .border(3.dp, PopDarkBorder, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = PopDarkBorder,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Neo-Pop Game Over Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PopSubtitlePurple,
                        modifier = Modifier
                            .border(2.dp, PopDarkBorder, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "GAME OVER · 比赛结束",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Winner Title with Party Popper and Ribbon Spray
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Confetti ribbons spraying out from the left emoji!
                            ConfettiRibbonSpray(
                                active = true,
                                isBlueWinner = isLeftWinner
                            )

                            Text(
                                text = "🎉",
                                fontSize = 32.sp
                            )
                        }

                        Text(
                            text = "$winnerName 获胜！",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = winnerColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Score Box with 3D shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = 3.5.dp, y = 3.5.dp)
                                .background(PopDarkBorder, RoundedCornerShape(16.dp))
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.8.dp, PopDarkBorder, RoundedCornerShape(16.dp)),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = matchState.leftTeamName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopBlue,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "${matchState.leftScore}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopBlue
                                )
                                Text(
                                    text = " : ",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                                Text(
                                    text = "${matchState.rightScore}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopRed
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = matchState.rightTeamName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopRed,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons (All 3 buttons have equal 1f weight, generous 150dp+ width, no text truncation!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoPopWinnerButton(
                            text = "主菜单",
                            icon = Icons.Default.Home,
                            backgroundColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = onBackToHome
                        )

                        NeoPopWinnerButton(
                            text = "换边再战",
                            icon = Icons.Default.SwapHoriz,
                            backgroundColor = PopCardSkyBlue,
                            modifier = Modifier.weight(1f),
                            onClick = onSwapAndRestart
                        )

                        NeoPopWinnerButton(
                            text = "再来一局",
                            icon = Icons.Default.Refresh,
                            backgroundColor = PopYellow,
                            modifier = Modifier.weight(1f),
                            onClick = onRestart
                        )
                    }
                }
            }
        }
    }
}

/**
 * Neo-Pop tactile physical push button with 3D drop shadow and bounce animation.
 */
@Composable
private fun NeoPopWinnerButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color = PopDarkBorder,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pressOffset = remember { Animatable(0f) }
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.height(48.dp)
    ) {
        // 3D Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(PopDarkBorder, RoundedCornerShape(14.dp))
        )

        // Physical push-button face
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = pressOffset.value.dp, y = pressOffset.value.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(2.6.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                .clickable(enabled = !isPressed) {
                    if (isPressed) return@clickable
                    isPressed = true
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        pressOffset.animateTo(2.5f, tween(durationMillis = 60))
                        pressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        delay(40L)
                        onClick()
                        isPressed = false
                    }
                },
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = text,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * One-Shot Explosive Party Popper Confetti & Ribbon Blast:
 * Emulates a real party popper bursting once out of the 🎉 emoji cone mouth!
 * High initial muzzle velocity -> air resistance deceleration -> gentle gravity drift.
 */
@Composable
fun ConfettiRibbonSpray(
    active: Boolean,
    isBlueWinner: Boolean,
    modifier: Modifier = Modifier
) {
    if (!active) return

    val blastProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        blastProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2100, easing = LinearEasing)
        )
    }

    val t = blastProgress.value
    if (t >= 1f) return // Only plays ONCE then completes cleanly!

    // 36 One-shot explosive confetti ribbons and flakes
    val particles = remember(isBlueWinner) {
        val colorPalette = if (isBlueWinner) {
            listOf(
                Color(0xFF00E5FF),
                Color(0xFF3B82F6),
                Color(0xFF60A5FA),
                Color(0xFFFFD700),
                Color(0xFFFF5722),
                Color(0xFFFFFFFF),
                Color(0xFFA855F7),
                Color(0xFF22C55E)
            )
        } else {
            listOf(
                Color(0xFFFF3366),
                Color(0xFFFFA000),
                Color(0xFFFFD700),
                Color(0xFFFF5722),
                Color(0xFFFFFFFF),
                Color(0xFF38BDF8),
                Color(0xFFA855F7)
            )
        }

        List(36) { index ->
            // Spray cone pointing up & to the right (-88° to -18°) matching the 🎉 cone mouth orientation
            val angle = -88f + (index % 18) * 4.0f + (index / 18) * 3f
            val angleRad = angle * (PI / 180.0)
            val burstSpeed = 480f + (index % 7) * 95f
            val color = colorPalette[index % colorPalette.size]
            val waveFreq = 4.5f + (index % 4) * 2f
            val ribbonLength = 36f + (index % 5) * 12f
            val isRibbon = (index % 3) != 0 // 2/3 are curly ribbon streamers, 1/3 are fluttering flakes

            PopperParticle(
                vx0 = (cos(angleRad) * burstSpeed).toFloat(),
                vy0 = (sin(angleRad) * burstSpeed).toFloat(),
                color = color,
                waveFreq = waveFreq,
                length = ribbonLength,
                isRibbon = isRibbon,
                curlPhase = (index * 0.7f)
            )
        }
    }

    Canvas(
        modifier = modifier
            .size(1.dp)
            .wrapContentSize(align = Alignment.Center, unbounded = true)
    ) {
        val k = 1.7f // Air drag deceleration constant
        val gravity = 480f // Gravity constant

        particles.forEach { p ->
            // Physics: instant explosive spray with air resistance:
            // x(t) = (vx0 / k) * (1 - e^(-k * t))
            // y(t) = (vy0 / k) * (1 - e^(-k * t)) + 0.5 * gravity * t^2
            val dragFactor = ((1f - exp(-k * t * 1.8f)) / k)
            val x = p.vx0 * dragFactor
            val y = p.vy0 * dragFactor + 0.5f * gravity * t * t

            // Instant full visibility, gentle fade out during last 30% of lifetime
            val alpha = when {
                t < 0.05f -> t / 0.05f
                t > 0.70f -> ((1f - t) / 0.30f).coerceIn(0f, 1f)
                else -> 1f
            }

            if (p.isRibbon) {
                // Curled ribbon streamer unfurling out of the popper
                val path = Path()
                val waveOffset = sin((t * p.waveFreq + p.curlPhase) * 2 * PI).toFloat() * (12f * (1f - t * 0.4f))
                val ribbonWidth = (5.5f * abs(cos((t * p.waveFreq) * PI).toFloat())).coerceAtLeast(1.2f)

                path.moveTo(x - waveOffset, y)
                path.quadraticBezierTo(
                    x + waveOffset * 0.5f, y - p.length * 0.45f,
                    x + waveOffset, y - p.length
                )

                drawPath(
                    path = path,
                    color = p.color.copy(alpha = alpha),
                    style = Stroke(width = ribbonWidth, cap = StrokeCap.Round)
                )
            } else {
                // Fluttering confetti paper flake / star
                val rotation = t * 900f + p.curlPhase * 60f
                val flakeWidth = (8f * abs(cos(t * p.waveFreq * PI).toFloat())).coerceAtLeast(2f)

                rotate(degrees = rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - flakeWidth, y - 4f),
                        size = androidx.compose.ui.geometry.Size(flakeWidth * 2f, 8f)
                    )
                }
            }
        }
    }
}

private data class PopperParticle(
    val vx0: Float,
    val vy0: Float,
    val color: Color,
    val waveFreq: Float,
    val length: Float,
    val isRibbon: Boolean,
    val curlPhase: Float
)