package com.mimo.badmintonscore.model

enum class TeamSide {
    LEFT,
    RIGHT;

    fun opposite(): TeamSide = if (this == LEFT) RIGHT else LEFT
}

enum class MatchType {
    SINGLES,
    DOUBLES
}

enum class IntervalType {
    NONE,
    TECHNICAL_INTERVAL, // 局中技术暂停（11分或8分，休息60秒，决胜局交换场地）
    SET_BREAK           // 局间休息（120秒，双方交换场地，比分清零）
}

data class GameResult(
    val gameIndex: Int,
    val leftScore: Int,
    val rightScore: Int,
    val winner: TeamSide
)

data class ScoreSnapshot(
    val leftScore: Int,
    val rightScore: Int,
    val leftGamesWon: Int,
    val rightGamesWon: Int,
    val currentGameIndex: Int,
    val servingSide: TeamSide,
    val leftRightCourtPlayer: String,
    val leftLeftCourtPlayer: String,
    val rightRightCourtPlayer: String,
    val rightLeftCourtPlayer: String,
    val currentServerPlayer: String,
    val currentReceiverPlayer: String,
    val hasTriggeredIntervalInCurrentGame: Boolean,
    val pendingInterval: IntervalType = IntervalType.NONE,
    val courtSwapHint: String? = null,
    val isSidesSwapped: Boolean = false
)

