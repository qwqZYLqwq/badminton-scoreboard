package com.mimo.badmintonscore.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ActiveMatchRecord(
    val matchState: MatchState,
    val enableIntervalTimer: Boolean,
    val isServeAssistantEnabled: Boolean,
    val isServeDirectionReversed: Boolean,
    val isHapticEnabled: Boolean = true,
    val savedTimestamp: Long
)

object ActiveMatchRepository {
    private const val PREFS_NAME = "badminton_active_match_v1"
    private const val KEY_IS_ACTIVE = "key_is_active_match"
    private const val KEY_MATCH_JSON = "key_match_json"

    fun saveActiveMatch(
        context: Context,
        matchState: MatchState,
        enableIntervalTimer: Boolean,
        isServeAssistantEnabled: Boolean,
        isServeDirectionReversed: Boolean,
        isHapticEnabled: Boolean = true
    ) {
        if (matchState.isGameOver) {
            clearActiveMatch(context)
            return
        }

        try {
            val json = JSONObject().apply {
                put("targetScore", matchState.targetScore)
                put("leftScore", matchState.leftScore)
                put("rightScore", matchState.rightScore)
                put("leftGamesWon", matchState.leftGamesWon)
                put("rightGamesWon", matchState.rightGamesWon)
                put("currentGameIndex", matchState.currentGameIndex)
                put("leftTeamName", matchState.leftTeamName)
                put("rightTeamName", matchState.rightTeamName)
                put("matchType", matchState.matchType.name)
                put("servingSide", matchState.servingSide.name)
                put("isSidesSwapped", matchState.isSidesSwapped)

                // 双打站位
                put("leftPlayer1", matchState.leftPlayer1)
                put("leftPlayer2", matchState.leftPlayer2)
                put("rightPlayer1", matchState.rightPlayer1)
                put("rightPlayer2", matchState.rightPlayer2)
                put("leftRightCourtPlayer", matchState.leftRightCourtPlayer)
                put("leftLeftCourtPlayer", matchState.leftLeftCourtPlayer)
                put("rightRightCourtPlayer", matchState.rightRightCourtPlayer)
                put("rightLeftCourtPlayer", matchState.rightLeftCourtPlayer)
                put("currentServerPlayer", matchState.currentServerPlayer)
                put("currentReceiverPlayer", matchState.currentReceiverPlayer)

                // 暂停与局末
                put("hasTriggeredIntervalInCurrentGame", matchState.hasTriggeredIntervalInCurrentGame)
                put("pendingInterval", matchState.pendingInterval.name)
                put("courtSwapHint", matchState.courtSwapHint ?: "")
                put("currentSetWinner", matchState.currentSetWinner?.name ?: "")
                put("winner", matchState.winner?.name ?: "")

                // 外部配置
                put("enableIntervalTimer", enableIntervalTimer)
                put("isServeAssistantEnabled", isServeAssistantEnabled)
                put("isServeDirectionReversed", isServeDirectionReversed)
                put("isHapticEnabled", isHapticEnabled)
                put("savedTimestamp", System.currentTimeMillis())

                // 局历史 gameHistory
                val gameHistoryArray = JSONArray()
                matchState.gameHistory.forEach { gh ->
                    val ghObj = JSONObject().apply {
                        put("gameIndex", gh.gameIndex)
                        put("leftScore", gh.leftScore)
                        put("rightScore", gh.rightScore)
                        put("winner", gh.winner.name)
                    }
                    gameHistoryArray.put(ghObj)
                }
                put("gameHistory", gameHistoryArray)

                // 撤销快照 history
                val historyArray = JSONArray()
                matchState.history.forEach { snap ->
                    val snapObj = JSONObject().apply {
                        put("leftScore", snap.leftScore)
                        put("rightScore", snap.rightScore)
                        put("leftGamesWon", snap.leftGamesWon)
                        put("rightGamesWon", snap.rightGamesWon)
                        put("currentGameIndex", snap.currentGameIndex)
                        put("servingSide", snap.servingSide.name)
                        put("leftRightCourtPlayer", snap.leftRightCourtPlayer)
                        put("leftLeftCourtPlayer", snap.leftLeftCourtPlayer)
                        put("rightRightCourtPlayer", snap.rightRightCourtPlayer)
                        put("rightLeftCourtPlayer", snap.rightLeftCourtPlayer)
                        put("currentServerPlayer", snap.currentServerPlayer)
                        put("currentReceiverPlayer", snap.currentReceiverPlayer)
                        put("hasTriggeredIntervalInCurrentGame", snap.hasTriggeredIntervalInCurrentGame)
                        put("pendingInterval", snap.pendingInterval.name)
                        put("courtSwapHint", snap.courtSwapHint ?: "")
                        put("isSidesSwapped", snap.isSidesSwapped)
                    }
                    historyArray.put(snapObj)
                }
                put("history", historyArray)
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, true)
                .putString(KEY_MATCH_JSON, json.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveMatch(context: Context): ActiveMatchRecord? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_IS_ACTIVE, false)) return null
        val raw = prefs.getString(KEY_MATCH_JSON, null) ?: return null

        return try {
            val json = JSONObject(raw)

            val matchTypeStr = json.optString("matchType", MatchType.SINGLES.name)
            val matchType = try { MatchType.valueOf(matchTypeStr) } catch (e: Exception) { MatchType.SINGLES }

            val servingSideStr = json.optString("servingSide", TeamSide.LEFT.name)
            val servingSide = try { TeamSide.valueOf(servingSideStr) } catch (e: Exception) { TeamSide.LEFT }

            val pendingIntervalStr = json.optString("pendingInterval", IntervalType.NONE.name)
            val pendingInterval = try { IntervalType.valueOf(pendingIntervalStr) } catch (e: Exception) { IntervalType.NONE }

            val currentSetWinnerStr = json.optString("currentSetWinner", "")
            val currentSetWinner = if (currentSetWinnerStr.isNotBlank()) {
                try { TeamSide.valueOf(currentSetWinnerStr) } catch (e: Exception) { null }
            } else null

            val winnerStr = json.optString("winner", "")
            val winner = if (winnerStr.isNotBlank()) {
                try { TeamSide.valueOf(winnerStr) } catch (e: Exception) { null }
            } else null

            val courtSwapHint = json.optString("courtSwapHint", "").ifBlank { null }

            // 解析 gameHistory
            val gameHistoryList = mutableListOf<GameResult>()
            val gameHistoryArray = json.optJSONArray("gameHistory")
            if (gameHistoryArray != null) {
                for (i in 0 until gameHistoryArray.length()) {
                    val ghObj = gameHistoryArray.getJSONObject(i)
                    val ghWinner = try { TeamSide.valueOf(ghObj.getString("winner")) } catch (e: Exception) { TeamSide.LEFT }
                    gameHistoryList.add(
                        GameResult(
                            gameIndex = ghObj.optInt("gameIndex", 1),
                            leftScore = ghObj.optInt("leftScore", 0),
                            rightScore = ghObj.optInt("rightScore", 0),
                            winner = ghWinner
                        )
                    )
                }
            }

            // 解析 history 快照
            val historyList = mutableListOf<ScoreSnapshot>()
            val historyArray = json.optJSONArray("history")
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    val sObj = historyArray.getJSONObject(i)
                    val sServingSide = try { TeamSide.valueOf(sObj.getString("servingSide")) } catch (e: Exception) { TeamSide.LEFT }
                    val sPendingInterval = try { IntervalType.valueOf(sObj.optString("pendingInterval", IntervalType.NONE.name)) } catch (e: Exception) { IntervalType.NONE }
                    val sCourtSwapHint = sObj.optString("courtSwapHint", "").ifBlank { null }
                    historyList.add(
                        ScoreSnapshot(
                            leftScore = sObj.optInt("leftScore", 0),
                            rightScore = sObj.optInt("rightScore", 0),
                            leftGamesWon = sObj.optInt("leftGamesWon", 0),
                            rightGamesWon = sObj.optInt("rightGamesWon", 0),
                            currentGameIndex = sObj.optInt("currentGameIndex", 1),
                            servingSide = sServingSide,
                            leftRightCourtPlayer = sObj.optString("leftRightCourtPlayer", "蓝方一号"),
                            leftLeftCourtPlayer = sObj.optString("leftLeftCourtPlayer", "蓝方二号"),
                            rightRightCourtPlayer = sObj.optString("rightRightCourtPlayer", "红方一号"),
                            rightLeftCourtPlayer = sObj.optString("rightLeftCourtPlayer", "红方二号"),
                            currentServerPlayer = sObj.optString("currentServerPlayer", "蓝方一号"),
                            currentReceiverPlayer = sObj.optString("currentReceiverPlayer", "红方一号"),
                            hasTriggeredIntervalInCurrentGame = sObj.optBoolean("hasTriggeredIntervalInCurrentGame", false),
                            pendingInterval = sPendingInterval,
                            courtSwapHint = sCourtSwapHint,
                            isSidesSwapped = sObj.optBoolean("isSidesSwapped", false)
                        )
                    )
                }
            }

