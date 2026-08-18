package dev.statup.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Triggers a refresh of every [StatsWidgetProvider] instance currently on the user's home screen.
 *
 * Strategy: broadcast `ACTION_APPWIDGET_UPDATE` for the live widget IDs. That re-enters
 * [StatsWidgetProvider.onUpdate], which re-reads the DB and rebuilds the RemoteViews — so the
 * provider stays the single source of truth for how the widget looks.
 *
 * Call sites: data mutations that change anything the widget displays (rank, balance, streak,
 * today's earned points). Today that's [dev.statup.app.data.repository.PointsRepository]
 * (earn/redeem) and [dev.statup.app.rpg.DecayEngine] (daily tick → streak/rank).
 *
 * Cheap when no widget is on the home screen — `getAppWidgetIds()` returns empty and we early-out.
 */
class StatsWidgetUpdater(private val appContext: Context) {

    fun refresh() {
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, StatsWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            this.component = component
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        appContext.sendBroadcast(intent)
    }
}
