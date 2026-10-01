package com.mimo.badmintonscore.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary

@Composable
fun ModeSelectScreen(
    onSelectMode: (targetScore: Int, leftName: String, rightName: String) -> Unit
) {
    var leftTeamName by remember { mutableStateOf("蓝方") }
    var rightTeamName by remember { mutableStateOf("红方") }
    var showNameEditDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = if (isLandscape) 12.dp else 20.dp)
    ) {
        if (isLandscape) {
            // LANDSCAPE HOME LAYOUT
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Branding & Team Setup
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF3B82F6))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏸",
                            fontSize = 34.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "羽毛球比赛计分",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "BADMINTON SCOREBOARD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Team names badge (clickable to edit)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33334155),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showNameEditDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = leftTeamName,
                                color = TeamBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "  VS  ",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                            Text(
                                text = rightTeamName,
                                color = TeamRedPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "修改队伍名",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💡 左右点击大色块直接加分 · 支持左单右双发球提示",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.85f)
                        .background(Color(0xFF334155))
                )

                // Right Column: Mode Cards
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "请选择比赛赛制：",
                        color = Color(0xFFCBD5E1),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    // 15 Points Card
                    ModeCard(
                        score = 15,
                        title = "15 分制",
                        subtitle = "14平后净胜2分 · 封顶21分",
                        accentColor = Color(0xFF00E5FF),
                        icon = Icons.Default.Bolt,
                        onClick = {
                            onSelectMode(15, leftTeamName, rightTeamName)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 31 Points Card
                    ModeCard(
                        score = 31,
                        title = "31 分制",
                        subtitle = "30平后净胜2分 · 封顶36分",
                        accentColor = Color(0xFFFFB300),
                        icon = Icons.Default.Timer,
                        onClick = {
                            onSelectMode(31, leftTeamName, rightTeamName)
                        }
                    )
                }
            }
        } else {
            // PORTRAIT HOME LAYOUT
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF3B82F6))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏸",
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "羽毛球比赛计分",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "BADMINTON SCOREBOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Team names badge (clickable to edit)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33334155),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showNameEditDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = leftTeamName,
                                color = TeamBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "  VS  ",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Text(
                                text = rightTeamName,
                                color = TeamRedPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "修改队伍名",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Mode Selection Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "请选择比赛赛制：",
                        color = Color(0xFFCBD5E1),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    // 15 Points Card
                    ModeCard(
                        score = 15,
                        title = "15 分制",
                        subtitle = "14平后净胜2分 · 封顶21分",
                        accentColor = Color(0xFF00E5FF),
                        icon = Icons.Default.Bolt,
                        onClick = {
                            onSelectMode(15, leftTeamName, rightTeamName)
                        }
                    )

                    // 31 Points Card
                    ModeCard(
                        score = 31,
                        title = "31 分制",
                        subtitle = "30平后净胜2分 · 封顶36分",
                        accentColor = Color(0xFFFFB300),
                        icon = Icons.Default.Timer,
                        onClick = {
                            onSelectMode(31, leftTeamName, rightTeamName)
                        }
                    )
                }

                // Bottom Tip
                Text(
                    text = "💡 进入比赛后自动转为横屏，左右点击大色块直接计分",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }

    // Edit Name Dialog
    if (showNameEditDialog) {
        var tempLeft by remember { mutableStateOf(leftTeamName) }
        var tempRight by remember { mutableStateOf(rightTeamName) }

        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            title = { Text("自定义队伍 / 选手名称") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempLeft,
                        onValueChange = { tempLeft = it },
                        label = { Text("左侧选手/队伍 (蓝方)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempRight,
                        onValueChange = { tempRight = it },
                        label = { Text("右侧选手/队伍 (红方)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    leftTeamName = tempLeft.ifBlank { "蓝方" }
                    rightTeamName = tempRight.ifBlank { "红方" }
                    showNameEditDialog = false
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ModeCard(
    score: Int,
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
        color = Color(0xFF1E293B),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content with weight
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Score Badge (guaranteed horizontal, never wrap)
            Surface(
                color = accentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "${score}分",
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}