            val matchState = MatchState(
                targetScore = json.optInt("targetScore", 21),
                leftScore = json.optInt("leftScore", 0),
                rightScore = json.optInt("rightScore", 0),
                leftGamesWon = json.optInt("leftGamesWon", 0),
                rightGamesWon = json.optInt("rightGamesWon", 0),
                currentGameIndex = json.optInt("currentGameIndex", 1),
                leftTeamName = json.optString("leftTeamName", "蓝方"),
                rightTeamName = json.optString("rightTeamName", "红方"),
                matchType = matchType,
                servingSide = servingSide,
                isSidesSwapped = json.optBoolean("isSidesSwapped", false),
                leftPlayer1 = json.optString("leftPlayer1", "蓝方一号"),
                leftPlayer2 = json.optString("leftPlayer2", "蓝方二号"),
                rightPlayer1 = json.optString("rightPlayer1", "红方一号"),
                rightPlayer2 = json.optString("rightPlayer2", "红方二号"),
                leftRightCourtPlayer = json.optString("leftRightCourtPlayer", "蓝方一号"),
                leftLeftCourtPlayer = json.optString("leftLeftCourtPlayer", "蓝方二号"),
                rightRightCourtPlayer = json.optString("rightRightCourtPlayer", "红方一号"),
                rightLeftCourtPlayer = json.optString("rightLeftCourtPlayer", "红方二号"),
                currentServerPlayer = json.optString("currentServerPlayer", "蓝方一号"),
                currentReceiverPlayer = json.optString("currentReceiverPlayer", "红方一号"),
                hasTriggeredIntervalInCurrentGame = json.optBoolean("hasTriggeredIntervalInCurrentGame", false),
                pendingInterval = pendingInterval,
                courtSwapHint = courtSwapHint,
                gameHistory = gameHistoryList,
                history = historyList,
                currentSetWinner = currentSetWinner,
                winner = winner
            )

            ActiveMatchRecord(
                matchState = matchState,
                enableIntervalTimer = json.optBoolean("enableIntervalTimer", true),
                isServeAssistantEnabled = json.optBoolean("isServeAssistantEnabled", false),
                isServeDirectionReversed = json.optBoolean("isServeDirectionReversed", false),
                isHapticEnabled = json.optBoolean("isHapticEnabled", true),
                savedTimestamp = json.optLong("savedTimestamp", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearActiveMatch(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVE, false)
            .remove(KEY_MATCH_JSON)
            .apply()
    }
}
