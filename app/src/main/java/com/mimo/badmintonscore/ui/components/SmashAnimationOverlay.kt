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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.theme.ServerGold
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Represents a single projectile currently in flight.
 */
data class ActiveShuttlecock(
    val id: Long,
    val scorerSide: TeamSide,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val arcPeakHeight: Float
)

/**
 * Represents an impact explosion after the shuttlecock hits the opponent side,
 * while the celebratory +1 badge displays on the scorer's side.
 */
data class ImpactEffect(
    val id: Long,
    val hitX: Float,
    val hitY: Float,
    val scorerX: Float,
    val scorerY: Float,
    val scorerSide: TeamSide
)

@Composable
fun SmashAnimationOverlay(
    activeShuttlecocks: List<ActiveShuttlecock>,
    activeImpacts: List<ImpactEffect>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Draw Active Flying Shuttlecocks & Motion Trails concurrently
        activeShuttlecocks.forEach { shuttle ->
            key(shuttle.id) {
                FlyingShuttlecockView(shuttlecock = shuttle)
            }
        }

        // 2. Draw Impact Shockwaves & Spark Bursts concurrently
        activeImpacts.forEach { impact ->
            key(impact.id) {
                ImpactBurstView(impact = impact)
            }
        }
    }
}

@Composable
private fun FlyingShuttlecockView(shuttlecock: ActiveShuttlecock) {
    val progress = remember(shuttlecock.id) { Animatable(0f) }

    LaunchedEffect(shuttlecock.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 360,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
            )
        )
    }

    val t = progress.value
    val startX = shuttlecock.startX
    val startY = shuttlecock.startY
    val endX = shuttlecock.endX
    val endY = shuttlecock.endY
    val arcHeight = shuttlecock.arcPeakHeight

    // Parabolic trajectory formula:
    // x(t) = startX + (endX - startX) * t
    // y(t) = startY + (endY - startY) * t - 4 * arcHeight * t * (1 - t)
    val currentX = startX + (endX - startX) * t
    val currentY = startY + (endY - startY) * t - 4f * arcHeight * t * (1f - t)

    // Tangent derivative for rotation angle:
    // dx/dt = endX - startX
    // dy/dt = endY - startY - 4 * arcHeight * (1 - 2t)
    val dx = endX - startX
    val dy = (endY - startY) - 4f * arcHeight * (1f - 2f * t)
    val angleRad = atan2(dy, dx)
    val angleDeg = (angleRad * 180f / PI).toFloat()

    val trailColor = if (shuttlecock.scorerSide == TeamSide.LEFT) {
        Color(0xFF60A5FA)
    } else {
        Color(0xFFF87171)
    }

    // Canvas drawing shuttlecock + speed trail
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Draw trailing speed sparks & lines (scaled up)
        for (i in 1..5) {
            val trailT = (t - i * 0.045f).coerceIn(0f, 1f)
            if (trailT > 0f) {
                val tx = startX + (endX - startX) * trailT
                val ty = startY + (endY - startY) * trailT - 4f * arcHeight * trailT * (1f - trailT)
                val alpha = (1f - i * 0.18f) * 0.75f
                val radius = (16f - i * 2.4f).coerceAtLeast(3.5f)

                drawCircle(
                    color = trailColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(tx, ty)
                )
                // Soft white inner glow
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.9f),
                    radius = radius * 0.5f,
                    center = Offset(tx, ty)
                )
            }
        }

        // Draw the Shuttlecock at (currentX, currentY) rotated by angleDeg (scaled up to 2.7f for great visibility)
        rotate(degrees = angleDeg, pivot = Offset(currentX, currentY)) {
            drawStylizedShuttlecock(
                center = Offset(currentX, currentY),
                teamColor = trailColor,
                scale = 2.7f
            )
        }
    }
}

/**
 * Draws a sharp, lively stylized badminton shuttlecock pointing to the right (0 degrees).
 */
