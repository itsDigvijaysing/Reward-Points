package com.statup.app.rpg

import com.statup.app.domain.model.PlayerStats
import com.statup.app.domain.model.Rank

/**
 * Narrow read/write surface that [DecayEngine] needs from the player-stats store. Implemented by
 * [com.statup.app.data.repository.PlayerRepository]; depending on this interface (instead of
 * the concrete repository) lets DecayEngine be unit-tested on the JVM with a hand-written fake —
 * no Android, no Room.
 */
interface DecayStatsStore {
    suspend fun getStatsOnce(): PlayerStats?
    suspend fun updateStats(stats: PlayerStats)
    suspend fun updateStreak(streak: Int)
    suspend fun updateRank(rank: Rank)
    suspend fun updateRankUpCounter(counter: Int)
}

/** Persisted "last decay applied" local-day marker. Implemented by UserPreferences (DataStore). */
interface DecayDayStore {
    suspend fun getLastDecayDay(): String?
    suspend fun setLastDecayDay(day: String)
}

/**
 * Runs [block] inside a single DB transaction so the daily tick's read-modify-write of the
 * singleton player_stats row is atomic with concurrent earn / redeem / buy-shield transactions.
 * Without this, a late-firing decay (e.g. 02:15 while the user is earning) could clobber a
 * just-purchased Streak Shield or freshly-earned stat points with its stale full-row write.
 *
 * Implemented in production by [com.statup.app.data.local.db.RoomTransactor]; JVM tests use
 * a pass-through fake.
 */
interface Transactor {
    suspend fun <R> transaction(block: suspend () -> R): R
}
