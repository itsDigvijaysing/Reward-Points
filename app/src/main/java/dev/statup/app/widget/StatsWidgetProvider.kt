package dev.statup.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.statup.app.MainActivity
import dev.statup.app.R
import dev.statup.app.data.local.db.AppDatabase
import dev.statup.app.data.local.db.entity.PlayerStatsEntity
import dev.statup.app.domain.model.Rank
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId

/**
 * 4×2 home-screen widget showing the player's current rank, balance, streak, and today's points.
 *
 * Update model: explicit broadcast-driven. We don't rely on `updatePeriodMillis` (the manifest
 * sets it to 0). Instead, [StatsWidgetUpdater.update] is called from the app whenever data that
 * affects the widget changes: balance updates (PointsRepository.addPoints), decay tick
 * (DecayEngine.applyDailyDecay), and app start.
 *
 * RemoteViews trade-offs:
 *   - Can't use Compose, custom views, or coroutines on the rendering thread.
 *   - The provider receives onUpdate on a binder thread; we read the DB synchronously via
 *     runBlocking. Stats are a singleton row + a tiny SUM query — fast enough.
 *   - Click target is the whole root view → opens MainActivity.
 */
class StatsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val snapshot = readSnapshot(context)
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context, snapshot))
        }
    }

    private fun readSnapshot(context: Context): WidgetSnapshot {
        val db = AppDatabase.getInstance(context.applicationContext)
        val statsDao = db.playerStatsDao()
        val txDao = db.transactionDao()
        return runBlocking {
            val stats = statsDao.getStatsOnce()
                ?: PlayerStatsEntity(id = 1, lastActivityAt = null, updatedAt = System.currentTimeMillis())
            val (start, end) = todayMillisRange()
            val today = txDao.getEarnedInRange(start, end) ?: 0
            // Pull the live balance in the same runBlocking so we only suspend the binder
            // thread once. getBalance().first() is a one-row aggregate — fast.
            val balance = txDao.getBalance().first()
            WidgetSnapshot(stats, today, balance)
        }
    }

    private fun todayMillisRange(): Pair<Long, Long> {
        val start = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return start to (start + 86_400_000L)
    }

    private fun buildViews(context: Context, snapshot: WidgetSnapshot): RemoteViews {
        val rank = runCatching { Rank.valueOf(snapshot.stats.rank) }.getOrDefault(Rank.E)

        return RemoteViews(context.packageName, R.layout.widget_stats).apply {
            setTextViewText(R.id.widget_rank, rank.name)
            setTextViewText(R.id.widget_rank_title, rank.title)
            setTextViewText(R.id.widget_balance, "✨ ${snapshot.balance}")
            setTextViewText(R.id.widget_streak, "🔥 ${snapshot.stats.streak}d")
            setTextViewText(R.id.widget_today, "+${snapshot.today} today")

            setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
        }
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private data class WidgetSnapshot(
        val stats: PlayerStatsEntity,
        val today: Int,
        val balance: Int
    )

    companion object {
        private const val REQUEST_OPEN_APP = 100
    }
}
