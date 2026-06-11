package com.rewardpoints.app.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guards the "updates must preserve user data" guarantee.
 *
 * Creates a v1 database and runs the registered migrations all the way to the current
 * version, validating the resulting schema against the exported JSON in app/schemas at
 * each step. If a future version bump forgets to add a migration (which would otherwise
 * silently wipe user data on update), this test fails.
 *
 * Instrumented — run on a device/emulator (e.g. Waydroid):
 *     ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrateFrom1ToLatest_preservesSchemaAtEveryStep() {
        // Seed an empty v1 database, then migrate up to the current version. Validation of
        // the final schema against schemas/4.json is done by runMigrationsAndValidate.
        helper.createDatabase(testDb, 1).close()
        helper.runMigrationsAndValidate(
            testDb,
            AppDatabase.CURRENT_VERSION,
            /* validateDroppedTables = */ true,
            *AppDatabase.ALL_MIGRATIONS
        )
    }
}
