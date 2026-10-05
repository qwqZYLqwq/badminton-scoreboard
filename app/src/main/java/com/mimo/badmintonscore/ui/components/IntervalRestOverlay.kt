package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.IntervalType
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.ui.theme.ServerGold
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary
import kotlinx.coroutines.delay

private val NeoBorderDark = Color(0xFF1E1B18)

@Composable
fun IntervalRestOverlay(
    matchState: MatchState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (matchState.pendingInterval == IntervalType.NONE) return

    val isSetBreak = matchState.pendingInterval == IntervalType.SET_BREAK
    val totalSeconds = if (isSetBreak) 120 else 60
    var secondsRemaining by remember(matchState.pendingInterval, matchState.currentGameIndex, matchState.leftScore, matchState.rightScore) {
        mutableIntStateOf(totalSeconds)
    }

    val haptic = LocalHapticFeedback.current

    // 每秒倒计时
    LaunchedEffect(matchState.pendingInterval, matchState.currentGameIndex) {
        secondsRemaining = totalSeconds
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onDismiss()
    }

    // 全屏绿色背景容器，双击跳过
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF047857), // 沉浸护眼地胶绿
                        Color(0xFF065F46),
                        Color(0xFF064E3B)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 球场淡绿背景网格
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color(0x18FFFFFF)
            val strokeW = 2.dp.toPx()
            // 场地外框与中心线
            drawRect(
                color = lineColor,
                topLeft = Offset(40.dp.toPx(), 24.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(size.width - 80.dp.toPx(), size.height - 48.dp.toPx()),
                style = Stroke(width = strokeW)
            )
            drawLine(
                color = lineColor,
                start = Offset(size.width / 2f, 24.dp.toPx()),
                end = Offset(size.width / 2f, size.height - 24.dp.toPx()),
                strokeWidth = strokeW
            )
        }

        // 核心内容卡片 (Neo-Pop Arcade Style)
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. 顶部标题与类型徽章
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEB3B),
                    modifier = Modifier.border(2.dp, NeoBorderDark, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeoBorderDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSetBreak) "局间休息 (${totalSeconds}s)" else "局中技术暂停 (${totalSeconds}s)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = NeoBorderDark
                        )
                    }
                }

                if (matchState.courtSwapHint != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.border(2.dp, NeoBorderDark, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = NeoBorderDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "双方交换场地",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = NeoBorderDark
                            )
                        }
                    }
                }
            }

            // 2. 超大倒计时圆盘
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // 3D 投影底块
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(NeoBorderDark, CircleShape)
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFBF7EE),
                    modifier = Modifier
                        .size(144.dp)
                        .border(3.2.dp, NeoBorderDark, CircleShape)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = if (secondsRemaining <= 10) Color(0xFFDC2626) else NeoBorderDark,
                            lineHeight = 38.sp
                        )
                        Text(
                            text = "REST TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 3. 当前大比分与比赛信息卡片
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .background(NeoBorderDark, RoundedCornerShape(14.dp))
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.5.dp, NeoBorderDark, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${matchState.leftTeamName} (${matchState.leftGamesWon})",
                            color = TeamBluePrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )

                        Text(
                            text = if (isSetBreak) "第 ${matchState.currentGameIndex} 局结束" else "第 ${matchState.currentGameIndex} 局中 (比分 ${matchState.leftScore}:${matchState.rightScore})",
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "(${matchState.rightGamesWon}) ${matchState.rightTeamName}",
                            color = TeamRedPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 4. 双击跳过提示与快速跳过按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "💡 双击屏幕任意位置跳过休息",
                    color = Color(0xFFD1FAE5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFACC15),
                        contentColor = NeoBorderDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.border(2.dp, NeoBorderDark, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "跳过",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "直接开始",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
