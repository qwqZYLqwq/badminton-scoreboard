package com.mimo.badmintonscore.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.MatchType
import com.mimo.badmintonscore.ui.components.ServeAssistDialog

// Neo-Pop Arcade Palette
private val PopCreamBg = Color(0xFFFBF7EE)
private val PopDarkBorder = Color(0xFF1E1B18)
private val PopYellow = Color(0xFFFFEB3B)
private val PopBlue = Color(0xFF2563EB)
private val PopRed = Color(0xFFDC2626)
private val PopCoralVs = Color(0xFFFF5722)
private val PopSubtitlePurple = Color(0xFF6366F1)
private val PopCardSkyBlue = Color(0xFF38BDF8)
private val PopBadgeSkyBlue = Color(0xFF0284C7)
private val PopCardYellow = Color(0xFFFACC15)
private val PopBadgeOrange = Color(0xFFEA580C)
private val PopTextMuted = Color(0xFF64748B)

@Composable
fun ModeSelectScreen(
    onSelectMode: (
        targetScore: Int,
        leftName: String,
        rightName: String,
        matchType: MatchType,
        enableIntervalTimer: Boolean,
        isServeAssistantEnabled: Boolean,
        isServeDirectionReversed: Boolean
    ) -> Unit
) {
    var leftTeamName by remember { mutableStateOf("蓝方") }
    var rightTeamName by remember { mutableStateOf("红方") }
    var matchType by remember { mutableStateOf(MatchType.SINGLES) }
    var enableIntervalTimer by remember { mutableStateOf(true) }
    var isServeAssistantEnabled by remember { mutableStateOf(false) }
    var isServeDirectionReversed by remember { mutableStateOf(false) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showServeAssistDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopCreamBg)
    ) {
        // 1. Retro Polka-Dot Arcade Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotColor = Color(0x181E1B18)
            val spacing = 24.dp.toPx()
            val dotRadius = 1.6.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
        }

        // 2. Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = if (isLandscape) 14.dp else 20.dp)
        ) {
            if (isLandscape) {
                // ==================== LANDSCAPE LAYOUT ====================
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Branding, Team Setup & Singles/Doubles Switch
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Tilted Mascot Box with 3D shadow
                        Box(modifier = Modifier.size(62.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = 3.5.dp, y = 3.5.dp)
                                    .background(PopDarkBorder, RoundedCornerShape(20.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(-3f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PopYellow)
                                    .border(3.dp, PopDarkBorder, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏸", fontSize = 32.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title with Neo-Pop 3D shadow
                        Box {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 2.5.dp, y = 2.5.dp)
                                    .background(PopDarkBorder, RoundedCornerShape(12.dp))
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.border(2.5.dp, PopDarkBorder, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = "羽毛球比赛计分",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "★ ARCADE SCOREKEEPER ★",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = PopSubtitlePurple,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Team Matchup Pill
                        NeoPopTeamPill(
                            leftName = leftTeamName,
                            rightName = rightTeamName,
                            onClick = { showNameEditDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 单打 / 双打选择切换器 (在该蓝红方昵称的正下方)
                        NeoPopMatchTypeSwitch(
                            selectedType = matchType,
                            onSelectType = { matchType = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "💡 进入比赛后自动切为横屏 · 点大色块扣杀加分",
                            color = PopTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Divider line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight(0.85f)
                            .background(Color(0x331E1B18))
                    )

                    // Right Column: Neo-Pop Mode Cards & Settings
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(start = 20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CHOOSE MATCH MODE / 赛制选择",
                            color = PopDarkBorder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                        )

                        NeoPopModeCard(
                            score = 15,
                            title = "15 分竞速局",
                            subtitle = "14平净胜2分 · 封顶21分 · 三局两胜",
                            icon = Icons.Default.Bolt,
                            iconColor = PopCardSkyBlue,
                            badgeColor = PopBadgeSkyBlue,
                            onClick = {
                                onSelectMode(15, leftTeamName, rightTeamName, matchType, enableIntervalTimer, isServeAssistantEnabled, isServeDirectionReversed)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        NeoPopModeCard(
                            score = 21,
                            title = "21 分正规赛",
                            subtitle = "20平净胜2分 · 封顶30分 · 三局两胜",
                            icon = Icons.Default.Timer,
                            iconColor = PopCardYellow,
                            badgeColor = PopBadgeOrange,
                            onClick = {
                                onSelectMode(21, leftTeamName, rightTeamName, matchType, enableIntervalTimer, isServeAssistantEnabled, isServeDirectionReversed)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 赛制选择下方的设置选项栏：休息计时开关 & 发球辅助设置
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 休息计时开关
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, PopDarkBorder, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("⏱️ 休息计时", fontWeight = FontWeight.Black, fontSize = 11.5.sp, color = PopDarkBorder)
                                        Text("局中与局间倒计时", fontSize = 9.sp, color = PopTextMuted)
                                    }
                                    Switch(
                                        checked = enableIntervalTimer,
                                        onCheckedChange = { enableIntervalTimer = it },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                            }

                            // 发球辅助设置按钮 (点击弹出二级菜单)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, PopDarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { showServeAssistDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("📐 发球辅助设置", fontWeight = FontWeight.Black, fontSize = 11.5.sp, color = PopDarkBorder)
                                        Text(
                                            text = if (isServeAssistantEnabled) (if (isServeDirectionReversed) "已开启 · 反装" else "已开启 · 默认") else "未开启",
                                            fontSize = 9.sp,
                                            color = if (isServeAssistantEnabled) PopBlue else PopTextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = PopDarkBorder,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==================== PORTRAIT LAYOUT ====================
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Mascot Box
                        Box(modifier = Modifier.size(68.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = 3.5.dp, y = 3.5.dp)
                                    .background(PopDarkBorder, RoundedCornerShape(22.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(-3f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(PopYellow)
                                    .border(3.2.dp, PopDarkBorder, RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏸", fontSize = 36.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Neo-Pop Title Badge
                        Box {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(PopDarkBorder, RoundedCornerShape(14.dp))
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.border(3.dp, PopDarkBorder, RoundedCornerShape(14.dp))
                            ) {
                                Text(
                                    text = "羽毛球比赛计分",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "★ ARCADE SCOREKEEPER ★",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PopSubtitlePurple,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Team Matchup Pill
                        NeoPopTeamPill(
                            leftName = leftTeamName,
                            rightName = rightTeamName,
                            onClick = { showNameEditDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 单打 / 双打选择切换器
                        NeoPopMatchTypeSwitch(
                            selectedType = matchType,
                            onSelectType = { matchType = it }
                        )
                    }

                    // Mode Selection Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CHOOSE MATCH MODE / 赛制选择",
                            color = PopDarkBorder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                        )

                        NeoPopModeCard(
                            score = 15,
                            title = "15 分竞速局",
                            subtitle = "14平净胜2分 · 封顶21分 · 三局两胜",
                            icon = Icons.Default.Bolt,
                            iconColor = PopCardSkyBlue,
                            badgeColor = PopBadgeSkyBlue,
                            onClick = {
                                onSelectMode(15, leftTeamName, rightTeamName, matchType, enableIntervalTimer, isServeAssistantEnabled, isServeDirectionReversed)
                            }
                        )

                        NeoPopModeCard(
                            score = 21,
                            title = "21 分正规赛",
                            subtitle = "20平净胜2分 · 封顶30分 · 三局两胜",
                            icon = Icons.Default.Timer,
                            iconColor = PopCardYellow,
                            badgeColor = PopBadgeOrange,
                            onClick = {
                                onSelectMode(21, leftTeamName, rightTeamName, matchType, enableIntervalTimer, isServeAssistantEnabled, isServeDirectionReversed)
                            }
                        )

                        // 赛制选择下方的设置选项栏：休息计时开关 & 发球辅助设置
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, PopDarkBorder, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("⏱️ 休息计时", fontWeight = FontWeight.Black, fontSize = 11.sp, color = PopDarkBorder)
                                        Text("倒计时提示", fontSize = 9.sp, color = PopTextMuted)
                                    }
                                    Switch(
                                        checked = enableIntervalTimer,
                                        onCheckedChange = { enableIntervalTimer = it },
                                        modifier = Modifier.scale(0.75f)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, PopDarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { showServeAssistDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("📐 发球辅助", fontWeight = FontWeight.Black, fontSize = 11.sp, color = PopDarkBorder)
                                        Text(
                                            text = if (isServeAssistantEnabled) "已开启" else "未开启",
                                            fontSize = 9.sp,
                                            color = if (isServeAssistantEnabled) PopBlue else PopTextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = PopDarkBorder,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Footer Tip
                    Text(
                        text = "💡 进入比赛后自动切为横屏 · 点大色块扣杀加分",
                        color = PopTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }

    // 发球辅助二级设置菜单弹窗
    if (showServeAssistDialog) {
        ServeAssistDialog(
            isServeAssistEnabled = isServeAssistantEnabled,
            onToggleServeAssist = { isServeAssistantEnabled = it },
            isDirectionReversed = isServeDirectionReversed,
            onToggleDirectionReversed = { isServeDirectionReversed = it },
            onDismiss = { showServeAssistDialog = false }
        )
    }

    // Name Edit Dialog (Neo-Pop Arcade Style)
    if (showNameEditDialog) {
        var tempLeft by remember { mutableStateOf(leftTeamName) }
        var tempRight by remember { mutableStateOf(rightTeamName) }

        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            title = {
                Text(
                    text = "自定义选手 / 战队名称",
                    fontWeight = FontWeight.Black,
                    color = PopDarkBorder
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempLeft,
                        onValueChange = { tempLeft = it },
                        label = { Text("左侧选手/战队 (蓝方)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempRight,
                        onValueChange = { tempRight = it },
                        label = { Text("右侧选手/战队 (红方)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        leftTeamName = tempLeft.ifBlank { "蓝方" }
                        rightTeamName = tempRight.ifBlank { "红方" }
                        showNameEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PopDarkBorder)
                ) {
                    Text("保存", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditDialog = false }) {
                    Text("取消", color = PopTextMuted)
                }
            }
        )
    }
}

/**
 * Neo-Pop Arcade Single/Double Match Type Switch (单打/双打切换器)
 */
@Composable
private fun NeoPopMatchTypeSwitch(
    selectedType: MatchType,
    onSelectType: (MatchType) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.wrapContentSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.5.dp, y = 2.5.dp)
                .background(PopDarkBorder, RoundedCornerShape(14.dp))
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            modifier = Modifier.border(2.5.dp, PopDarkBorder, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 单打
                val isSingles = selectedType == MatchType.SINGLES
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSingles) PopYellow else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!isSingles) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectType(MatchType.SINGLES)
                            }
                        }
                        .then(if (isSingles) Modifier.border(1.8.dp, PopDarkBorder, RoundedCornerShape(10.dp)) else Modifier)
                ) {
                    Text(
                        text = "🏸 单打模式",
                        fontWeight = if (isSingles) FontWeight.Black else FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PopDarkBorder,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // 双打
                val isDoubles = selectedType == MatchType.DOUBLES
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDoubles) PopYellow else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!isDoubles) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectType(MatchType.DOUBLES)
                            }
                        }
                        .then(if (isDoubles) Modifier.border(1.8.dp, PopDarkBorder, RoundedCornerShape(10.dp)) else Modifier)
                ) {
                    Text(
                        text = "👥 双打模式",
                        fontWeight = if (isDoubles) FontWeight.Black else FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PopDarkBorder,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Neo-Pop Arcade Team Matchup Pill with thick 3D black border and shadow
 */
@Composable
private fun NeoPopTeamPill(
    leftName: String,
    rightName: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.wrapContentSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.5.dp, y = 3.5.dp)
                .background(PopDarkBorder, RoundedCornerShape(18.dp))
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .border(3.dp, PopDarkBorder, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leftName,
                    color = PopBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // VS Badge
                Surface(
                    color = PopCoralVs,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.5.dp, PopDarkBorder, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "VS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = rightName,
                    color = PopRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "修改队伍名",
                    tint = PopDarkBorder,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Neo-Pop 3D physical push-button style match card
 */
@Composable
private fun NeoPopModeCard(
    score: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pressOffset = remember { Animatable(0f) }
    var isPressed by remember { mutableStateOf(false) }

    fun handleCardClick() {
        if (isPressed) return
        isPressed = true
        coroutineScope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // Physical button press down into shadow (matching the HTML active effect)
            pressOffset.animateTo(3.5f, tween(durationMillis = 70))
            pressOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            delay(50L)
            onClick()
            isPressed = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        // Solid black 3D shadow block
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.5.dp, y = 4.5.dp)
                .background(PopDarkBorder, RoundedCornerShape(22.dp))
        )

        // White card face with thick black border (physically presses down into shadow)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = pressOffset.value.dp, y = pressOffset.value.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(3.2.dp, PopDarkBorder, RoundedCornerShape(22.dp))
                .clickable(enabled = !isPressed) { handleCardClick() },
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box with 3D offset
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = 2.5.dp, y = 2.5.dp)
                            .background(PopDarkBorder, RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconColor)
                            .border(2.5.dp, PopDarkBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PopDarkBorder,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PopDarkBorder
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Score Badge with 3D offset
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 2.dp, y = 2.dp)
                            .background(PopDarkBorder, RoundedCornerShape(10.dp))
                    )
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(2.dp, PopDarkBorder, RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "${score}分",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}
