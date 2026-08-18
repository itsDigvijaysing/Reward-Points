package com.rewardpoints.app.rpg

import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank

/**
 * Pure helper for "how many active days until the next rank up". The actual rank state
 * transitions live in [RankLogic] (called from [DecayEngine]); this class only exists
 * for `StatusViewModel` to render the progress UI without duplicating threshold logic.
 *
 * `checkRankUp` and the `RankResult` sealed class used to live here too but were never
 * read by `DecayEngine` (which inlined its own logic and now delegates to `RankLogic`),
 * so they were removed.
 */
class RankCalculator {

    fun getStreakDaysToNextRank(stats: PlayerStats): Int =
        (Rank.STREAK_DAYS_TO_RANK_UP - stats.rankUpStreakCounter).coerceAtLeast(0)
}
