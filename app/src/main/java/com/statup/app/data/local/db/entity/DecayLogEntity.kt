package com.rewardpoints.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decay_log")
data class DecayLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strLost: Int = 0,
    val intLost: Int = 0,
    val wisLost: Int = 0,
    val dexLost: Int = 0,
    val chaLost: Int = 0,
    val vitLost: Int = 0,
    val idleHours: Int?,
    val reason: String?,
    val createdAt: Long
)
