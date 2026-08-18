package com.rewardpoints.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val emoji: String?,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long?,
    val progress: Int = 0,
    val target: Int,
    val rewardPoints: Int = 0
)
