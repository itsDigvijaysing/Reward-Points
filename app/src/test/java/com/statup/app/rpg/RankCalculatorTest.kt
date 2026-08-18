package com.statup.app.rpg

import com.statup.app.domain.model.PlayerStats
import com.statup.app.domain.model.Rank
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The full rank transition state machine is tested in [RankLogicTest]. This file only
 * covers the small `getStreakDaysToNextRank` helper used by `StatusViewModel`.
 */
class RankCalculatorTest {
    private val calc = RankCalculator()

    @Test fun `getStreakDaysToNextRank reports remaining days`() {
        val stats = PlayerStats(rank = Rank.D, rankUpStreakCounter = 2)
        assertEquals(3, calc.getStreakDaysToNextRank(stats))
    }

    @Test fun `getStreakDaysToNextRank clamps at zero when over threshold`() {
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
