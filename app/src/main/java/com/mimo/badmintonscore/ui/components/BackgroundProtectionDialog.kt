package com.mimo.badmintonscore.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val PopDarkBorder = Color(0xFF1E1B18)
private val PopCreamBg = Color(0xFFFBF7EE)
private val PopYellow = Color(0xFFFFD028)
private val PopSubtitlePurple = Color(0xFF8B5CF6)
private val PopGreen = Color(0xFF10B981)
private val PopBlue = Color(0xFF38BDF8)
private val PopTextMuted = Color(0xFF64748B)

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }
    return true
}

fun requestIgnoreBatteryOptimizations(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            } catch (e2: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun BackgroundProtectionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var isIgnoringBattery by remember {
        mutableStateOf(isIgnoringBatteryOptimizations(context))
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
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            // 3D 阴影底色
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(PopDarkBorder, RoundedCornerShape(24.dp))
            )

            // 对话框主卡片
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
                    // Header
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
                                Text(text = "🛡️", fontSize = 18.sp)
                            }

                            Column {
                                Text(
                                    text = "后台保护与防杀机制",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                                Text(
                                    text = "BACKGROUND KEEP-ALIVE & PROTECTION",
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

                    // 1. 前台服务常驻保活
                    ProtectionFeatureCard(
                        icon = Icons.Default.NotificationsActive,
                        iconTint = PopBlue,
                        title = "前台常驻服务保活 (已激活)",
                        badgeText = "运行中",
                        badgeBg = PopGreen,
                        description = "比赛进行时自动在系统通知栏常驻并更新比分，将应用进程提升至前台优先级，防止系统因内存清理误杀后台。"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 电池优化白名单保护
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.2.dp, PopDarkBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = if (isIgnoringBattery) PopGreen else Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "系统免电池优化 (白名单)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopDarkBorder
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isIgnoringBattery) PopGreen else Color(0xFFF59E0B),
                                    modifier = Modifier.border(1.5.dp, PopDarkBorder, RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = if (isIgnoringBattery) "已允许" else "未允许",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "将本软件设为无限制省电策略，避免手机切到后台锁屏或切换微信时被系统电池管理强杀。",
                                fontSize = 11.sp,
                                color = PopTextMuted,
                                lineHeight = 16.sp
                            )

                            if (!isIgnoringBattery) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        requestIgnoreBatteryOptimizations(context)
                                        // 延迟刷新状态
                                        isIgnoringBattery = isIgnoringBatteryOptimizations(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PopYellow),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, PopDarkBorder),
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                ) {
                                    Text(
                                        text = "⚡ 一键申请加入免电池优化白名单",
                                        color = PopDarkBorder,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 实时比分断电/强杀秒级恢复
                    ProtectionFeatureCard(
                        icon = Icons.Default.Security,
                        iconTint = PopSubtitlePurple,
                        title = "比赛实时持久化与秒级恢复",
                        badgeText = "双重保护",
                        badgeBg = PopSubtitlePurple,
                        description = "每次击球加分与暂停换边均毫秒级写入本地存储。即使遇到极端内存清理或重启手机，再次打开软件即可一键无缝恢复比赛！"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 常见手机防杀后台提示
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, PopDarkBorder, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PopSubtitlePurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "主流机型最佳防杀设置建议:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopDarkBorder
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• 小米/红米/HyperOS: 多任务后台下拉卡片加锁，自启动开启。\n• 华为/荣耀/鸿蒙: 应用启动管理改为手动管理，允许后台活动。\n• OPPO/vivo/一加: 电池管理中设置允许完全后台行为。",
                                fontSize = 10.5.sp,
                                color = PopTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 完成按钮
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
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDismiss()
                                },
                            color = PopYellow
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "完成并了解",
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

@Composable
private fun ProtectionFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    badgeText: String,
    badgeBg: Color,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.2.dp, PopDarkBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = PopDarkBorder
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    modifier = Modifier.border(1.5.dp, PopDarkBorder, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = PopTextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
