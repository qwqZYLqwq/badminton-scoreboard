package com.mimo.badmintonscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val PopCreamBg = Color(0xFFFBF7EE)
private val PopDarkBorder = Color(0xFF1E1B18)
private val PopYellow = Color(0xFFFFEB3B)
private val PopBlue = Color(0xFF2563EB)
private val PopSubtitlePurple = Color(0xFF8B5CF6)
private val PopTextMuted = Color(0xFF64748B)

@Composable
fun SettingsDialog(
    initialServeAssistEnabled: Boolean,
    initialDirectionReversed: Boolean,
    initialHapticEnabled: Boolean,
    onConfirm: (assistEnabled: Boolean, directionReversed: Boolean, hapticEnabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var tempAssistEnabled by remember { mutableStateOf(initialServeAssistEnabled) }
    var tempDirectionReversed by remember { mutableStateOf(initialDirectionReversed) }
    var tempHapticEnabled by remember { mutableStateOf(initialHapticEnabled) }

    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    fun playHaptic(type: HapticFeedbackType) {
        if (tempHapticEnabled) {
            haptic.performHapticFeedback(type)
        }
    }

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
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(14.dp)
                .imePadding(),
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .verticalScroll(scrollState)
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
                                Text(text = "⚙️", fontSize = 18.sp)
                            }

                            Column {
                                Text(
                                    text = "功能与偏好设置",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                                Text(
                                    text = "SETTINGS & PREFERENCES",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopSubtitlePurple,
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
                                tint = PopDarkBorder,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. 发球辅助总开关卡片
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.2.dp, PopDarkBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsTennis,
                                        contentDescription = null,
                                        tint = PopBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "发球辅助功能",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopDarkBorder
                                    )
                                }

                                Switch(
                                    checked = tempAssistEnabled,
                                    onCheckedChange = {
                                        playHaptic(HapticFeedbackType.TextHandleMove)
                                        tempAssistEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PopBlue,
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFFE2E8F0)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "在比赛界面绘制球场四分区横线，并以对角动态虚线直观指引发球员与接发球落地区域。",
                                fontSize = 11.sp,
                                color = PopTextMuted,
                                lineHeight = 16.sp
                            )

                            // 2. 发球方向反转模式（嵌套于发球辅助中）
                            if (tempAssistEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.6.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "颠倒发球辅助方向",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PopDarkBorder
                                            )
                                            Text(
                                                text = "将辅助箭头流动方向反转180°，满足裁判特定视角的观赛需求",
                                                fontSize = 10.sp,
                                                color = PopTextMuted
                                            )
                                        }

                                        Switch(
                                            checked = tempDirectionReversed,
                                            onCheckedChange = {
                                                playHaptic(HapticFeedbackType.TextHandleMove)
                                                tempDirectionReversed = it
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = PopYellow,
                                                uncheckedThumbColor = Color(0xFF94A3B8),
                                                uncheckedTrackColor = Color(0xFFE2E8F0)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 触控震动反馈开关卡片
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.2.dp, PopDarkBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "触控震动反馈",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopDarkBorder
                                    )
                                }

                                Switch(
                                    checked = tempHapticEnabled,
                                    onCheckedChange = {
                                        tempHapticEnabled = it
                                        if (it) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFF59E0B),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFFE2E8F0)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "点击加分扣杀、撤销比分、按键切换与设置保存时提供物理触感振动。",
                                fontSize = 11.sp,
                                color = PopTextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 底部“完成设置”按键（零延迟即点即响应）
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
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.6.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    playHaptic(HapticFeedbackType.LongPress)
                                    onConfirm(tempAssistEnabled, tempDirectionReversed, tempHapticEnabled)
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
