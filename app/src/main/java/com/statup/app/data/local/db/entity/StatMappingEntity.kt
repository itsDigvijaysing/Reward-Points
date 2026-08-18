package com.statup.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stat_mappings")
data class StatMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceType: String,
    val sourceId: String,
    val sourceName: String,
    val statType: String,
    val createdAt: Long
)
