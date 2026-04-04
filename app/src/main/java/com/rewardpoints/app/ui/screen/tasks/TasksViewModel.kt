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
import kotlinx.coroutines.flow.first
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
    val showTodoistTasks: Boolean = true
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
            val token = userPreferences.todoistToken.first()
            val lastSync = userPreferences.lastSyncTime.first()
            val lastSyncStr = if (lastSync > 0) {
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync))
            } else null
            val isConnected = !token.isNullOrBlank()
            _uiState.update {
                it.copy(
                    todoistConnected = isConnected,
                    todoistLastSync = lastSyncStr
                )
            }
            
            // Load active tasks if connected
            if (isConnected) {
                loadTodoistTasks()
            }
        }
    }

    fun loadTodoistTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(todoistTasksLoading = true, todoistTasksError = null) }
            
            val result = todoistSyncManager.getActiveTasks()
            result.fold(
                onSuccess = { tasks ->
                    // Sort by priority (high to low) then by due date
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
    }

    fun toggleTodoistTasks() {
        _uiState.update { it.copy(showTodoistTasks = !it.showTodoistTasks) }
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
                is SyncResult.Error -> {
                    log.add(0, "[$timestamp] Error: ${result.message}")
                }
            }

            val lastSync = userPreferences.lastSyncTime.first()
            val lastSyncStr = if (lastSync > 0) {
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync))
            } else null

            _uiState.update {
                it.copy(
                    todoistSyncing = false,
                    todoistLastSync = lastSyncStr,
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
                _uiState.value = _uiState.value.copy(
                    missions = missions,
                    isLoading = false
                )
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
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
            val statType = try {
                StatType.valueOf(mission.statType)
            } catch (e: Exception) {
                StatType.STR
            }

            // Award points
            pointsRepository.addPoints(
                points = mission.pointsReward,
                type = TransactionType.EARN,
                source = TransactionSource.MISSION,
                description = "Completed: ${mission.name}",
                statType = statType,
                relatedId = mission.id.toString()
            )

            // Track achievements
            achievementTracker.onPointsEarned(TransactionSource.MISSION)

            // Update mission completion status
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            missionDao.update(
                mission.copy(
                    isCompletedToday = true,
                    lastCompletedAt = System.currentTimeMillis(),
                    streak = mission.streak + 1
                )
            )
        }
    }

    fun deleteMission(mission: MissionEntity) {
        viewModelScope.launch {
            missionDao.delete(mission)
        }
    }

    fun resetDailyMissions() {
        viewModelScope.launch {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Reset missions that were completed before today
            _uiState.value.missions
                .filter { it.isDaily && it.isCompletedToday }
                .filter { (it.lastCompletedAt ?: 0) < todayStart }
                .forEach { mission ->
                    missionDao.update(mission.copy(isCompletedToday = false))
                }
        }
    }
}
