package com.mimo.badmintonscore.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mimo.badmintonscore.ui.theme.ServerGold
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary

@Composable
fun TopControlBar(
    isVisible: Boolean,
    matchState: MatchState,
    onAddScore: (TeamSide) -> Unit,
    onMinusScore: (TeamSide) -> Unit,
    onToggleServer: () -> Unit,
    onSwapSides: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onExit: () -> Unit,
    onExpandBar: () -> Unit,
    onCollapseBar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Top gray rectangle when collapsed
        AnimatedVisibility(
            visible = !isVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                    .clickable { onExpandBar() },
                color = Color(0xDD334155),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "展开控制栏",
                        tint = Color(0xFFF1F5F9),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "控制栏",
                        color = Color(0xFFF1F5F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 大比分简要徽章
                    Surface(
                        color = Color(0x660F172A),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = " 局分 ${matchState.leftGamesWon} | ${matchState.rightGamesWon} ",
                            color = Color(0xFFFACC15),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
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
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val leftTeamColor = if (!matchState.isSidesSwapped) TeamBluePrimary else TeamRedPrimary
                    val rightTeamColor = if (!matchState.isSidesSwapped) TeamRedPrimary else TeamBluePrimary

                    // LEFT TEAM CONTROLS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = matchState.leftTeamName,
                            color = leftTeamColor,
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
                            Icon(Icons.Default.Remove, contentDescription = "左方 -1", modifier = Modifier.size(18.dp))
                        }

                        // Left +1
                        FilledIconButton(
                            onClick = { onAddScore(TeamSide.LEFT) },
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = leftTeamColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "左方 +1", modifier = Modifier.size(18.dp))
                        }
                    }

                    // CENTER DIVIDER & UTILITIES
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color(0xFF334155))
                    )

                    // 大比分显示：在最中间，中间用竖线隔开，三局两胜
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${matchState.leftGamesWon}",
                                color = leftTeamColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "|",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = "${matchState.rightGamesWon}",
                                color = rightTeamColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (matchState.matchType == com.mimo.badmintonscore.model.MatchType.DOUBLES) "双打·第${matchState.currentGameIndex}局" else "第${matchState.currentGameIndex}局",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Target badge
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${matchState.targetScore}分制",
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Toggle server button
                    IconButton(
                        onClick = onToggleServer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.SportsTennis,
                            contentDescription = "切换发球方",
                            tint = ServerGold,
                            modifier = Modifier.size(20.dp)
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
                                containerColor = rightTeamColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "右方 +1", modifier = Modifier.size(18.dp))
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
                            Icon(Icons.Default.Remove, contentDescription = "右方 -1", modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = matchState.rightTeamName,
                            color = rightTeamColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Quick collapse button
                    IconButton(
                        onClick = onCollapseBar,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "收起控制栏",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
