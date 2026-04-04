package com.rewardpoints.app.rpg

import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank

class RankCalculator {

    fun checkRankUp(stats: PlayerStats): RankResult {
        val currentRank = stats.rank
        val streakCounter = stats.rankUpStreakCounter + 1

        return if (streakCounter >= Rank.STREAK_DAYS_TO_RANK_UP && currentRank.canRankUp()) {
            RankResult.RankUp(
                newRank = currentRank.nextRank()!!,
                message = "Congratulations! You've ranked up to ${currentRank.nextRank()!!.title}!"
            )
        } else {
            RankResult.Progress(
                daysToRankUp = Rank.STREAK_DAYS_TO_RANK_UP - streakCounter,
                currentStreakCounter = streakCounter
            )
        }
    }

    fun checkRankDown(stats: PlayerStats): RankResult {
        val currentRank = stats.rank
        val breakCounter = stats.rankDownBreakCounter + 1

        return if (breakCounter >= Rank.BREAKS_TO_RANK_DOWN && currentRank.canRankDown()) {
            RankResult.RankDown(
                newRank = currentRank.previousRank()!!,
                message = "Your rank has degraded to ${currentRank.previousRank()!!.title}. Get back on track!"
            )
        } else {
            RankResult.Warning(
                breaksToRankDown = Rank.BREAKS_TO_RANK_DOWN - breakCounter,
                currentBreakCounter = breakCounter
            )
        }
    }

    fun getStreakDaysToNextRank(stats: PlayerStats): Int {
        return (Rank.STREAK_DAYS_TO_RANK_UP - stats.rankUpStreakCounter).coerceAtLeast(0)
    }

    fun getBreaksToRankDown(stats: PlayerStats): Int {
        return (Rank.BREAKS_TO_RANK_DOWN - stats.rankDownBreakCounter).coerceAtLeast(0)
    }
}

sealed class RankResult {
    data class RankUp(val newRank: Rank, val message: String) : RankResult()
    data class RankDown(val newRank: Rank, val message: String) : RankResult()
    data class Progress(val daysToRankUp: Int, val currentStreakCounter: Int) : RankResult()
    data class Warning(val breaksToRankDown: Int, val currentBreakCounter: Int) : RankResult()
}
