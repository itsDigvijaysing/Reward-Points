package com.rewardpoints.app.rpg

import com.rewardpoints.app.data.local.db.dao.DecayLogDao
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.local.db.entity.DecayLogEntity
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import java.util.Calendar

class DecayEngine(
    private val playerRepository: PlayerRepository,
    private val decayLogDao: DecayLogDao,
    private val transactionDao: TransactionDao? = null,
    private val achievementTracker: AchievementTracker? = null
) {
    /**
     * Called at midnight by DecayWorker.
     * Checks if any tasks/points were earned today.
     * If yes: recordSuccessfulDay()
     * If no: applyDecay()
     */
    suspend fun applyDailyDecay(): DailyDecayResult {
        val todayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1) // Check yesterday since we run at midnight
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Check if any EARN transactions happened yesterday
        val earnedYesterday = transactionDao?.getEarnedInRange(todayStart, todayEnd) ?: 0

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

        // Star lines system: break day removes 1 star line
        val newRankUpCounter = stats.rankUpStreakCounter - 1

        // Check for rank down: stars go below 0 → immediate rank down
        val previousRank = stats.rank.previousRank()
        if (newRankUpCounter < 0 && previousRank != null) {
            val newRank = previousRank

            // Update stats with decay + reset streak
            val updatedStats = stats.copy(
                strStat = stats.strStat - strLost,
                intStat = stats.intStat - intLost,
                wisStat = stats.wisStat - wisLost,
                dexStat = stats.dexStat - dexLost,
                chaStat = stats.chaStat - chaLost,
                vitStat = stats.vitStat - vitLost,
                streak = 0,
                rankUpStreakCounter = Rank.STREAK_DAYS_TO_RANK_UP, // Reset to 5 after rank down
                rank = newRank,
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

            return DecayResult.DecayWithRankDown(
                statsLost = totalLost,
                newRank = newRank,
                breakCounter = 0
            )
        }

        // Clamp at 0 if at lowest rank (E) — can't go negative
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
            DecayResult.DecayApplied(
                statsLost = totalLost,
                breakCounter = clampedCounter,
                breaksToRankDown = clampedCounter
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

        // Check for rank up (5 star lines = rank up)
        val nextRank = stats.rank.nextRank()
        if (newRankUpCounter >= Rank.STREAK_DAYS_TO_RANK_UP && nextRank != null) {
            val newRank = nextRank
            playerRepository.updateRank(newRank)
            // Reset to 0 lines after rank up
            playerRepository.updateRankUpCounter(0)

            return StreakResult.StreakWithRankUp(
                newStreak = newStreak,
                newRank = newRank
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
