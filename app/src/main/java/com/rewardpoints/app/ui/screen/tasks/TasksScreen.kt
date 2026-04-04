package com.rewardpoints.app.ui.screen.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.rewardpoints.app.data.local.db.entity.MissionEntity
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.sync.TodoistTask
import com.rewardpoints.app.ui.components.glass.*
import com.rewardpoints.app.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetDailyMissions()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks & Missions",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                GlassIconButton(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = AccentPrimary
                        )
                    },
                    onClick = { viewModel.showCreateDialog() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Todoist section
                    if (uiState.todoistConnected) {
                        item {
                            TodoistSyncCard(
                                lastSync = uiState.todoistLastSync,
                                isSyncing = uiState.todoistSyncing,
                                syncLog = uiState.todoistSyncLog,
                                onSync = { viewModel.syncTodoist() }
                            )
                        }
                        
                        // Todoist Tasks Section
                        item {
                            TodoistTasksSection(
                                tasks = uiState.todoistTasks,
                                isLoading = uiState.todoistTasksLoading,
                                error = uiState.todoistTasksError,
                                isExpanded = uiState.showTodoistTasks,
                                onToggle = { viewModel.toggleTodoistTasks() },
                                onRefresh = { viewModel.loadTodoistTasks() }
                            )
                        }
                    }

                    // Active missions
                    val activeMissions = uiState.missions.filter { !it.isCompletedToday }
                    val completedMissions = uiState.missions.filter { it.isCompletedToday }

                    if (activeMissions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active (${activeMissions.size})",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Inter
                            )
                        }
                        items(activeMissions, key = { it.id }) { mission ->
                            MissionCard(
                                mission = mission,
                                onComplete = { viewModel.completeMission(mission) },
                                onDelete = { viewModel.deleteMission(mission) }
                            )
                        }
                    }

                    if (completedMissions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Completed Today (${completedMissions.size})",
                                color = TextTertiary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Inter
                            )
                        }
                        items(completedMissions, key = { it.id }) { mission ->
                            MissionCard(
                                mission = mission,
                                onComplete = { },
                                onDelete = { viewModel.deleteMission(mission) },
                                isCompleted = true
                            )
                        }
                    }

                    if (uiState.missions.isEmpty() && !uiState.todoistConnected) {
                        item {
                            EmptyTasksState(onCreateClick = { viewModel.showCreateDialog() })
                        }
                    }
                }
            }
        }

        // Create Dialog
        if (uiState.showCreateDialog) {
            CreateMissionDialog(
                onDismiss = { viewModel.hideCreateDialog() },
                onCreate = { name, desc, points, stat, isDaily ->
                    viewModel.createMission(name, desc, points, stat, isDaily)
                }
            )
        }
    }
}

