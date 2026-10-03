package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.MatchType
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.theme.ServerGold
import kotlin.math.*

/**
 * 发球辅助覆盖层：
 * 1. 在计分板每个半场增加横线，将半场分为左、右发球区。
 * 2. 根据左单右双与当前发球方，绘制一条流动的发球站位与对角发球方向虚线。
 * 3. 支持方向反装（将上下半区颠倒）。
 */
@Composable
fun ServeAssistOverlay(
    matchState: MatchState,
    isServeAssistEnabled: Boolean,
    isDirectionReversed: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isServeAssistEnabled || matchState.isGameOver) return

    val isLeftServer = matchState.servingSide == TeamSide.LEFT
    val isServingFromRightCourt = matchState.isServingFromRightCourt // 偶数在右发球区

    // 虚线流动动画
    val infiniteTransition = rememberInfiniteTransition(label = "ServeDash")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DashPhase"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val midX = w / 2f

            // 1. 绘制球场水平分割中线（每边一条，将半场分为上下两个发球区）
            val courtLineColor = Color(0x38FFFFFF)
            val courtLineStroke = 2.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)

            // 左场水平线
            drawLine(
                color = courtLineColor,
                start = Offset(24.dp.toPx(), midY),
                end = Offset(midX - 12.dp.toPx(), midY),
                strokeWidth = courtLineStroke,
                pathEffect = dashEffect
            )

            // 右场水平线
            drawLine(
                color = courtLineColor,
                start = Offset(midX + 12.dp.toPx(), midY),
                end = Offset(w - 24.dp.toPx(), midY),
                strokeWidth = courtLineStroke,
                pathEffect = dashEffect
            )

            // 2. 计算发球起点与对角接发球终点坐标
            // 默认情况（控制栏在上方）：
            // 左半场：面向网(向右)，右发球区在下半部 (y > midY)，左发球区在上半部 (y < midY)
            // 右半场：面向网(向左)，右发球区在上半部 (y < midY)，左发球区在下半部 (y > midY)
            // 若方向反装，则上下完全颠倒
            val leftRightCourtIsBottom = !isDirectionReversed

            val serverX: Float
            val serverY: Float
            val receiverX: Float
            val receiverY: Float

            if (isLeftServer) {
                serverX = w * 0.22f
                val serverIsBottom = if (isServingFromRightCourt) leftRightCourtIsBottom else !leftRightCourtIsBottom
                serverY = if (serverIsBottom) h * 0.72f else h * 0.28f

                // 对角接发球区在右场对应的右发球区/左发球区（即几何对角位置）
                receiverX = w * 0.78f
                receiverY = if (serverIsBottom) h * 0.28f else h * 0.72f
            } else {
                serverX = w * 0.78f
                val serverIsBottom = if (isServingFromRightCourt) !leftRightCourtIsBottom else leftRightCourtIsBottom
                serverY = if (serverIsBottom) h * 0.72f else h * 0.28f

                // 对角接发球区在左场
                receiverX = w * 0.22f
                receiverY = if (serverIsBottom) h * 0.28f else h * 0.72f
            }

            // 3. 绘制动态流动的发球对角虚线与发球方向箭头
            val movingDash = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), dashPhase)
            drawLine(
                color = ServerGold.copy(alpha = 0.85f * pulseGlow),
                start = Offset(serverX, serverY),
                end = Offset(receiverX, receiverY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = movingDash
            )

            // 发球方向箭头 (在中间靠近接发球方位置)
            val arrowT = 0.65f
            val ax = serverX + (receiverX - serverX) * arrowT
            val ay = serverY + (receiverY - serverY) * arrowT
            val angle = atan2(receiverY - serverY, receiverX - serverX) * 180f / PI.toFloat()

            rotate(degrees = angle, pivot = Offset(ax, ay)) {
                val arrowPath = Path().apply {
                    moveTo(ax + 16.dp.toPx(), ay)
                    lineTo(ax - 10.dp.toPx(), ay - 10.dp.toPx())
                    lineTo(ax - 4.dp.toPx(), ay)
                    lineTo(ax - 10.dp.toPx(), ay + 10.dp.toPx())
                    close()
                }
                drawPath(
                    path = arrowPath,
                    color = ServerGold.copy(alpha = pulseGlow)
                )
            }

            // 4. 发球站位光环
            drawCircle(
                color = ServerGold.copy(alpha = 0.22f * pulseGlow),
                radius = 42.dp.toPx(),
                center = Offset(serverX, serverY)
            )
            drawCircle(
                color = ServerGold.copy(alpha = 0.95f),
                radius = 7.dp.toPx(),
                center = Offset(serverX, serverY)
            )

            // 5. 接发球目标靶环
            drawCircle(
                color = Color.White.copy(alpha = 0.20f),
                radius = 34.dp.toPx(),
                center = Offset(receiverX, receiverY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.75f * pulseGlow),
                radius = 5.dp.toPx(),
                center = Offset(receiverX, receiverY)
            )
        }

        // 浮动发球站位与半场说明小标签（不阻挡主要比分，放置在四角发球区内）
        ServeCourtLabels(
            matchState = matchState,
            isDirectionReversed = isDirectionReversed
        )
    }
}

