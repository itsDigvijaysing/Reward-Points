package com.statup.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todoist_tasks")
data class TodoistTaskEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val description: String?,
    val priority: Int,
    val projectId: String?,
    val labels: String?,
    val dueDate: String?,
    val isCompleted: Boolean = false,
    val pointsEarned: Int = 0,
    val syncedAt: Long
)
