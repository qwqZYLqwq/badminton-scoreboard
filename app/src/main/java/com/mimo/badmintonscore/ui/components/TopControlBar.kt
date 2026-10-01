package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary

@Composable
fun TopControlBar(
    isVisible: Boolean,
    matchState: MatchState,
    onAddScore: (TeamSide) -> Unit,
    onMinusScore: (TeamSide) -> Unit,
    onSwapSides: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onExit: () -> Unit,
    onExpandBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Subtle expand handle when hidden
        AnimatedVisibility(
            visible = !isVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .clickable { onExpandBar() },
                color = Color(0xCC0F172A),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "展开控制栏",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "下滑或点击展开控制栏",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Expanded Control Bar
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                color = Color(0xF00F172A),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // LEFT TEAM CONTROLS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = matchState.leftTeamName,
                            color = TeamBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Left -1
                        FilledIconButton(
                            onClick = { onMinusScore(TeamSide.LEFT) },
                            enabled = matchState.leftScore > 0,
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.4f),
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "蓝方 -1", modifier = Modifier.size(18.dp))
                        }

                        // Left +1
                        FilledIconButton(
                            onClick = { onAddScore(TeamSide.LEFT) },
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = TeamBluePrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "蓝方 +1", modifier = Modifier.size(18.dp))
                        }
                    }

                    // CENTER DIVIDER & UTILITIES
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color(0xFF334155))
                    )

                    // Target badge
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${matchState.targetScore}分制",
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = onUndo,
                        enabled = matchState.history.isNotEmpty(),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "撤销",
                            tint = if (matchState.history.isNotEmpty()) Color.White else Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Swap Sides
                    IconButton(
                        onClick = onSwapSides,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "换边",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Reset
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "重置比分",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Exit
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "返回主菜单",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color(0xFF334155))
                    )

                    // RIGHT TEAM CONTROLS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Right +1
                        FilledIconButton(
                            onClick = { onAddScore(TeamSide.RIGHT) },
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = TeamRedPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "红方 +1", modifier = Modifier.size(18.dp))
                        }

                        // Right -1
                        FilledIconButton(
                            onClick = { onMinusScore(TeamSide.RIGHT) },
                            enabled = matchState.rightScore > 0,
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.4f),
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "红方 -1", modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = matchState.rightTeamName,
                            color = TeamRedPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

