package dev.statup.app.rpg

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure tests for the priority→points table.
 * Todoist inverts: their API `priority=4` is the user-facing "p1 urgent".
 */
class StatsEngineTest {

    @Test fun `p1 urgent maps to 4 points`() {
        assertEquals(4, StatsEngine.calculateTaskPoints(4))
    }

    @Test fun `p2 high maps to 3 points`() {
        assertEquals(3, StatsEngine.calculateTaskPoints(3))
    }

    @Test fun `p3 medium maps to 2 points`() {
        assertEquals(2, StatsEngine.calculateTaskPoints(2))
    }

    @Test fun `p4 normal maps to 1 point`() {
        assertEquals(1, StatsEngine.calculateTaskPoints(1))
    }

    @Test fun `out-of-range priority defaults to 1 point`() {
        assertEquals(1, StatsEngine.calculateTaskPoints(0))
        assertEquals(1, StatsEngine.calculateTaskPoints(5))
        assertEquals(1, StatsEngine.calculateTaskPoints(-1))
    }
}
