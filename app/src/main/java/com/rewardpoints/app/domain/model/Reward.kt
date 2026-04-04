package com.rewardpoints.app.domain.model

data class Reward(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val pointsCost: Int,
    val category: String,
    val emoji: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val timesRedeemed: Int = 0
)
