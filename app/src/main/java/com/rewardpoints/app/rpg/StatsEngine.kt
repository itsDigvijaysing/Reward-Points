package com.rewardpoints.app.rpg

import com.rewardpoints.app.data.repository.PointsRepository
import com.rewardpoints.app.domain.model.StatType
import com.rewardpoints.app.domain.model.TransactionSource

class StatsEngine(
    private val pointsRepository: PointsRepository
) {
    suspend fun earnPointsFromTask(
        priority: Int,
        labels: List<String>,
        taskDescription: String?,
        taskId: String?
    ) {
        val points = calculateTaskPoints(priority)
        val statType = pointsRepository.routeToStat(labels)

        pointsRepository.earnPoints(
            points = points,
            statType = statType,
            source = TransactionSource.TODOIST,
            description = taskDescription,
            relatedId = taskId
        )
    }

    suspend fun earnManualPoints(
        points: Int,
        statType: StatType,
        description: String?
    ) {
        pointsRepository.earnPoints(
            points = points,
            statType = statType,
            source = TransactionSource.MANUAL,
            description = description
        )
    }

    suspend fun earnMoodPoints() {
        pointsRepository.earnPoints(
            points = MOOD_POINTS,
            statType = StatType.WIS,
            source = TransactionSource.MOOD,
            description = "Daily mood check-in"
        )
    }

    suspend fun earnMissionPoints(
        points: Int,
        statType: StatType,
        missionName: String,
        missionId: Long
    ) {
        pointsRepository.earnPoints(
            points = points,
            statType = statType,
            source = TransactionSource.MISSION,
            description = missionName,
            relatedId = missionId.toString()
        )
    }

    companion object {
        const val MOOD_POINTS = 2

        fun calculateTaskPoints(apiPriority: Int): Int {
            // Todoist API inverts: p1 (urgent) = priority 4, p4 (normal) = priority 1
            return when (apiPriority) {
                4 -> 4  // p1 urgent → 4 reward points
                3 -> 3  // p2 high   → 3 reward points
                2 -> 2  // p3 medium → 2 reward points
                1 -> 1  // p4 normal → 1 reward point
                else -> 1
            }
        }
    }
}
