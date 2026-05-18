package com.rewardpoints.app.rpg

import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank

/**
 * Pure helpers for rank-up progression checks. The actual rank state transitions
 * are owned by [DecayEngine], which mutates rank via the star-line model on
 * [PlayerStats.rankUpStreakCounter]. The legacy two-counter model that referenced
 * a separate rank-down counter was removed in favor of that single counter.
 */
class RankCalculator {

    fun checkRankUp(stats: PlayerStats): RankResult {
        val streakCounter = stats.rankUpStreakCounter + 1
        val nextRank = stats.rank.nextRank()
        return if (streakCounter >= Rank.STREAK_DAYS_TO_RANK_UP && nextRank != null) {
            RankResult.RankUp(
                newRank = nextRank,
                message = "Congratulations! You've ranked up to ${nextRank.title}!"
            )
        } else {
            RankResult.Progress(
                daysToRankUp = Rank.STREAK_DAYS_TO_RANK_UP - streakCounter,
                currentStreakCounter = streakCounter
            )
        }
    }

    fun getStreakDaysToNextRank(stats: PlayerStats): Int =
        (Rank.STREAK_DAYS_TO_RANK_UP - stats.rankUpStreakCounter).coerceAtLeast(0)
}

sealed class RankResult {
    data class RankUp(val newRank: Rank, val message: String) : RankResult()
    data class Progress(val daysToRankUp: Int, val currentStreakCounter: Int) : RankResult()
}
