package com.rewardpoints.app.rpg

import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RankCalculatorTest {
    private val calc = RankCalculator()

    @Test fun `checkRankUp returns Progress when below threshold`() {
        val stats = PlayerStats(rank = Rank.D, rankUpStreakCounter = 2)
        val result = calc.checkRankUp(stats)
        assertTrue(result is RankResult.Progress)
        val progress = result as RankResult.Progress
        // After this active day, counter would be 3, so 2 days remaining
        assertEquals(2, progress.daysToRankUp)
        assertEquals(3, progress.currentStreakCounter)
    }

    @Test fun `checkRankUp returns RankUp when hitting threshold`() {
        val stats = PlayerStats(rank = Rank.D, rankUpStreakCounter = Rank.STREAK_DAYS_TO_RANK_UP - 1)
        val result = calc.checkRankUp(stats)
        assertTrue(result is RankResult.RankUp)
        assertEquals(Rank.C, (result as RankResult.RankUp).newRank)
    }

    @Test fun `checkRankUp at S rank returns Progress (no next rank)`() {
        val stats = PlayerStats(rank = Rank.S, rankUpStreakCounter = Rank.STREAK_DAYS_TO_RANK_UP - 1)
        val result = calc.checkRankUp(stats)
        // At S, nextRank() is null so we get Progress, not RankUp
        assertTrue(result is RankResult.Progress)
    }

    @Test fun `getStreakDaysToNextRank clamps at zero`() {
        val stats = PlayerStats(rankUpStreakCounter = Rank.STREAK_DAYS_TO_RANK_UP + 5)
        assertEquals(0, calc.getStreakDaysToNextRank(stats))
    }

    @Test fun `Rank order is E to S ascending`() {
        assertEquals(0, Rank.E.order)
        assertEquals(5, Rank.S.order)
        assertEquals(Rank.D, Rank.E.nextRank())
        assertEquals(null, Rank.S.nextRank())
        assertEquals(null, Rank.E.previousRank())
        assertEquals(Rank.A, Rank.S.previousRank())
    }
}
