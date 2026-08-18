package dev.statup.app.rpg

import dev.statup.app.domain.model.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Black-box tests for the star-line counter model.
 *
 * Design (asymmetric on purpose):
 *   - +5 active days from counter 0 → rank up, counter resets to 0 at the new higher rank.
 *   - Counter dropping below 0 (i.e. to -1) → immediate rank down, counter resets to +5
 *     at the new lower rank.
 *   - At rank E (the floor) the counter clamps at 0.
 *   - The +5-after-demotion reset is a "near-miss" cushion: 1 active day at the lower rank
 *     bounces straight back up (counter +5 → +6, which is ≥ 5 → re-promote).
 *
 * Concrete user-described example covered below:
 *   At D with counter +1: 1 idle → 0, 2 idle → -1 → demote to E with counter +5.
 */
class RankLogicTest {

    @Test fun `5 active days from E_0 promotes to D with counter reset to 0`() {
        var counter = 0
        var rank = Rank.E
        repeat(Rank.STREAK_DAYS_TO_RANK_UP) {
            val t = RankLogic.applyActiveDay(counter, rank)
            when (t) {
                is RankLogic.Transition.RankUp -> {
                    rank = t.newRank
                    counter = t.newCounter
                }
                is RankLogic.Transition.CounterUpdated -> counter = t.newCounter
                else -> error("unexpected: $t")
            }
        }
        assertEquals(Rank.D, rank)
        assertEquals(0, counter)
    }

    @Test fun `at D with counter plus1, two idle days demote and reset counter to plus5`() {
        // Day 1 idle: +1 → 0, still at D
        val day1 = RankLogic.applyIdleDay(1, Rank.D)
        assertTrue(day1 is RankLogic.Transition.CounterUpdated)
        assertEquals(0, (day1 as RankLogic.Transition.CounterUpdated).newCounter)

        // Day 2 idle: 0 → -1 → demote to E, counter resets to +5
        val day2 = RankLogic.applyIdleDay(0, Rank.D)
        assertTrue(day2 is RankLogic.Transition.RankDown)
        val down = day2 as RankLogic.Transition.RankDown
        assertEquals(Rank.E, down.newRank)
        assertEquals(Rank.STREAK_DAYS_TO_RANK_UP, down.newCounter)
    }

    @Test fun `at D with counter 0 (just promoted), one idle day demotes immediately`() {
        // No grace period after promotion — single idle day drops you straight back.
        val t = RankLogic.applyIdleDay(0, Rank.D)
        assertTrue(t is RankLogic.Transition.RankDown)
        val down = t as RankLogic.Transition.RankDown
        assertEquals(Rank.E, down.newRank)
        assertEquals(Rank.STREAK_DAYS_TO_RANK_UP, down.newCounter)
    }

    @Test fun `after demotion to E with counter plus5, one active day re-promotes to D`() {
        // Counter +5 → +6 (≥ 5) → rank up back to D, counter resets to 0
        val t = RankLogic.applyActiveDay(Rank.STREAK_DAYS_TO_RANK_UP, Rank.E)
        assertTrue(t is RankLogic.Transition.RankUp)
        val up = t as RankLogic.Transition.RankUp
        assertEquals(Rank.D, up.newRank)
        assertEquals(0, up.newCounter)
    }

    @Test fun `at rank E with counter 0, idle days clamp the counter at 0`() {
        var counter = 0
        repeat(10) {
            val t = RankLogic.applyIdleDay(counter, Rank.E)
            assertTrue("E should clamp, never demote: $t", t is RankLogic.Transition.CounterUpdated)
            counter = (t as RankLogic.Transition.CounterUpdated).newCounter
            assertEquals(0, counter)
        }
    }

    @Test fun `at rank E with positive counter, idle days decrement until 0 then clamp`() {
        // From E with counter +3, idle days should go 3 → 2 → 1 → 0 → 0 → 0
        var counter = 3
        listOf(2, 1, 0, 0, 0).forEach { expected ->
            val t = RankLogic.applyIdleDay(counter, Rank.E)
            counter = (t as RankLogic.Transition.CounterUpdated).newCounter
            assertEquals(expected, counter)
        }
    }

    @Test fun `at rank S, active days update counter but cap at STREAK_DAYS_TO_RANK_UP`() {
        // No rank above S, so the counter just sits at the threshold instead of growing
        // unbounded. The counter still climbs from 0 → 5; subsequent active days no-op.
        var counter = 0
        repeat(8) {
            val t = RankLogic.applyActiveDay(counter, Rank.S)
            assertTrue("S is the cap: $t", t is RankLogic.Transition.CounterUpdated)
            counter = (t as RankLogic.Transition.CounterUpdated).newCounter
        }
        assertEquals(Rank.STREAK_DAYS_TO_RANK_UP, counter)
    }

    @Test fun `full cycle - promote E to D, then 2 idle from plus1 back to E`() {
        // 5 active days at E (counter 0): promote to D
        var counter = 0
        var rank = Rank.E
        repeat(5) {
            val t = RankLogic.applyActiveDay(counter, rank)
            when (t) {
                is RankLogic.Transition.RankUp -> { rank = t.newRank; counter = t.newCounter }
                is RankLogic.Transition.CounterUpdated -> counter = t.newCounter
                else -> error("unexpected: $t")
            }
        }
        assertEquals(Rank.D, rank)
        assertEquals(0, counter)

        // 1 active day at D to get counter to +1
        val active = RankLogic.applyActiveDay(counter, rank)
        counter = (active as RankLogic.Transition.CounterUpdated).newCounter
        assertEquals(1, counter)

        // 2 idle days: +1 → 0 (stay), 0 → -1 (demote)
        val idle1 = RankLogic.applyIdleDay(counter, rank)
        counter = (idle1 as RankLogic.Transition.CounterUpdated).newCounter
        assertEquals(0, counter)

        val idle2 = RankLogic.applyIdleDay(counter, rank)
        assertTrue(idle2 is RankLogic.Transition.RankDown)
        val down = idle2 as RankLogic.Transition.RankDown
        rank = down.newRank
        counter = down.newCounter
        assertEquals(Rank.E, rank)
        assertEquals(Rank.STREAK_DAYS_TO_RANK_UP, counter)
    }
}
