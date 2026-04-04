package com.rewardpoints.app.sync

import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.local.db.dao.TransactionDao
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.StatType
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
        // Todoist API: priority 4 = urgent (red), priority 1 = no priority
        // Our mapping: urgent = 4 pts, no priority = 1 pt
        fun todoistPriorityToPoints(todoistPriority: Int): Int {
            return when (todoistPriority) {
                4 -> 4  // Todoist priority 4 (red/urgent) = 4 points
                3 -> 3  // Todoist priority 3 = 3 points
                2 -> 2  // Todoist priority 2 = 2 points
                else -> 1  // Todoist priority 1 (no priority) = 1 point
            }
        }
    }

    suspend fun syncCompletedTasks(): SyncResult {
        val token = userPreferences.todoistToken.first()
        if (token.isNullOrBlank()) {
            return SyncResult.NotConnected
        }

        return try {
            val lastSyncTime = userPreferences.lastSyncTime.first()
            val since = if (lastSyncTime > 0) {
                java.time.Instant.ofEpochMilli(lastSyncTime).toString()
            } else null

            // Fetch active tasks to build a priority/labels lookup
            val activeTasks = todoistApi.getTasks(token).getOrNull().orEmpty()
            val taskLookup = activeTasks.associateBy { it.id }

            val result = todoistApi.getCompletedTasks(token, since)

            result.fold(
                onSuccess = { tasks ->
                    var pointsEarned = 0
                    var tasksProcessed = 0

                    tasks.forEach { completedTask ->
                        // Check if we already processed this task
                        val existingTransaction = transactionDao.getByExternalId(completedTask.taskId)
                        if (existingTransaction == null) {
                            // Look up task details for priority and labels
                            val taskDetails = taskLookup[completedTask.taskId]
                            val points = todoistPriorityToPoints(taskDetails?.priority ?: 1)
                            val labels = taskDetails?.labels.orEmpty()

                            // Route to stat based on labels, or fall back to default
                            val statType = if (labels.isNotEmpty()) {
                                pointsRepository.routeToStat(labels)
                            } else {
                                val defaultStatName = userPreferences.defaultStat.first()
                                StatType.fromString(defaultStatName) ?: StatType.INT
                            }

                            // Route through PointsRepository so stat accumulators update
                            pointsRepository.earnPoints(
                                points = points,
                                statType = statType,
                                source = TransactionSource.TODOIST,
                                description = "Todoist: ${completedTask.content}",
                                relatedId = completedTask.taskId,
                                externalId = completedTask.taskId
                            )

                            pointsEarned += points
                            tasksProcessed++

                            // Track achievements per task
                            achievementTracker.onPointsEarned(TransactionSource.TODOIST)
                        }
                    }

                    // Update last sync time
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
