package com.rewardpoints.app.sync

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.rpg.AchievementTracker
import kotlinx.coroutines.flow.first

class TodoistSyncManager(
    private val todoistApi: TodoistApi,
    private val userPreferences: UserPreferences,
    private val transactionDao: TransactionDao,
    private val pointsRepository: PointsRepository,
    private val achievementTracker: AchievementTracker
) {
    companion object {
        fun todoistPriorityToPoints(todoistPriority: Int): Int {
            return when (todoistPriority) {
                4 -> 4
                3 -> 3
                2 -> 2
                else -> 1
            }
        }
    }

    suspend fun syncCompletedTasks(): SyncResult {
        val token = userPreferences.todoistToken.first()
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
                    var pointsEarned = 0
                    var tasksProcessed = 0

                    tasks.forEach { completedTask ->
                        val externalId = completedTask.stableId
                        if (externalId.isBlank()) return@forEach

                        val existingTransaction = transactionDao.getByExternalId(externalId)
                        if (existingTransaction == null) {
                            val points = todoistPriorityToPoints(completedTask.priority)
                            val labels = completedTask.labels

                            // If labels exist, route to stat via mapping. Otherwise just award points (no stat).
                            val statType = if (labels.isNotEmpty()) {
                                pointsRepository.routeToStat(labels)
                            } else {
                                null
                            }

                            pointsRepository.earnPoints(
                                points = points,
                                statType = statType,
                                source = TransactionSource.TODOIST,
                                description = "Todoist: ${completedTask.content}",
                                relatedId = externalId,
                                externalId = externalId
                            )

                            pointsEarned += points
                            tasksProcessed++

                            achievementTracker.onPointsEarned(TransactionSource.TODOIST)
                        }
                    }

                    userPreferences.setLastSyncTime(System.currentTimeMillis())

                    SyncResult.Success(tasksProcessed, pointsEarned)
                },
                onFailure = { error ->
                    SyncResult.Error(error.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Sync failed")
        }
    }

    suspend fun getActiveTasks(): Result<List<TodoistTask>> {
        val token = userPreferences.todoistToken.first()
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
}
