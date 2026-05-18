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
            // User was idle, apply decay
            when (val result = applyDecay("daily_idle")) {
                is DecayResult.DecayWithRankDown -> DailyDecayResult.IdleWithRankDown(result.newRank)
                is DecayResult.DecayApplied -> DailyDecayResult.IdleDay(result.statsLost)
                else -> DailyDecayResult.IdleDay(0)
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

        // Calculate stat loss (1 point from each stat above BASE_STAT)
        val strLost = if (stats.strStat > PlayerStats.BASE_STAT) 1 else 0
        val intLost = if (stats.intStat > PlayerStats.BASE_STAT) 1 else 0
        val wisLost = if (stats.wisStat > PlayerStats.BASE_STAT) 1 else 0
        val dexLost = if (stats.dexStat > PlayerStats.BASE_STAT) 1 else 0
        val chaLost = if (stats.chaStat > PlayerStats.BASE_STAT) 1 else 0
        val vitLost = if (stats.vitStat > PlayerStats.BASE_STAT) 1 else 0

        val totalLost = strLost + intLost + wisLost + dexLost + chaLost + vitLost

        // Star-line counter: each idle day removes one.
        //
        // Up:   5 consecutive active days needed (counter climbs 0 → +5 → rank up, reset to 0).
        // Down: dropping below 0 demotes immediately. No grace period after promotion — slack
        //       off the day after rank-up and you fall straight back.
        // After demotion the counter resets to +5 at the lower rank: one active day there
        // (counter +5 → +6 ≥ 5) bounces you back up, so the lower rank feels like a "near miss"
        // rather than a hard reset. This asymmetry is intentional (game design).
        // At rank E the counter clamps at 0 (no rank below E to fall to).
        val newRankUpCounter = stats.rankUpStreakCounter - 1

        val previousRank = stats.rank.previousRank()
        if (newRankUpCounter < 0 && previousRank != null) {
            val newRank = previousRank

            val updatedStats = stats.copy(
                strStat = stats.strStat - strLost,
                intStat = stats.intStat - intLost,
                wisStat = stats.wisStat - wisLost,
                dexStat = stats.dexStat - dexLost,
                chaStat = stats.chaStat - chaLost,
                vitStat = stats.vitStat - vitLost,
                streak = 0,
                rankUpStreakCounter = Rank.STREAK_DAYS_TO_RANK_UP, // +5 cushion at the lower rank
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

            return DecayResult.DecayWithRankDown(
                statsLost = totalLost,
                newRank = newRank,
                breakCounter = Rank.STREAK_DAYS_TO_RANK_UP
            )
        }

        // At rank E (no previousRank): clamp counter at 0 so idle days don't accumulate negative.
        val clampedCounter = newRankUpCounter.coerceAtLeast(0)

        // Update stats
        val updatedStats = stats.copy(
            strStat = stats.strStat - strLost,
            intStat = stats.intStat - intLost,
            wisStat = stats.wisStat - wisLost,
            dexStat = stats.dexStat - dexLost,
            chaStat = stats.chaStat - chaLost,
            vitStat = stats.vitStat - vitLost,
            streak = 0,
            rankUpStreakCounter = clampedCounter,
            updatedAt = System.currentTimeMillis()
        )

        playerRepository.updateStats(updatedStats)

        // Log decay
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

        return if (totalLost > 0) {
            // breaksToRankDown = how many more idle days until counter drops below 0.
            // After a normal decay the counter just got -=1; one more idle day at counter 0
            // would demote (0 → -1). At counter >0 (e.g. just-promoted at 0 then active days),
            // we still have clampedCounter+1 idle days of buffer.
            val daysToDemote = clampedCounter + 1
            DecayResult.DecayApplied(
                statsLost = totalLost,
                breakCounter = clampedCounter,
                breaksToRankDown = daysToDemote
            )
        } else {
            DecayResult.NoDecay
        }
    }

    suspend fun recordSuccessfulDay(): StreakResult {
        val stats = playerRepository.getStatsOnce() ?: return StreakResult.NoStats

        val newStreak = stats.streak + 1
        val newRankUpCounter = stats.rankUpStreakCounter + 1

        playerRepository.updateStreak(newStreak)
        playerRepository.updateRankUpCounter(newRankUpCounter)

        // Check for rank up (5 star lines = rank up). `updateRank` resets the counter to 0
        // as part of its SQL, so no separate `updateRankUpCounter(0)` call is needed.
        val nextRank = stats.rank.nextRank()
        if (newRankUpCounter >= Rank.STREAK_DAYS_TO_RANK_UP && nextRank != null) {
            playerRepository.updateRank(nextRank)
            return StreakResult.StreakWithRankUp(
                newStreak = newStreak,
                newRank = nextRank
            )
        }

        return StreakResult.StreakContinued(
            newStreak = newStreak,
            daysToRankUp = Rank.STREAK_DAYS_TO_RANK_UP - newRankUpCounter
        )
    }
}

sealed class DailyDecayResult {
    data class ActiveDay(val streak: Int) : DailyDecayResult()
    data class ActiveWithRankUp(val newRank: Rank) : DailyDecayResult()
    data class IdleDay(val statsLost: Int) : DailyDecayResult()
    data class IdleWithRankDown(val newRank: Rank) : DailyDecayResult()
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
