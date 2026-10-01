package com.mimo.badmintonscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mimo.badmintonscore.model.MatchState
import com.mimo.badmintonscore.model.TeamSide
import com.mimo.badmintonscore.ui.theme.TeamBluePrimary
import com.mimo.badmintonscore.ui.theme.TeamRedPrimary

@Composable
fun WinnerDialog(
    matchState: MatchState,
    onRestart: () -> Unit,
    onSwapAndRestart: () -> Unit,
    onBackToHome: () -> Unit
) {
    if (matchState.winner == null) return

    val isLeftWinner = matchState.winner == TeamSide.LEFT
    val winnerName = if (isLeftWinner) matchState.leftTeamName else matchState.rightTeamName
    val winnerColor = if (isLeftWinner) TeamBluePrimary else TeamRedPrimary

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "\u6BD4\u8D5B\u7ED3\u675F\uFF01",
                    fontSize = 18.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "\uD83C\uDF89 " + winnerName + " \u83B7\u80DC\uFF01",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = winnerColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeamBluePrimary
                        )
                        Text(
                            text = "  :  ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeamRedPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFCBD5E1)
                        )
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u4E3B\u83DC\u5355", fontSize = 14.sp)
                    }

                    FilledTonalButton(
                        onClick = onSwapAndRestart,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u6362\u8FB9\u518D\u6218", fontSize = 14.sp)
                    }

                    Button(
                        onClick = onRestart,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u518D\u6765\u4E00\u5C40", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}