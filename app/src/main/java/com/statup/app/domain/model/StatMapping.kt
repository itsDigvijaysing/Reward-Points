package com.rewardpoints.app.domain.model

data class StatMapping(
    val id: Long = 0,
    val sourceType: String,
    val sourceId: String,
    val sourceName: String,
    val statType: StatType,
    val createdAt: Long = System.currentTimeMillis()
)