/**
 * 在四个半区显示“右发球区 / 左发球区”与双打时的球员站位提示
 */
@Composable
private fun BoxWithConstraintsScope.ServeCourtLabels(
    matchState: MatchState,
    isDirectionReversed: Boolean
) {
    val leftRightIsBottom = !isDirectionReversed
    val isLeftServer = matchState.servingSide == TeamSide.LEFT
    val isRightServer = matchState.servingSide == TeamSide.RIGHT
    val isRightCourt = matchState.isServingFromRightCourt

    // 左场 - 上半区
    CourtZoneBadge(
        courtName = if (leftRightIsBottom) "左发球区" else "右发球区",
        playerName = if (matchState.matchType == MatchType.DOUBLES) {
            if (leftRightIsBottom) matchState.leftLeftCourtPlayer else matchState.leftRightCourtPlayer
        } else null,
        isServingHere = isLeftServer && (if (leftRightIsBottom) !isRightCourt else isRightCourt),
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 28.dp, top = 40.dp)
    )

    // 左场 - 下半区
    CourtZoneBadge(
        courtName = if (leftRightIsBottom) "右发球区" else "左发球区",
        playerName = if (matchState.matchType == MatchType.DOUBLES) {
            if (leftRightIsBottom) matchState.leftRightCourtPlayer else matchState.leftLeftCourtPlayer
        } else null,
        isServingHere = isLeftServer && (if (leftRightIsBottom) isRightCourt else !isRightCourt),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 28.dp, bottom = 26.dp)
    )

    // 右场 - 上半区
    CourtZoneBadge(
        courtName = if (leftRightIsBottom) "右发球区" else "左发球区",
        playerName = if (matchState.matchType == MatchType.DOUBLES) {
            if (leftRightIsBottom) matchState.rightRightCourtPlayer else matchState.rightLeftCourtPlayer
        } else null,
        isServingHere = isRightServer && (if (leftRightIsBottom) isRightCourt else !isRightCourt),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 28.dp, top = 40.dp)
    )

    // 右场 - 下半区
    CourtZoneBadge(
        courtName = if (leftRightIsBottom) "左发球区" else "右发球区",
        playerName = if (matchState.matchType == MatchType.DOUBLES) {
            if (leftRightIsBottom) matchState.rightLeftCourtPlayer else matchState.rightRightCourtPlayer
        } else null,
        isServingHere = isRightServer && (if (leftRightIsBottom) !isRightCourt else isRightCourt),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 28.dp, bottom = 26.dp)
    )
}

@Composable
private fun CourtZoneBadge(
    courtName: String,
    playerName: String?,
    isServingHere: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isServingHere) ServerGold.copy(alpha = 0.90f) else Color(0x33000000)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isServingHere) {
                Text(
                    text = "🏸 发球站位",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E1B18)
                )
            } else {
                Text(
                    text = courtName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xCCFFFFFF)
                )
            }

            if (playerName != null) {
                Text(
                    text = "· $playerName",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isServingHere) Color(0xFF1E1B18) else Color(0xEEFFFFFF)
                )
            }
        }
    }
}
