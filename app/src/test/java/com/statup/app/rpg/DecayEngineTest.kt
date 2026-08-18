package com.rewardpoints.app.rpg

import com.rewardpoints.app.data.local.db.dao.DecayLogDao
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.local.db.entity.DecayLogEntity
import com.rewardpoints.app.data.local.db.entity.TransactionEntity
import com.rewardpoints.app.domain.model.PlayerStats
import com.rewardpoints.app.domain.model.Rank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * JVM tests for [DecayEngine.applyDailyDecay] — the daily-tick heart. Uses hand-written fakes for
 * the narrow ports (no Android / Room), matching the project's test style. The atomicity wrapper
 * is exercised via a pass-through [Transactor]; the read-modify-write logic and the idempotency
 * gate are what's under test here.
 */
class DecayEngineTest {

    private fun engine(
        statsStore: FakeStatsStore,
        dayStore: FakeDayStore,
        logDao: FakeDecayLogDao = FakeDecayLogDao(),
        earnedYesterday: Int = 0,
    ) = DecayEngine(
        statsStore = statsStore,
        decayLogDao = logDao,
        dayStore = dayStore,
        transactor = ImmediateTransactor,
        transactionDao = FakeTransactionDao(earnedYesterday),
        achievementTracker = null,
        widgetUpdater = null,
    )

    @Test
    fun `already applied today is a no-op`() = runTest {
        val stats = FakeStatsStore(PlayerStats(strStat = 50))
        val result = engine(stats, FakeDayStore(lastDay = LocalDate.now().toString())).applyDailyDecay()

        assertTrue(result is DailyDecayResult.AlreadyApplied)
        assertTrue("no stat writes on a no-op", stats.updatedStats.isEmpty())
    }

    @Test
    fun `idle day with a shield consumes one shield and skips decay`() = runTest {
        val stats = FakeStatsStore(PlayerStats(strStat = 50, streakShields = 2))
        val day = FakeDayStore()

        val result = engine(stats, day, earnedYesterday = 0).applyDailyDecay()

        assertEquals(DailyDecayResult.ShieldConsumed(shieldsLeft = 1), result)
        assertEquals("shield decremented", 1, stats.stats!!.streakShields)
        assertEquals("stat NOT decayed", 50, stats.stats!!.strStat)
        assertEquals("idempotency marker advanced", LocalDate.now().toString(), day.lastDay)
    }

    @Test
    fun `idle day without a shield decays stats above base and logs it`() = runTest {
        val stats = FakeStatsStore(PlayerStats(strStat = 10, rank = Rank.D, rankUpStreakCounter = 3))
        val log = FakeDecayLogDao()

        val result = engine(stats, FakeDayStore(), log, earnedYesterday = 0).applyDailyDecay()

        assertTrue(result is DailyDecayResult.IdleDay)
        assertEquals("one stat point lost", 1, (result as DailyDecayResult.IdleDay).statsLost)
        assertEquals("str decremented by 1", 9, stats.stats!!.strStat)
        assertEquals("streak reset on idle decay", 0, stats.stats!!.streak)
        assertEquals("decay row written", 1, log.inserts)
    }

    @Test
    fun `decay floors at base — no loss and no log row when already at base`() = runTest {
        val stats = FakeStatsStore(PlayerStats(rank = Rank.D, rankUpStreakCounter = 3)) // all at BASE_STAT
        val log = FakeDecayLogDao()

        val result = engine(stats, FakeDayStore(), log, earnedYesterday = 0).applyDailyDecay()

        assertEquals(DailyDecayResult.IdleDay(0), result)
        assertEquals("no stat falls below base", PlayerStats.BASE_STAT, stats.stats!!.strStat)
        assertEquals("nothing lost → no decay row", 0, log.inserts)
    }

    @Test
    fun `active day increments streak and advances the rank-up counter`() = runTest {
        val stats = FakeStatsStore(PlayerStats(streak = 3, rank = Rank.D, rankUpStreakCounter = 1))
        val day = FakeDayStore()

        val result = engine(stats, day, earnedYesterday = 4).applyDailyDecay()

        assertTrue(result is DailyDecayResult.ActiveDay)
        assertEquals("streak +1", 4, stats.lastStreak)
        assertEquals("rank-up counter advanced", 2, stats.lastCounter)
        assertEquals("idempotency marker advanced", LocalDate.now().toString(), day.lastDay)
    }

    // ---- Fakes ----

    private class FakeStatsStore(var stats: PlayerStats?) : DecayStatsStore {
        val updatedStats = mutableListOf<PlayerStats>()
        var lastStreak: Int? = null
        var lastRank: Rank? = null
        var lastCounter: Int? = null
        override suspend fun getStatsOnce(): PlayerStats? = stats
        override suspend fun updateStats(stats: PlayerStats) {
            this.stats = stats
            updatedStats.add(stats)
        }
        override suspend fun updateStreak(streak: Int) { lastStreak = streak }
        override suspend fun updateRank(rank: Rank) { lastRank = rank }
        override suspend fun updateRankUpCounter(counter: Int) { lastCounter = counter }
    }

    private class FakeDayStore(var lastDay: String? = null) : DecayDayStore {
        override suspend fun getLastDecayDay(): String? = lastDay
        override suspend fun setLastDecayDay(day: String) { lastDay = day }
    }

    private object ImmediateTransactor : Transactor {
        override suspend fun <R> transaction(block: suspend () -> R): R = block()
    }

    private class FakeDecayLogDao : DecayLogDao {
        var inserts = 0
        override fun getAll(): Flow<List<DecayLogEntity>> = error("unused")
        override fun getRecent(limit: Int): Flow<List<DecayLogEntity>> = error("unused")
        override suspend fun insert(log: DecayLogEntity): Long { inserts++; return inserts.toLong() }
        override suspend fun deleteAll() = error("unused")
    }

    private class FakeTransactionDao(private val earned: Int) : TransactionDao {
        override suspend fun getEarnedInRange(startTime: Long, endTime: Long): Int? = earned
        override fun getAll(): Flow<List<TransactionEntity>> = error("unused")
        override fun getRecent(limit: Int): Flow<List<TransactionEntity>> = error("unused")
        override fun getByType(type: String): Flow<List<TransactionEntity>> = error("unused")
        override fun getRecentByType(type: String, limit: Int): Flow<List<TransactionEntity>> = error("unused")
        override fun getByStatType(statType: String): Flow<List<TransactionEntity>> = error("unused")
        override fun getByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> = error("unused")
        override suspend fun getByExternalId(externalId: String): TransactionEntity? = error("unused")
        override suspend fun getTotalEarned(): Int? = error("unused")
        override suspend fun getTotalRedeemed(): Int? = error("unused")
        override fun getBalance(): Flow<Int> = error("unused")
        override fun observeEarnedInRange(startTime: Long, endTime: Long): Flow<Int> = error("unused")
        override suspend fun insert(transaction: TransactionEntity): Long = error("unused")
        override suspend fun insertIgnore(transaction: TransactionEntity): Long = error("unused")
        override suspend fun delete(transaction: TransactionEntity) = error("unused")
        override suspend fun getTaskTransactionCount(): Int = error("unused")
        override fun countBySourceInRange(source: String, startTime: Long, endTime: Long): Flow<Int> = error("unused")
        override suspend fun deleteAll() = error("unused")
    }
}
