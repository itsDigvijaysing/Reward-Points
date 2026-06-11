package com.rewardpoints.app.rpg

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.dao.DecayLogDao
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.local.db.entity.DecayLogEntity
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import com.rewardpoints.app.widget.StatsWidgetUpdater
import java.time.LocalDate
import java.util.Calendar

class DecayEngine(
    private val playerRepository: PlayerRepository,
    private val decayLogDao: DecayLogDao,
    private val userPreferences: UserPreferences,
    private val transactionDao: TransactionDao? = null,
    private val achievementTracker: AchievementTracker? = null,
    private val widgetUpdater: StatsWidgetUpdater? = null
) {
    /**
     * Called at midnight by DecayWorker.
     * Checks if any tasks/points were earned today.
     * If yes: recordSuccessfulDay()
     * If no: applyDecay()
     *
     * Idempotent within a local day — returns [DailyDecayResult.AlreadyApplied] if today's
     * boundary was already processed. Guards against WorkManager retries, manual `runNow`
     * calls during the same day, and overlapping schedules.
     */
    suspend fun applyDailyDecay(): DailyDecayResult {
        val today = LocalDate.now().toString() // yyyy-MM-dd in local zone
        val lastRun = userPreferences.getLastDecayDay()
        if (lastRun == today) {
            return DailyDecayResult.AlreadyApplied
        }

        // Snapshot today's midnight once. Yesterday's window is [todayMidnight - 24h, todayMidnight).
        // This is robust to WorkManager firing late: even at 02:15 the boundary stays correct.
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayMidnight = todayMidnight - 24L * 60L * 60L * 1000L

        // Check if any EARN transactions happened yesterday
        val earnedYesterday = transactionDao?.getEarnedInRange(yesterdayMidnight, todayMidnight) ?: 0

        val dailyResult = if (earnedYesterday > 0) {
            // User was active, record success
            when (val result = recordSuccessfulDay()) {
                is StreakResult.StreakWithRankUp -> DailyDecayResult.ActiveWithRankUp(result.newRank)
                is StreakResult.StreakContinued -> DailyDecayResult.ActiveDay(result.newStreak)
                else -> DailyDecayResult.ActiveDay(0)
            }
        } else {
            // User was idle. A Streak Freeze Shield (if owned) absorbs the idle day as a
            // "rest day": consume one shield, leave stats / streak / star-line counter
            // untouched. Otherwise decay applies as usual.
            val stats = playerRepository.getStatsOnce()
            if (stats != null && stats.streakShields > 0) {
                playerRepository.updateStats(
                    stats.copy(
                        streakShields = stats.streakShields - 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                DailyDecayResult.ShieldConsumed(shieldsLeft = stats.streakShields - 1)
            } else {
                when (val result = applyDecay("daily_idle")) {
                    is DecayResult.DecayWithRankDown -> DailyDecayResult.IdleWithRankDown(result.newRank)
                    is DecayResult.DecayApplied -> DailyDecayResult.IdleDay(result.statsLost)
                    else -> DailyDecayResult.IdleDay(0)
                }
            }
        }

        // Update streak/rank achievements
        achievementTracker?.onStreakUpdated()

        // Push fresh state to any home-screen widgets (rank/streak/balance may have changed).
        widgetUpdater?.refresh()

        userPreferences.setLastDecayDay(today)
        return dailyResult
    }

    suspend fun applyDecay(reason: String = "daily_idle"): DecayResult {
        val stats = playerRepository.getStatsOnce() ?: return DecayResult.NoStats

        // Stat loss: 1 point from each stat above BASE_STAT, floored there.
        val strLost = if (stats.strStat > PlayerStats.BASE_STAT) 1 else 0
        val intLost = if (stats.intStat > PlayerStats.BASE_STAT) 1 else 0
        val wisLost = if (stats.wisStat > PlayerStats.BASE_STAT) 1 else 0
        val dexLost = if (stats.dexStat > PlayerStats.BASE_STAT) 1 else 0
        val chaLost = if (stats.chaStat > PlayerStats.BASE_STAT) 1 else 0
        val vitLost = if (stats.vitStat > PlayerStats.BASE_STAT) 1 else 0
        val totalLost = strLost + intLost + wisLost + dexLost + chaLost + vitLost

        // Rank-state transition: delegate to RankLogic so the threshold/reset rules are
        // owned by a single tested module. See RankLogicTest for the full truth table.
        val transition = RankLogic.applyIdleDay(stats.rankUpStreakCounter, stats.rank)

        val newRank = when (transition) {
            is RankLogic.Transition.RankDown -> transition.newRank
            else -> stats.rank
        }
        val newCounter = when (transition) {
            is RankLogic.Transition.RankDown -> transition.newCounter
            is RankLogic.Transition.CounterUpdated -> transition.newCounter
            // applyIdleDay can only return RankDown or CounterUpdated — fail fast if that
            // invariant ever breaks rather than silently keeping a stale counter.
            is RankLogic.Transition.RankUp ->
                error("RankLogic.applyIdleDay returned RankUp — impossible on the idle-day path")
        }

        val updatedStats = stats.copy(
            strStat = stats.strStat - strLost,
            intStat = stats.intStat - intLost,
            wisStat = stats.wisStat - wisLost,
            dexStat = stats.dexStat - dexLost,
            chaStat = stats.chaStat - chaLost,
            vitStat = stats.vitStat - vitLost,
            streak = 0,
            rankUpStreakCounter = newCounter,
            rank = newRank,
            updatedAt = System.currentTimeMillis()
        )
        playerRepository.updateStats(updatedStats)

        if (totalLost > 0) {
            decayLogDao.insert(
                DecayLogEntity(
                    strLost = strLost, intLost = intLost, wisLost = wisLost,
                    dexLost = dexLost, chaLost = chaLost, vitLost = vitLost,
                    idleHours = null, reason = reason,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        return when (transition) {
            is RankLogic.Transition.RankDown ->
                DecayResult.DecayWithRankDown(
                    statsLost = totalLost,
                    newRank = newRank,
                    breakCounter = newCounter
                )
            is RankLogic.Transition.CounterUpdated -> if (totalLost > 0) {
                // breaksToRankDown = how many more idle days until counter drops below 0.
                DecayResult.DecayApplied(
                    statsLost = totalLost,
                    breakCounter = newCounter,
                    breaksToRankDown = newCounter + 1
                )
            } else DecayResult.NoDecay
            // Unreachable — the `newCounter` when above already failed fast on RankUp.
            is RankLogic.Transition.RankUp ->
                error("RankLogic.applyIdleDay returned RankUp — impossible on the idle-day path")
        }
    }

    suspend fun recordSuccessfulDay(): StreakResult {
        val stats = playerRepository.getStatsOnce() ?: return StreakResult.NoStats

        val newStreak = stats.streak + 1
        playerRepository.updateStreak(newStreak)

        // Delegate rank decision to RankLogic.
        val transition = RankLogic.applyActiveDay(stats.rankUpStreakCounter, stats.rank)
        return when (transition) {
            is RankLogic.Transition.RankUp -> {
                // updateRank resets rankUpStreakCounter to 0 in its SQL.
                playerRepository.updateRank(transition.newRank)
                StreakResult.StreakWithRankUp(
                    newStreak = newStreak,
                    newRank = transition.newRank
                )
            }
            is RankLogic.Transition.CounterUpdated -> {
                playerRepository.updateRankUpCounter(transition.newCounter)
                StreakResult.StreakContinued(
                    newStreak = newStreak,
                    daysToRankUp = (Rank.STREAK_DAYS_TO_RANK_UP - transition.newCounter)
                        .coerceAtLeast(0)
                )
            }
            // applyActiveDay can only return RankUp or CounterUpdated — fail fast rather
            // than masking a broken invariant behind a misleading NoStats.
            is RankLogic.Transition.RankDown ->
                error("RankLogic.applyActiveDay returned RankDown — impossible on the active-day path")
        }
    }
}

sealed class DailyDecayResult {
    data class ActiveDay(val streak: Int) : DailyDecayResult()
    data class ActiveWithRankUp(val newRank: Rank) : DailyDecayResult()
    data class IdleDay(val statsLost: Int) : DailyDecayResult()
    data class IdleWithRankDown(val newRank: Rank) : DailyDecayResult()
    /** An idle day absorbed by a Streak Freeze Shield — no decay, streak/counter intact. */
    data class ShieldConsumed(val shieldsLeft: Int) : DailyDecayResult()
    /** Today's window was already processed — current call is a no-op (idempotency guard). */
    data object AlreadyApplied : DailyDecayResult()
}

sealed class DecayResult {
    data object NoStats : DecayResult()
    data object NoDecay : DecayResult()
    data class DecayApplied(
        val statsLost: Int,
        val breakCounter: Int,
        val breaksToRankDown: Int
    ) : DecayResult()
    data class DecayWithRankDown(
        val statsLost: Int,
        val newRank: Rank,
        val breakCounter: Int
    ) : DecayResult()
}

sealed class StreakResult {
    data object NoStats : StreakResult()
    data class StreakContinued(
        val newStreak: Int,
        val daysToRankUp: Int
    ) : StreakResult()
    data class StreakWithRankUp(
        val newStreak: Int,
        val newRank: Rank
    ) : StreakResult()
}
