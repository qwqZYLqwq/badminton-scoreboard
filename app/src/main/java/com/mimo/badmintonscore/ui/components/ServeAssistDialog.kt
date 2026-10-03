package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PopCreamBg = Color(0xFFFBF7EE)
private val PopDarkBorder = Color(0xFF1E1B18)
private val PopYellow = Color(0xFFFFEB3B)
private val PopBlue = Color(0xFF2563EB)
private val PopTextMuted = Color(0xFF64748B)

@Composable
fun ServeAssistDialog(
    isServeAssistEnabled: Boolean,
    onToggleServeAssist: (Boolean) -> Unit,
    isDirectionReversed: Boolean,
    onToggleDirectionReversed: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val confirmPressOffset = remember { Animatable(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(480.dp)
                .wrapContentHeight()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 3D 实体阴影块
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(PopDarkBorder, RoundedCornerShape(24.dp))
            )

            // 弹窗主表面 (Neo-Pop Arcade Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.2.dp, PopDarkBorder, RoundedCornerShape(24.dp)),
                color = PopCreamBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    // 顶部标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PopYellow)
                                    .border(2.2.dp, PopDarkBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📐", fontSize = 18.sp)
                            }

                            Column {
                                Text(
                                    text = "发球辅助设置",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                                Text(
                                    text = "SERVING ASSISTANT SETTINGS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6366F1),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = PopDarkBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 选项 1：是否开启发球辅助
                    ServeSettingCard(
                        title = "发球辅助",
                        description = "在计分板上显示球场水平线与发球虚线，实时提示发球站位与对角发球方向",
                        checked = isServeAssistEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleServeAssist(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 选项 2：是否方向反装
                    ServeSettingCard(
                        title = "方向反装",
                        description = "将发球辅助线与虚线方向颠倒（当手机放置在远离控制栏/反向面对球场时使用）",
                        checked = isDirectionReversed,
                        enabled = isServeAssistEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleDirectionReversed(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 完成按钮 (3D 实体下压按键)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(PopDarkBorder, RoundedCornerShape(14.dp))
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = confirmPressOffset.value.dp, y = confirmPressOffset.value.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.6.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        confirmPressOffset.animateTo(2.5f, tween(50))
                                        confirmPressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                        delay(40L)
                                        onDismiss()
                                    }
                                },
                            color = PopYellow
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "完成设置",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 每一项的卡片：左侧标题，靠近名称一方有小字描述该功能，右侧开关按钮
 */
@Composable
private fun ServeSettingCard(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, PopDarkBorder, RoundedCornerShape(14.dp)),
        color = if (enabled) Color.White else Color(0xFFF1F5F9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) PopDarkBorder else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = if (enabled) PopTextMuted else Color.LightGray,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PopBlue,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }
    }
}
