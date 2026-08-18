package com.statup.app.data.repository

import com.statup.app.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

/**
 * Narrow read-only view of the player's current state. Lets collaborators that only need the
 * username + a one-shot stats snapshot (e.g. [com.statup.app.ai.AgentContextBuilder])
 * depend on this slice instead of the full [PlayerRepository], which keeps them unit-testable
 * without a Room/Context-backed repository.
 */
interface PlayerStateProvider {
    val username: Flow<String>
    suspend fun getStatsOnce(): PlayerStats?
}
