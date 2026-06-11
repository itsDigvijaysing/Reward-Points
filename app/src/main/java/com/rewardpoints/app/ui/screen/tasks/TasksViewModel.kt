package com.rewardpoints.app.ui.screen.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rewardpoints.app.data.local.db.dao.MissionDao
import com.rewardpoints.app.data.local.db.entity.MissionEntity
import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.data.repository.PlayerRepository
import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.rpg.AchievementTracker
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.domain.model.TransactionSource
import com.rewardpoints.app.domain.model.TransactionType
import com.rewardpoints.app.sync.SyncResult
import com.rewardpoints.app.sync.TodoistSyncManager
import com.rewardpoints.app.sync.TodoistTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TasksUiState(
    val missions: List<MissionEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = true,
    val todoistConnected: Boolean = false,
    val todoistSyncing: Boolean = false,
    val todoistLastSync: String? = null,
    val todoistSyncLog: List<String> = emptyList(),
    val todoistTasks: List<TodoistTask> = emptyList(),
    val todoistTasksLoading: Boolean = false,
    val todoistTasksError: String? = null,
    val showTodoistTasks: Boolean = false,
    val isRefreshing: Boolean = false
)

class TasksViewModel(
    private val missionDao: MissionDao,
    private val pointsRepository: PointsRepository,
    private val playerRepository: PlayerRepository,
    private val achievementTracker: AchievementTracker,
    private val todoistSyncManager: TodoistSyncManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
        loadTodoistStatus()
    }

    private fun loadTodoistStatus() {
        viewModelScope.launch {
            // Hydrate the encrypted-secret cache first — the token flow starts as null even
            // when a token is saved, and this ViewModel can win the race against
            // StatUpApp.loadSecretsIfNeeded() on cold start.
            userPreferences.getTodoistToken()
            // REACTIVE: collect the token + last-sync flows instead of reading once. This
            // ViewModel survives bottom-tab switches, so a one-shot read meant connecting
            // Todoist in Settings never revealed the sync UI until the app was restarted.
            var wasConnected = false
            combine(
                userPreferences.todoistToken,
                userPreferences.lastSyncTime
            ) { token, lastSync ->
                !token.isNullOrBlank() to lastSync
            }.collect { (isConnected, lastSync) ->
                val lastSyncStr = if (lastSync > 0) {
                    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync))
                } else null
                _uiState.update {
                    it.copy(
                        todoistConnected = isConnected,
                        todoistLastSync = lastSyncStr
                    )
                }
                // Fetch active tasks when the connection first appears (or re-appears).
                if (isConnected && !wasConnected) {
                    loadTodoistTasks()
                }
                wasConnected = isConnected
            }
        }
    }

    fun loadTodoistTasks() {
        viewModelScope.launch { loadTodoistTasksSuspend() }
    }

    private suspend fun loadTodoistTasksSuspend() {
        _uiState.update { it.copy(todoistTasksLoading = true, todoistTasksError = null) }
        val result = todoistSyncManager.getActiveTasks()
        result.fold(
            onSuccess = { tasks ->
                val sortedTasks = tasks.sortedWith(
                    compareByDescending<TodoistTask> { it.priority }
                        .thenBy { it.due?.date ?: "9999-99-99" }
                )
                _uiState.update {
                    it.copy(
                        todoistTasks = sortedTasks,
                        todoistTasksLoading = false,
                        todoistTasksError = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        todoistTasksLoading = false,
                        todoistTasksError = error.message ?: "Failed to load tasks"
                    )
                }
            }
        )
    }

    fun toggleTodoistTasks() {
        _uiState.update { it.copy(showTodoistTasks = !it.showTodoistTasks) }
    }

    /** Pull-to-refresh handler: re-fetches Todoist + resets daily missions. Awaits completion. */
    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            resetDailyMissionsSuspend()
            if (_uiState.value.todoistConnected) {
                loadTodoistTasksSuspend()
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun syncTodoist() {
        viewModelScope.launch {
            _uiState.update { it.copy(todoistSyncing = true) }
            val result = todoistSyncManager.syncCompletedTasks()
            val log = _uiState.value.todoistSyncLog.toMutableList()
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            when (result) {
                is SyncResult.Success -> {
                    log.add(0, "[$timestamp] Synced ${result.tasksProcessed} tasks (+${result.pointsEarned} pts)")
                }
                is SyncResult.NotConnected -> {
                    log.add(0, "[$timestamp] Not connected - add token in Settings")
                }
                is SyncResult.AuthFailed -> {
                    log.add(0, "[$timestamp] Token rejected - reconnect in Settings")
                }
                is SyncResult.Error -> {
                    log.add(0, "[$timestamp] Error: ${result.message}")
                }
            }

            // todoistLastSync updates reactively via the lastSyncTime flow in
            // loadTodoistStatus() — no manual re-read needed here.
            _uiState.update {
                it.copy(
                    todoistSyncing = false,
                    todoistSyncLog = log.take(10)
                )
            }
            
            // Reload active tasks after sync
            loadTodoistTasks()
        }
    }

    private fun loadMissions() {
        viewModelScope.launch {
            missionDao.getAllMissions().collect { missions ->
                _uiState.update {
                    it.copy(
                        missions = missions,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createMission(
        name: String,
        description: String?,
        points: Int,
        statType: StatType,
        isDaily: Boolean
    ) {
        viewModelScope.launch {
            val mission = MissionEntity(
                name = name,
                description = description,
                pointsReward = points,
                statType = statType.name,
                isDaily = isDaily,
                isCompletedToday = false,
                createdAt = System.currentTimeMillis()
            )
            missionDao.insert(mission)
            hideCreateDialog()
        }
    }

    fun completeMission(mission: MissionEntity) {
        viewModelScope.launch {
            // Re-fetch the live row inside the coroutine to close the double-tap race:
            // the snapshot on the UI side may still say `isCompletedToday=false` while a
            // concurrent tap is already mid-award.
            val current = missionDao.getById(mission.id) ?: return@launch
            if (current.isDaily && current.isCompletedToday) return@launch

            val statType = try {
                StatType.valueOf(current.statType)
            } catch (e: Exception) {
                StatType.STR
            }

            // Mark complete FIRST so a second tap that races past the guard above still
            // sees `isCompletedToday=true` before awarding. addPoints is the expensive op.
            missionDao.update(
                current.copy(
                    isCompletedToday = true,
                    lastCompletedAt = System.currentTimeMillis(),
                    streak = current.streak + 1
                )
            )

            pointsRepository.addPoints(
                points = current.pointsReward,
                type = TransactionType.EARN,
                source = TransactionSource.MISSION,
                description = "Completed: ${current.name}",
                statType = statType,
                relatedId = current.id.toString()
            )

            // Best-effort: an achievement-check failure must not crash mission completion
            // (the mission points were already awarded atomically above).
            runCatching { achievementTracker.onPointsEarned(TransactionSource.MISSION) }
        }
    }

    fun deleteMission(mission: MissionEntity) {
        viewModelScope.launch {
            missionDao.delete(mission)
        }
    }

    fun resetDailyMissions() {
        viewModelScope.launch { resetDailyMissionsSuspend() }
    }

    private suspend fun resetDailyMissionsSuspend() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Reset missions that were completed before today.
        _uiState.value.missions
            .filter { it.isDaily && it.isCompletedToday }
            .filter { (it.lastCompletedAt ?: 0) < todayStart }
            .forEach { mission ->
                missionDao.update(mission.copy(isCompletedToday = false))
            }
    }
}
