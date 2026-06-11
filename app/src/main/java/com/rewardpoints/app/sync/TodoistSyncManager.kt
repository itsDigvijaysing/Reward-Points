package com.rewardpoints.app.sync

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.rpg.StatsEngine
import kotlinx.coroutines.flow.first

class TodoistSyncManager(
    private val todoistApi: TodoistApi,
    private val userPreferences: UserPreferences,
    private val pointsRepository: PointsRepository,
    private val achievementTracker: AchievementTracker
) {

    suspend fun syncCompletedTasks(): SyncResult {
        val token = userPreferences.getTodoistToken()
        if (token.isNullOrBlank()) {
            return SyncResult.NotConnected
        }

        return try {
            // Fetch recent completed tasks. First sync gets more history,
            // subsequent syncs only need recent ones (dedup via externalId handles overlap).
            val lastSyncTime = userPreferences.lastSyncTime.first()
            val limit = if (lastSyncTime > 0) 30 else 200
            val result = todoistApi.getCompletedTasks(token, limit = limit)

            result.fold(
                onSuccess = { tasks ->
                    // Cache stat mappings once per sync (avoid N round trips for N tasks)
                    val mappingsCache = pointsRepository.loadStatMappings()
                    var pointsEarned = 0
                    var tasksProcessed = 0

                    tasks.forEach { completedTask ->
                        val externalId = completedTask.stableId
                        if (externalId.isBlank()) return@forEach

                        val points = StatsEngine.calculateTaskPoints(completedTask.priority)
                        val labels = completedTask.labels
                        val statType = if (labels.isNotEmpty()) {
                            pointsRepository.routeToStatCached(labels, mappingsCache)
                        } else {
                            null
                        }

                        // tryEarnExternalPoints handles dedup atomically via the unique index
                        // on transactions.externalId — returns null if this task was already synced.
                        val tx = pointsRepository.tryEarnExternalPoints(
                            externalId = externalId,
                            points = points,
                            statType = statType,
                            source = TransactionSource.TODOIST,
                            description = "Todoist: ${completedTask.content}"
                        )

                        if (tx != null) {
                            pointsEarned += points
                            tasksProcessed++
                        }
                    }

                    userPreferences.setLastSyncTime(System.currentTimeMillis())

                    // Run achievement checks ONCE after the loop and off the sync's critical
                    // path. onPointsEarned keys off cumulative totals (it writes absolute
                    // progress), so once-after-the-loop is identical to per-task — but a thrown
                    // achievement check can no longer downgrade an already-successful, already
                    // idempotently-awarded sync to a retry + false "sync failed" notification.
                    // It also collapses ~N achievement passes (one per task) into a single pass.
                    if (tasksProcessed > 0) {
                        runCatching { achievementTracker.onPointsEarned(TransactionSource.TODOIST) }
                    }

                    SyncResult.Success(tasksProcessed, pointsEarned)
                },
                onFailure = { error ->
                    if (error is TodoistAuthException) {
                        SyncResult.AuthFailed(error.message ?: "Invalid token")
                    } else {
                        SyncResult.Error(error.message ?: "Unknown error")
                    }
                }
            )
        } catch (e: TodoistAuthException) {
            SyncResult.AuthFailed(e.message ?: "Invalid token")
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Sync failed")
        }
    }

    suspend fun getActiveTasks(): Result<List<TodoistTask>> {
        val token = userPreferences.getTodoistToken()
        if (token.isNullOrBlank()) {
            return Result.failure(Exception("Not connected to Todoist"))
        }
        return todoistApi.getTasks(token)
    }
}

sealed class SyncResult {
    data object NotConnected : SyncResult()
    data class Success(val tasksProcessed: Int, val pointsEarned: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    /** Token invalid/expired — do not retry until user re-enters token. */
    data class AuthFailed(val message: String) : SyncResult()
}