private fun DrawScope.drawStylizedShuttlecock(
    center: Offset,
    teamColor: Color,
    scale: Float
) {
    val length = 40f * scale
    val halfWidth = 16f * scale
    val corkRadius = 8f * scale

    // Feather skirt (white trapezoid)
    val skirtPath = Path().apply {
        moveTo(center.x - length * 0.6f, center.y - halfWidth)
        lineTo(center.x + length * 0.15f, center.y - halfWidth * 0.45f)
        lineTo(center.x + length * 0.15f, center.y + halfWidth * 0.45f)
        lineTo(center.x - length * 0.6f, center.y + halfWidth)
        close()
    }

    // Skirt fill with gradient
    drawPath(
        path = skirtPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xCCFFFFFF), Color(0xFFFFFFFF)),
            startX = center.x - length * 0.6f,
            endX = center.x + length * 0.2f
        )
    )

    // Skirt outline
    drawPath(
        path = skirtPath,
        color = Color(0xFF94A3B8),
        style = Stroke(width = 1.5f * scale)
    )

    // Feather longitudinal rib lines
    drawLine(
        color = Color(0x66CBD5E1),
        start = Offset(center.x - length * 0.55f, center.y - halfWidth * 0.35f),
        end = Offset(center.x + length * 0.12f, center.y - halfWidth * 0.15f),
        strokeWidth = 1.2f * scale
    )
    drawLine(
        color = Color(0x66CBD5E1),
        start = Offset(center.x - length * 0.55f, center.y + halfWidth * 0.35f),
        end = Offset(center.x + length * 0.12f, center.y + halfWidth * 0.15f),
        strokeWidth = 1.2f * scale
    )

    // Ribbon / Band (team colored stripe)
    drawRect(
        color = teamColor,
        topLeft = Offset(center.x + length * 0.05f, center.y - halfWidth * 0.48f),
        size = androidx.compose.ui.geometry.Size(4f * scale, halfWidth * 0.96f)
    )

    // Cork Head (rounded semicircle in front)
    drawArc(
        color = ServerGold,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x + length * 0.15f - corkRadius, center.y - corkRadius),
        size = androidx.compose.ui.geometry.Size(corkRadius * 2f, corkRadius * 2f)
    )

    // Cork outline
    drawArc(
        color = Color(0xFFD97706),
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x + length * 0.15f - corkRadius, center.y - corkRadius),
        size = androidx.compose.ui.geometry.Size(corkRadius * 2f, corkRadius * 2f),
        style = Stroke(width = 1.5f * scale)
    )
}

/**
 * Explosion shockwave + floating "+1 SMASH!" text upon landing.
 */
@Composable
private fun ImpactBurstView(impact: ImpactEffect) {
    val burstProgress = remember(impact.id) { Animatable(0f) }

    LaunchedEffect(impact.id) {
        burstProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = LinearOutSlowInEasing)
        )
    }

    val progress = burstProgress.value
    val themeColor = if (impact.scorerSide == TeamSide.LEFT) TeamBluePrimary else TeamRedPrimary
    val density = LocalDensity.current

    // Shockwave rings & sparks via Canvas at hit location (opponent/losing court)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(impact.hitX, impact.hitY)

        // 1. Central radiant flash burst (fades quickly)
        val flashAlpha = (1f - progress * 1.6f).coerceIn(0f, 1f)
        if (flashAlpha > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = flashAlpha * 0.85f),
                radius = 60f + 120f * progress,
                center = center
            )
            drawCircle(
                color = themeColor.copy(alpha = flashAlpha * 0.5f),
                radius = 100f + 160f * progress,
                center = center
            )
        }

        // 2. Giant primary expanding shockwave ring
        val ringRadius = 35f + 320f * progress
        val ringAlpha = (1f - progress).coerceIn(0f, 1f)
        drawCircle(
            color = themeColor.copy(alpha = ringAlpha * 0.95f),
            radius = ringRadius,
            center = center,
            style = Stroke(width = (14f * (1f - progress * 0.65f)).coerceAtLeast(2.5f))
        )

        // 3. Secondary inner golden shockwave ring
        val innerRadius = 25f + 200f * progress
        drawCircle(
            color = ServerGold.copy(alpha = ringAlpha * 0.9f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = (7f * (1f - progress * 0.5f)).coerceAtLeast(1.5f))
        )

        // 4. Outer thin air-blast ripple ring
        val outerRadius = 50f + 420f * progress
        drawCircle(
            color = Color.White.copy(alpha = (ringAlpha * 0.6f)),
            radius = outerRadius,
            center = center,
            style = Stroke(width = (4f * (1f - progress)).coerceAtLeast(1f))
        )

        // 5. 12 Star / Spark particles shooting outwards across court
        val sparkCount = 12
        for (i in 0 until sparkCount) {
            val angle = (i * (360f / sparkCount) + (impact.id % 45)).toDouble() * PI / 180.0
            val distance = (45f + 240f * progress) * (0.8f + (i % 3) * 0.2f)
            val sx = center.x + (cos(angle) * distance).toFloat()
            val sy = center.y + (sin(angle) * distance).toFloat()
            val sparkSize = (10f * (1f - progress)).coerceAtLeast(2.5f)

            drawCircle(
                color = if (i % 2 == 0) ServerGold.copy(alpha = ringAlpha) else Color.White.copy(alpha = ringAlpha),
                radius = sparkSize,
                center = Offset(sx, sy)
            )
        }
    }

    // Floating "+1" badge on SCORER / WINNING side
    val offsetY = with(density) { (impact.scorerY.toDp() - 30.dp - (progress * 50f).dp) }
    val offsetX = with(density) { (impact.scorerX.toDp() - 48.dp) }
    val badgeAlpha = (1f - progress * 0.85f).coerceIn(0f, 1f)
    val badgeScale = 0.8f + 0.45f * sin(progress * PI.toFloat()).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .graphicsLayer {
                scaleX = badgeScale
                scaleY = badgeScale
                alpha = badgeAlpha
            }
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xEE0F172A),
            shadowElevation = 8.dp,
            modifier = Modifier.border(1.5f.dp, ServerGold, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "💥",
                    fontSize = 16.sp
                )
                Text(
                    text = "+1",
                    color = ServerGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }
    }
}
