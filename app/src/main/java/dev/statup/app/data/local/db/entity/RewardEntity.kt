package dev.statup.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val pointsCost: Int,
    val category: String,
    val emoji: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val timesRedeemed: Int = 0
)
