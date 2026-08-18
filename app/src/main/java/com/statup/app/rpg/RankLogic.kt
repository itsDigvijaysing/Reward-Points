package com.statup.app.rpg

import com.statup.app.domain.model.Rank

/**
 * Pure functions for the star-line counter model. Extracted from [DecayEngine] so the
 * transition rules are testable without all the DB / preferences plumbing.
 *
 * Counter mental model (asymmetric by design):
 *   - Each active day: counter `+1`.
 *   - Each idle day: counter `-1`.
 *   - Counter `>= +STREAK_DAYS_TO_RANK_UP` → rank up, counter resets to `0` at the new rank.
 *     Going up takes 5 hard days of work.
 *   - Counter `< 0` (i.e. dropped to `-1`) → rank down immediately (if not already at floor),
 *     counter resets to `+STREAK_DAYS_TO_RANK_UP` at the lower rank. The +5 reset is a
 *     "near-miss" cushion: one active day at the lower rank (counter `+5 → +6 ≥ 5`) bounces
 *     you straight back up.
 *   - At rank E (no previous rank) the counter floors at `0` — no infinite negative
 *     accumulation, so the first active day always starts climbing toward D.
 *
 * Concrete walkthrough (matches the documented design):
 *   - At D with counter `+1`: 1 idle → 0 (still D), 2 idle → -1 → demote to E with counter +5.
 *   - At D with counter `0` (just promoted): 1 idle → -1 → demote to E with counter +5.
 *   - At E with counter `+5` (just demoted): 1 active → counter +6 ≥ 5 → re-promote to D.
 */
object RankLogic {

    sealed class Transition {
        data class CounterUpdated(val newCounter: Int) : Transition()
        data class RankUp(val newRank: Rank) : Transition() {
            val newCounter: Int = 0
        }
        data class RankDown(val newRank: Rank) : Transition() {
            val newCounter: Int = Rank.STREAK_DAYS_TO_RANK_UP
        }
    }

    fun applyActiveDay(currentCounter: Int, currentRank: Rank): Transition {
        val next = currentCounter + 1
        val nextRank = currentRank.nextRank()
        return if (next >= Rank.STREAK_DAYS_TO_RANK_UP && nextRank != null) {
            Transition.RankUp(nextRank)
        } else {
            // At rank S (nextRank == null) the counter has nowhere to promote to. Cap it
            // at the threshold so it doesn't grow unbounded across years of S-tier play.
            val capped = if (nextRank == null) {
                next.coerceAtMost(Rank.STREAK_DAYS_TO_RANK_UP)
            } else next
            Transition.CounterUpdated(capped)
        }
    }

    fun applyIdleDay(currentCounter: Int, currentRank: Rank): Transition {
        val next = currentCounter - 1
        val prevRank = currentRank.previousRank()
        return when {
            next < 0 && prevRank != null ->
                Transition.RankDown(prevRank)
            prevRank == null && next < 0 ->
                // At rank E: clamp at 0 since there's no rank below to fall to.
                Transition.CounterUpdated(0)
            else ->
                Transition.CounterUpdated(next)
        }
    }
}