@Composable
private fun TodoistSyncCard(
    lastSync: String?,
    isSyncing: Boolean,
    syncLog: List<String>,
    onSync: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📋", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Todoist Sync",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Inter
                    )
                    Text(
                        text = if (lastSync != null) "Last: $lastSync" else "Never synced",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontFamily = Inter
                    )
                }
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = AccentPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    GlassButtonSmall(
                        text = "Sync Now",
                        onClick = onSync
                    )
                }
            }

            if (syncLog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Sync Log:",
                            color = TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        syncLog.forEach { entry ->
                            Text(
                                text = entry,
                                color = TextTertiary,
                                fontSize = 10.sp,
                                fontFamily = Inter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📋", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Tasks Yet",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Inter
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create custom missions to earn points\nor connect Todoist in Settings",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = Inter,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassButton(
                    text = "+ Create Mission",
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: MissionEntity,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    isCompleted: Boolean = false
) {
    val statType = try {
        StatType.valueOf(mission.statType)
    } catch (e: Exception) {
        StatType.STR
    }

    val statColor = when (statType) {
        StatType.STR -> StatStrength
        StatType.INT -> StatIntelligence
        StatType.WIS -> StatWisdom
        StatType.DEX -> StatDexterity
        StatType.CHA -> StatCharisma
        StatType.VIT -> StatVitality
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (!isCompleted) onComplete else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stat indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                statColor.copy(alpha = 0.3f),
                                statColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AccentSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = statType.name,
                        color = statColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mission info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.name,
                    color = if (isCompleted) TextTertiary else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Inter,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
                if (!mission.description.isNullOrBlank()) {
                    Text(
                        text = mission.description,
                        color = TextTertiary,
                        fontSize = 13.sp,
                        fontFamily = Inter
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${mission.pointsReward} pts",
                        color = AccentPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Inter
                    )
                    if (mission.isDaily) {
                        Text(
                            text = " • Daily",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = Inter
                        )
                    }
                    if (mission.streak > 0) {
                        Text(
                            text = " • 🔥 ${mission.streak}",
                            color = AccentWarning,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateMissionDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String?, points: Int, stat: StatType, isDaily: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("4") }
    var selectedStat by remember { mutableStateOf(StatType.STR) }
    var isDaily by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Create Mission",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Inter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Mission Name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description field
                    GlassTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Points selector - 4 in row
                    Text(
                        text = "Points:",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1", "2", "3", "4").forEach { p ->
                            GlassButtonSmall(
                                text = "+$p",
                                onClick = { points = p },
                                primary = points == p,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stat selector - 3x2 Grid
                    Text(
                        text = "Target Stat:",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = Inter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(StatType.entries.toList()) { stat ->
                            val isSelected = selectedStat == stat
                            GlassButtonSmall(
                                text = stat.name,
                                onClick = { selectedStat = stat },
                                primary = isSelected,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repeats Daily",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = Inter,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isDaily,
                            onCheckedChange = { isDaily = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentPrimary,
                                checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = GlassFill
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            primary = false,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Create",
                            onClick = {
                                if (name.isNotBlank()) {
                                    onCreate(
                                        name,
                                        description.takeIf { it.isNotBlank() },
                                        points.toIntOrNull() ?: 4,
                                        selectedStat,
                                        isDaily
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoistTasksSection(
    tasks: List<TodoistTask>,
    isLoading: Boolean,
    error: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRefresh: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📋", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Todoist Tasks",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter
                        )
                        Text(
                            text = "${tasks.size} active tasks",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontFamily = Inter
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = AccentPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }
            }
            
            // Error message
            if (error != null) {
                Text(
                    text = error,
                    color = AccentError,
                    fontSize = 12.sp,
                    fontFamily = Inter,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Tasks list
            AnimatedVisibility(
                visible = isExpanded && tasks.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.take(10).forEach { task ->
                        TodoistTaskCard(task = task)
                    }
                    
                    if (tasks.size > 10) {
                        Text(
                            text = "+${tasks.size - 10} more tasks",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = Inter,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Empty state
            AnimatedVisibility(
                visible = isExpanded && tasks.isEmpty() && !isLoading && error == null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = "No active tasks in Todoist",
                    color = TextTertiary,
                    fontSize = 13.sp,
                    fontFamily = Inter,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TodoistTaskCard(task: TodoistTask) {
    val priorityColor = when (task.priority) {
        4 -> AccentError      // P1 (urgent/red)
        3 -> AccentWarning     // P2 (orange)
        2 -> AccentPrimary     // P3 (blue)
        else -> TextTertiary   // P4/none
    }
    
    val priorityLabel = when (task.priority) {
        4 -> "P1"
        3 -> "P2"
        2 -> "P3"
        else -> "P4"
    }
    
    val pointsForTask = when (task.priority) {
        4 -> 4
        3 -> 3
        2 -> 2
        else -> 1
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(priorityColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = priorityLabel,
                color = priorityColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter
            )
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // Task content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.content,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Inter,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Due date if present
            task.due?.let { due ->
                val dueText = due.string ?: due.date ?: ""
                if (dueText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📅 $dueText",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontFamily = Inter
                    )
                }
            }
            
            // Labels if present
            if (task.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "🏷️ ${task.labels.joinToString(", ")}",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontFamily = Inter,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Points indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(AccentPrimary.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "+$pointsForTask",
                color = AccentPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Inter
            )
        }
    }
}
