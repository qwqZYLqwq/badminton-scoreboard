package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Neo-Pop Arcade Palette
private val PopCreamBg = Color(0xFFFBF7EE)
private val PopDarkBorder = Color(0xFF1E1B18)
private val PopYellow = Color(0xFFFFEB3B)
private val PopBlue = Color(0xFF2563EB)
private val PopRed = Color(0xFFDC2626)
private val PopSubtitlePurple = Color(0xFF6366F1)
private val PopTextMuted = Color(0xFF64748B)

@Composable
fun EditTeamNamesDialog(
    initialLeftName: String,
    initialRightName: String,
    onConfirm: (leftName: String, rightName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempLeft by remember { mutableStateOf(initialLeftName) }
    var tempRight by remember { mutableStateOf(initialRightName) }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val confirmPressOffset = remember { Animatable(0f) }
    val cancelPressOffset = remember { Animatable(0f) }
    val swapPressOffset = remember { Animatable(0f) }
    val scrollState = rememberScrollState()

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
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            // 1. 3D 实体黑边阴影
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(PopDarkBorder, RoundedCornerShape(24.dp))
            )

            // 2. 弹窗主卡片 (Neo-Pop Arcade Style)
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
                    // Header: Mascot + Title + Close Button
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
                                Text(text = "✏️", fontSize = 18.sp)
                            }

                            Column {
                                Text(
                                    text = "修改双方选手名字",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                                Text(
                                    text = "EDIT PLAYER / TEAM NAMES",
                                    fontSize = 9.sp,
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
                                tint = PopDarkBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Left Team (蓝方) Input Card
                    TeamInputCard(
                        teamLabel = "蓝方 · 左侧",
                        teamCaption = "开局位于左半场",
                        badgeColor = PopBlue,
                        value = tempLeft,
                        onValueChange = { tempLeft = it },
                        placeholder = "输入左侧选手/战队名"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Center Swap Pill Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = swapPressOffset.value.dp, y = swapPressOffset.value.dp)
                        ) {
                            // 3D shadow for swap button
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 2.dp, y = 2.dp)
                                    .background(PopDarkBorder, RoundedCornerShape(10.dp))
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .border(2.dp, PopDarkBorder, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            swapPressOffset.animateTo(2f, tween(40))
                                            swapPressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                            val t = tempLeft
                                            tempLeft = tempRight
                                            tempRight = t
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "交换红蓝双方",
                                        tint = PopDarkBorder,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "交换红蓝双方",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopDarkBorder
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Right Team (红方) Input Card
                    TeamInputCard(
                        teamLabel = "红方 · 右侧",
                        teamCaption = "开局位于右半场",
                        badgeColor = PopRed,
                        value = tempRight,
                        onValueChange = { tempRight = it },
                        placeholder = "输入右侧选手/战队名"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Preset Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "快速填充:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopTextMuted
                        )
                        PresetChip(
                            label = "默认",
                            onClick = {
                                tempLeft = "蓝方"
                                tempRight = "红方"
                            }
                        )
                        PresetChip(
                            label = "选手A/B",
                            onClick = {
                                tempLeft = "选手A"
                                tempRight = "选手B"
                            }
                        )
                        PresetChip(
                            label = "一队/二队",
                            onClick = {
                                tempLeft = "一队"
                                tempRight = "二队"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Action Buttons (取消 / 保存)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 取消按键
                        Box(
                            modifier = Modifier
                                .weight(1f)
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
                                    .offset(x = cancelPressOffset.value.dp, y = cancelPressOffset.value.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(2.5.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            cancelPressOffset.animateTo(2.5f, tween(50))
                                            cancelPressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                            delay(40L)
                                            onDismiss()
                                        }
                                    },
                                color = Color.White
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "取消",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopDarkBorder
                                    )
                                }
                            }
                        }

                        // 保存按键
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
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
                                    .border(2.5.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            confirmPressOffset.animateTo(2.5f, tween(50))
                                            confirmPressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                            delay(40L)
                                            val finalLeft = tempLeft.trim().ifBlank { "蓝方" }
                                            val finalRight = tempRight.trim().ifBlank { "红方" }
                                            onConfirm(finalLeft, finalRight)
                                        }
                                    },
                                color = PopYellow
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "保存设置",
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
}

/**
 * Neo-Pop Arcade Team Input Card
 */
@Composable
private fun TeamInputCard(
    teamLabel: String,
    teamCaption: String,
    badgeColor: Color,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, PopDarkBorder, RoundedCornerShape(14.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.5.dp, PopDarkBorder, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = teamLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = teamCaption,
                    fontSize = 10.sp,
                    color = PopTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = PopTextMuted,
                        fontSize = 13.5.sp
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopDarkBorder
                ),
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清空",
                                tint = PopTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedBorderColor = badgeColor,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    cursorColor = badgeColor,
                    focusedTextColor = PopDarkBorder,
                    unfocusedTextColor = PopDarkBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }
    }
}

/**
 * Neo-Pop Quick Preset Chip
 */
@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        modifier = Modifier
            .border(1.5.dp, PopDarkBorder, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = PopDarkBorder,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
