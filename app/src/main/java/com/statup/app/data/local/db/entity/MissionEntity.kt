package com.rewardpoints.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val pointsReward: Int,
    val statType: String,
    val isDaily: Boolean = true,
    val isCompletedToday: Boolean = false,
    val lastCompletedAt: Long? = null,
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
