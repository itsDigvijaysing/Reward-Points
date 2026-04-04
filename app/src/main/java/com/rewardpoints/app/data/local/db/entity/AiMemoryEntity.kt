package com.rewardpoints.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_memory")
data class AiMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val content: String,
    val confidence: Float = 0.8f,
    val createdAt: Long,
    val lastAccessedAt: Long,
    val accessCount: Int = 0,
    val source: String
)
