package com.rewardpoints.app.domain.model

import androidx.compose.ui.graphics.Color
import com.rewardpoints.app.ui.theme.*

enum class Rank(
    val title: String,
    val color: Color,
    val order: Int
) {
    E("Novice", RankE, 0),
    D("Apprentice", RankD, 1),
    C("Warrior", RankC, 2),
    B("Elite", RankB, 3),
    A("Champion", RankA, 4),
    S("Master", RankS, 5);

    fun canRankUp(): Boolean = this != S
    fun canRankDown(): Boolean = this != E

    fun nextRank(): Rank? = entries.find { it.order == order + 1 }
    fun previousRank(): Rank? = entries.find { it.order == order - 1 }

    companion object {
        fun fromString(value: String): Rank = entries.find { it.name == value } ?: E
        const val STREAK_DAYS_TO_RANK_UP = 5
        const val BREAKS_TO_RANK_DOWN = 5
    }
}
