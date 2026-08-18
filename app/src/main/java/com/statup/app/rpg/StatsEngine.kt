package com.statup.app.rpg

/**
 * Pure math for stat/points calculations. Used to host instance methods that wrapped
 * `PointsRepository.earnPoints` for each earn type (task / manual / mood / mission),
 * but every ViewModel went straight to `PointsRepository.addPoints` instead, so those
 * wrappers were dead code and were removed. Only [calculateTaskPoints] and [MOOD_POINTS]
 * remain — these are referenced from `TodoistSyncManager` and `StatusViewModel`.
 *
 * Kept as a class (not an `object`) so it can stay in Koin and keep room for future
 * stat math that legitimately needs DI.
 */
@Suppress("unused")
class StatsEngine {

    companion object {
        const val MOOD_POINTS = 2

        /**
         * Map Todoist's API priority (1=normal … 4=urgent — inverted from the UI p1..p4
         * scheme) to reward points. p1 urgent → 4 pts, p4 normal → 1 pt.
         */
        fun calculateTaskPoints(apiPriority: Int): Int = when (apiPriority) {
            4 -> 4
            3 -> 3
            2 -> 2
            1 -> 1
            else -> 1
        }
    }
}
