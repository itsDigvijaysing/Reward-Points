package com.rewardpoints.app.ui.navigation

object Routes {
    const val STATUS = "status"
    const val TASKS = "tasks"
    const val REWARDS = "rewards"
    const val AGENT = "agent"
    const val SETTINGS = "settings"

    const val FULL_STATS = "full_stats"
    const val ACHIEVEMENTS = "achievements"
    const val CREATE_REWARD = "create_reward"
    const val EDIT_REWARD = "edit_reward/{rewardId}"
    const val HISTORY = "history"

    /** Play's User Data policy requires the privacy policy to be reachable inside the app. */
    const val PRIVACY_POLICY = "privacy_policy"

    fun editReward(rewardId: Long) = "edit_reward/$rewardId"
}
