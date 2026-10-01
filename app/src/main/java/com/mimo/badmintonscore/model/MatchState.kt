package com.mimo.badmintonscore.model

enum class TeamSide {
    LEFT,
    RIGHT
}

data class ScoreSnapshot(
    val leftScore: Int,
    val rightScore: Int,
    val servingSide: TeamSide
)

data class MatchState(
    val targetScore: Int = 15,
    val leftScore: Int = 0,
    val rightScore: Int = 0,
    val leftTeamName: String = "蓝方",
    val rightTeamName: String = "红方",
    val servingSide: TeamSide = TeamSide.LEFT,
    val history: List<ScoreSnapshot> = emptyList(),
    val winner: TeamSide? = null
) {
    val isGameOver: Boolean
        get() = winner != null

    val capScore: Int
        get() = if (targetScore == 15) 21 else 36

    val deuceScore: Int
        get() = targetScore - 1 // 14 or 30

    val isDeuce: Boolean
        get() = leftScore >= deuceScore && rightScore >= deuceScore && !isGameOver

    // 当前发球方的当前局分
    val currentServerScore: Int
        get() = if (servingSide == TeamSide.LEFT) leftScore else rightScore

    // 左单右双原则（0算双数）：分数为偶数（0, 2, 4...）在右半场发球，单数（1, 3, 5...）在左半场发球
    val isServingFromRightCourt: Boolean
        get() = currentServerScore % 2 == 0

    val servingCourtName: String
        get() = if (isServingFromRightCourt) "右半场发球" else "左半场发球"

    val servingCourtDetail: String
        get() = if (isServingFromRightCourt) "右半场 (双数${currentServerScore}分)" else "左半场 (单数${currentServerScore}分)"

    private fun checkWinner(score: Int, opponentScore: Int): Boolean {
        // 达到封顶分数（15分制为21分，31分制为36分），直接获胜
        if (score >= capScore) return true

        // 未达到目标分，未分胜负
        if (score < targetScore) return false

        // 若对方未达到加分平分线（如14平或30平之前），先到15/31直接胜出
        return if (opponentScore < deuceScore) {
            score >= targetScore
        } else {
            // 战至14平或30平后，必须净胜2分，或达到封顶
            (score - opponentScore >= 2) || (score >= capScore)
        }
    }

    fun addPoint(side: TeamSide): MatchState {
        if (isGameOver) return this
        val currentSnapshot = ScoreSnapshot(leftScore, rightScore, servingSide)
        val newHistory = history + currentSnapshot

        return if (side == TeamSide.LEFT) {
            val newScore = leftScore + 1
            val won = if (checkWinner(newScore, rightScore)) TeamSide.LEFT else null
            copy(
                leftScore = newScore,
                servingSide = TeamSide.LEFT,
                history = newHistory,
                winner = won
            )
        } else {
            val newScore = rightScore + 1
            val won = if (checkWinner(newScore, leftScore)) TeamSide.RIGHT else null
            copy(
                rightScore = newScore,
                servingSide = TeamSide.RIGHT,
                history = newHistory,
                winner = won
            )
        }
    }

    fun minusPoint(side: TeamSide): MatchState {
        val currentSnapshot = ScoreSnapshot(leftScore, rightScore, servingSide)
        return if (side == TeamSide.LEFT) {
            if (leftScore <= 0) return this
            val newScore = leftScore - 1
            val won = if (checkWinner(newScore, rightScore)) TeamSide.LEFT else null
            copy(
                leftScore = newScore,
                history = history + currentSnapshot,
                winner = won
            )
        } else {
            if (rightScore <= 0) return this
            val newScore = rightScore - 1
            val won = if (checkWinner(newScore, leftScore)) TeamSide.RIGHT else null
            copy(
                rightScore = newScore,
                history = history + currentSnapshot,
                winner = won
            )
        }
    }

    fun toggleServer(): MatchState {
        return copy(
            servingSide = if (servingSide == TeamSide.LEFT) TeamSide.RIGHT else TeamSide.LEFT
        )
    }

    fun undo(): MatchState {
        if (history.isEmpty()) return this
        val last = history.last()
        return copy(
            leftScore = last.leftScore,
            rightScore = last.rightScore,
            servingSide = last.servingSide,
            history = history.dropLast(1),
            winner = null
        )
    }

    fun swapSides(): MatchState {
        return copy(
            leftScore = rightScore,
            rightScore = leftScore,
            leftTeamName = rightTeamName,
            rightTeamName = leftTeamName,
            servingSide = if (servingSide == TeamSide.LEFT) TeamSide.RIGHT else TeamSide.LEFT,
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
            history = emptyList(),
            winner = null
        )
    }
}