data class MatchState(
    val targetScore: Int = 21,
    val leftScore: Int = 0,
    val rightScore: Int = 0,
    val leftGamesWon: Int = 0,
    val rightGamesWon: Int = 0,
    val currentGameIndex: Int = 1,
    val leftTeamName: String = "蓝方",
    val rightTeamName: String = "红方",
    val matchType: MatchType = MatchType.SINGLES,
    val servingSide: TeamSide = TeamSide.LEFT,
    val isSidesSwapped: Boolean = false,

    // 双打球员站位记录
    val leftPlayer1: String = "蓝方一号",
    val leftPlayer2: String = "蓝方二号",
    val rightPlayer1: String = "红方一号",
    val rightPlayer2: String = "红方二号",
    val leftRightCourtPlayer: String = leftPlayer1,
    val leftLeftCourtPlayer: String = leftPlayer2,
    val rightRightCourtPlayer: String = rightPlayer1,
    val rightLeftCourtPlayer: String = rightPlayer2,
    val currentServerPlayer: String = leftPlayer1,
    val currentReceiverPlayer: String = rightPlayer1,

    // 暂停与换边状态
    val hasTriggeredIntervalInCurrentGame: Boolean = false,
    val pendingInterval: IntervalType = IntervalType.NONE,
    val courtSwapHint: String? = null,

    // 历史局分记录与撤销快照
    val gameHistory: List<GameResult> = emptyList(),
    val history: List<ScoreSnapshot> = emptyList(),
    val currentSetWinner: TeamSide? = null,
    val winner: TeamSide? = null // 全场比赛胜者（达到2局胜）
) {
    val isGameOver: Boolean
        get() = winner != null

    // 21分制封顶30分，15分制封顶21分
    val capScore: Int
        get() = if (targetScore == 15) 21 else 30

    // 加分赛门槛：15分制为14平，21分制为20平
    val deuceScore: Int
        get() = if (targetScore == 15) 14 else 20

    // 技术暂停分数线：15分制为8分，21分制为11分
    val intervalScore: Int
        get() = if (targetScore == 15) 8 else 11

    val isDeuce: Boolean
        get() = leftScore >= deuceScore && rightScore >= deuceScore && !isGameOver && currentSetWinner == null

    // 是否为决胜局（第三局，或前两局1:1平）
    val isDecidingGame: Boolean
        get() = currentGameIndex == 3 || (leftGamesWon == 1 && rightGamesWon == 1)

    // 当前发球方当前局的分数
    val currentServerScore: Int
        get() = if (servingSide == TeamSide.LEFT) leftScore else rightScore

    // 左单右双原则（0算双数）：分数为偶数在右半场发球，单数在左半场发球
    val isServingFromRightCourt: Boolean
        get() = currentServerScore % 2 == 0

    val servingCourtName: String
        get() = if (isServingFromRightCourt) "右半场发球" else "左半场发球"

    val servingCourtDetail: String
        get() = if (isServingFromRightCourt) "右半场 (双数${currentServerScore}分)" else "左半场 (单数${currentServerScore}分)"

    /**
     * 判断单局是否获胜
     * 21分制：20平需领先2分，29:29平时先到30分者胜该局。
     * 15分制：14平需领先2分，20:20平时先到21分者胜该局。
     */
    fun checkSetWinner(score: Int, opponentScore: Int): Boolean {
        if (score >= capScore) return true
        if (score < targetScore) return false
        return if (opponentScore < deuceScore) {
            score >= targetScore
        } else {
            (score - opponentScore >= 2) || (score >= capScore)
        }
    }

    private fun createSnapshot(): ScoreSnapshot {
        return ScoreSnapshot(
            leftScore = leftScore,
            rightScore = rightScore,
            leftGamesWon = leftGamesWon,
            rightGamesWon = rightGamesWon,
            currentGameIndex = currentGameIndex,
            servingSide = servingSide,
            leftRightCourtPlayer = leftRightCourtPlayer,
            leftLeftCourtPlayer = leftLeftCourtPlayer,
            rightRightCourtPlayer = rightRightCourtPlayer,
            rightLeftCourtPlayer = rightLeftCourtPlayer,
            currentServerPlayer = currentServerPlayer,
            currentReceiverPlayer = currentReceiverPlayer,
            hasTriggeredIntervalInCurrentGame = hasTriggeredIntervalInCurrentGame,
            pendingInterval = pendingInterval,
            courtSwapHint = courtSwapHint,
            isSidesSwapped = isSidesSwapped
        )
    }

    /**
     * 计分增加一分
     * 支持单打与双打规则：
     * 双打规则：只有发球方得分时，发球员才会换边；队友位置不变。接发球方得分时所有站位不变。
     */
    fun addPoint(scorerSide: TeamSide): MatchState {
        if (isGameOver || currentSetWinner != null || pendingInterval != IntervalType.NONE) return this
        val snapshot = createSnapshot()
        val newHistory = history + snapshot

        val newLeftScore = if (scorerSide == TeamSide.LEFT) leftScore + 1 else leftScore
        val newRightScore = if (scorerSide == TeamSide.RIGHT) rightScore + 1 else rightScore
        val scorerScore = if (scorerSide == TeamSide.LEFT) newLeftScore else newRightScore
        val opponentScore = if (scorerSide == TeamSide.LEFT) newRightScore else newLeftScore

        // 计算双打站位变动
        var newLeftRightPlayer = leftRightCourtPlayer
        var newLeftLeftPlayer = leftLeftCourtPlayer
        var newRightRightPlayer = rightRightCourtPlayer
        var newRightLeftPlayer = rightLeftCourtPlayer
        var newServerPlayer = currentServerPlayer
        var newReceiverPlayer = currentReceiverPlayer

        if (matchType == MatchType.DOUBLES) {
            if (scorerSide == servingSide) {
                // 发球方得分：发球员与队友换边，继续发球；接发球方位置不变
                if (scorerSide == TeamSide.LEFT) {
                    val temp = newLeftRightPlayer
                    newLeftRightPlayer = newLeftLeftPlayer
                    newLeftLeftPlayer = temp
                    // 发球员依然是这个人
                } else {
                    val temp = newRightRightPlayer
                    newRightRightPlayer = newRightLeftPlayer
                    newRightLeftPlayer = temp
                }
                // 接发球员为对方与新发球区处于对角的球员
                val serverInRightCourt = (scorerScore % 2 == 0)
                newReceiverPlayer = if (scorerSide == TeamSide.LEFT) {
                    if (serverInRightCourt) newRightRightPlayer else newRightLeftPlayer
                } else {
                    if (serverInRightCourt) newLeftRightPlayer else newLeftLeftPlayer
                }
            } else {
                // 接发球方得分（换发球权）：双方站位均不改变！由接发球方根据其总分确定发球员
                val nextServerInRightCourt = (scorerScore % 2 == 0)
                if (scorerSide == TeamSide.LEFT) {
                    newServerPlayer = if (nextServerInRightCourt) newLeftRightPlayer else newLeftLeftPlayer
                    newReceiverPlayer = if (nextServerInRightCourt) newRightRightPlayer else newRightLeftPlayer
                } else {
                    newServerPlayer = if (nextServerInRightCourt) newRightRightPlayer else newRightLeftPlayer
                    newReceiverPlayer = if (nextServerInRightCourt) newLeftRightPlayer else newLeftLeftPlayer
                }
            }
        }

        // 1. 检查当局是否决出胜负
        if (checkSetWinner(scorerScore, opponentScore)) {
            val setWinner = scorerSide
            val newLeftGamesWon = if (setWinner == TeamSide.LEFT) leftGamesWon + 1 else leftGamesWon
            val newRightGamesWon = if (setWinner == TeamSide.RIGHT) rightGamesWon + 1 else rightGamesWon
            val isMatchWon = newLeftGamesWon >= 2 || newRightGamesWon >= 2
            val gameResult = GameResult(
                gameIndex = currentGameIndex,
                leftScore = newLeftScore,
                rightScore = newRightScore,
                winner = setWinner
            )

            return if (isMatchWon) {
                // 全场比赛结束！
                copy(
                    leftScore = newLeftScore,
                    rightScore = newRightScore,
                    leftGamesWon = newLeftGamesWon,
                    rightGamesWon = newRightGamesWon,
                    gameHistory = gameHistory + gameResult,
                    history = newHistory,
                    currentSetWinner = setWinner,
                    winner = setWinner,
                    pendingInterval = IntervalType.NONE,
                    courtSwapHint = null
                )
            } else {
                // 单局结束，进入局间休息120秒，并提示换边，准备下一局
                copy(
                    leftScore = newLeftScore,
                    rightScore = newRightScore,
                    leftGamesWon = newLeftGamesWon,
                    rightGamesWon = newRightGamesWon,
                    gameHistory = gameHistory + gameResult,
                    history = newHistory,
                    currentSetWinner = setWinner,
                    pendingInterval = IntervalType.SET_BREAK,
                    courtSwapHint = "第 ${currentGameIndex} 局结束，双方交换场地！"
                )
            }
        }

        // 2. 检查是否触发局中技术暂停（11分或8分，每局一次）
        var intervalTriggered = false
        var nextPendingInterval = pendingInterval
        var swapHint: String? = null

        val leaderScore = maxOf(newLeftScore, newRightScore)
        if (!hasTriggeredIntervalInCurrentGame && leaderScore >= intervalScore) {
            intervalTriggered = true
            nextPendingInterval = IntervalType.TECHNICAL_INTERVAL
            if (isDecidingGame) {
                swapHint = "决胜局达到 ${intervalScore} 分，双方交换场地！"
            }
        }

        return copy(
            leftScore = newLeftScore,
            rightScore = newRightScore,
            servingSide = scorerSide,
            leftRightCourtPlayer = newLeftRightPlayer,
            leftLeftCourtPlayer = newLeftLeftPlayer,
            rightRightCourtPlayer = newRightRightPlayer,
            rightLeftCourtPlayer = newRightLeftPlayer,
            currentServerPlayer = newServerPlayer,
            currentReceiverPlayer = newReceiverPlayer,
            hasTriggeredIntervalInCurrentGame = hasTriggeredIntervalInCurrentGame || intervalTriggered,
            pendingInterval = nextPendingInterval,
            courtSwapHint = swapHint,
            history = newHistory
        )
    }

    /**
     * 减分（用于误触修正）
     */
    fun minusPoint(side: TeamSide): MatchState {
        val snapshot = createSnapshot()
        return if (side == TeamSide.LEFT) {
            if (leftScore <= 0) return this
            copy(
                leftScore = leftScore - 1,
                history = history + snapshot,
                currentSetWinner = null,
                winner = null,
                pendingInterval = IntervalType.NONE,
                courtSwapHint = null
            )
        } else {
            if (rightScore <= 0) return this
            copy(
                rightScore = rightScore - 1,
                history = history + snapshot,
                currentSetWinner = null,
                winner = null,
                pendingInterval = IntervalType.NONE,
                courtSwapHint = null
            )
        }
    }

    /**
     * 局间休息或决胜局暂停结束时，完成换边与新一局初始化
     */
    fun proceedToNextGameOrSwap(): MatchState {
        if (pendingInterval == IntervalType.SET_BREAK) {
            // 局间休息结束：进入下一局，双方场地互换（换边），双方局分清零，重置当局暂停标记
            return copy(
                leftScore = 0,
                rightScore = 0,
                currentGameIndex = currentGameIndex + 1,
                leftTeamName = rightTeamName,
                rightTeamName = leftTeamName,
                leftGamesWon = rightGamesWon,
                rightGamesWon = leftGamesWon,
                isSidesSwapped = !isSidesSwapped,
                // 双打队员归属也互换
                leftPlayer1 = rightPlayer1,
                leftPlayer2 = rightPlayer2,
                rightPlayer1 = leftPlayer1,
                rightPlayer2 = leftPlayer2,
                leftRightCourtPlayer = rightRightCourtPlayer,
                leftLeftCourtPlayer = rightLeftCourtPlayer,
                rightRightCourtPlayer = leftRightCourtPlayer,
                rightLeftCourtPlayer = leftLeftCourtPlayer,
                servingSide = servingSide.opposite(),
                currentSetWinner = null,
                hasTriggeredIntervalInCurrentGame = false,
                pendingInterval = IntervalType.NONE,
                courtSwapHint = null,
                history = emptyList()
            )
        } else if (pendingInterval == IntervalType.TECHNICAL_INTERVAL) {
            // 决胜局技术暂停换边：保持当前比分，场地左右互换
            if (isDecidingGame) {
                return copy(
                    leftScore = rightScore,
                    rightScore = leftScore,
                    leftTeamName = rightTeamName,
                    rightTeamName = leftTeamName,
                    leftGamesWon = rightGamesWon,
                    rightGamesWon = leftGamesWon,
                    isSidesSwapped = !isSidesSwapped,
                    leftPlayer1 = rightPlayer1,
                    leftPlayer2 = rightPlayer2,
                    rightPlayer1 = leftPlayer1,
                    rightPlayer2 = leftPlayer2,
                    leftRightCourtPlayer = rightRightCourtPlayer,
                    leftLeftCourtPlayer = rightLeftCourtPlayer,
                    rightRightCourtPlayer = leftRightCourtPlayer,
                    rightLeftCourtPlayer = leftLeftCourtPlayer,
                    servingSide = servingSide.opposite(),
                    pendingInterval = IntervalType.NONE,
                    courtSwapHint = null
                )
            } else {
                return copy(
                    pendingInterval = IntervalType.NONE,
                    courtSwapHint = null
                )
            }
        }
        return copy(pendingInterval = IntervalType.NONE, courtSwapHint = null)
    }

    fun dismissInterval(): MatchState {
        return proceedToNextGameOrSwap()
    }

    fun toggleServer(): MatchState {
        return copy(
            servingSide = servingSide.opposite()
        )
    }

    fun undo(): MatchState {
        if (history.isEmpty()) return this
        val last = history.last()
        return copy(
            leftScore = last.leftScore,
            rightScore = last.rightScore,
            leftGamesWon = last.leftGamesWon,
            rightGamesWon = last.rightGamesWon,
            currentGameIndex = last.currentGameIndex,
            servingSide = last.servingSide,
            leftRightCourtPlayer = last.leftRightCourtPlayer,
            leftLeftCourtPlayer = last.leftLeftCourtPlayer,
            rightRightCourtPlayer = last.rightRightCourtPlayer,
            rightLeftCourtPlayer = last.rightLeftCourtPlayer,
            currentServerPlayer = last.currentServerPlayer,
            currentReceiverPlayer = last.currentReceiverPlayer,
            hasTriggeredIntervalInCurrentGame = last.hasTriggeredIntervalInCurrentGame,
            pendingInterval = last.pendingInterval,
            courtSwapHint = last.courtSwapHint,
            isSidesSwapped = last.isSidesSwapped,
            history = history.dropLast(1),
            currentSetWinner = null,
            winner = null
        )
    }

    fun swapSides(): MatchState {
        return copy(
            leftScore = rightScore,
            rightScore = leftScore,
            leftTeamName = rightTeamName,
            rightTeamName = leftTeamName,
            leftGamesWon = rightGamesWon,
            rightGamesWon = leftGamesWon,
            isSidesSwapped = !isSidesSwapped,
            leftPlayer1 = rightPlayer1,
            leftPlayer2 = rightPlayer2,
            rightPlayer1 = leftPlayer1,
            rightPlayer2 = leftPlayer2,
            leftRightCourtPlayer = rightRightCourtPlayer,
            leftLeftCourtPlayer = rightLeftCourtPlayer,
            rightRightCourtPlayer = leftRightCourtPlayer,
            rightLeftCourtPlayer = leftLeftCourtPlayer,
            servingSide = servingSide.opposite(),
            history = emptyList(),
            winner = when (winner) {
                TeamSide.LEFT -> TeamSide.RIGHT
                TeamSide.RIGHT -> TeamSide.LEFT
                null -> null
            }
        )
    }

    fun reset(): MatchState {
        return copy(
            leftScore = 0,
            rightScore = 0,
            leftGamesWon = 0,
            rightGamesWon = 0,
            currentGameIndex = 1,
            isSidesSwapped = false,
            hasTriggeredIntervalInCurrentGame = false,
            pendingInterval = IntervalType.NONE,
            courtSwapHint = null,
            gameHistory = emptyList(),
            history = emptyList(),
            currentSetWinner = null,
            winner = null
        )
    }
}
