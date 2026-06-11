package com.rewardpoints.app.data.local.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests the recovery control-flow used to survive an undecryptable EncryptedSharedPreferences
 * file after a device restore (the AndroidKeyStore master key isn't backed up). The Android
 * glue (EncryptedSharedPreferences) can't run on the JVM, but the retry/wipe logic is the
 * part with the bugs, and it is pure.
 *
 * Semantics under test:
 *  1. First open fails → retry once WITHOUT wiping. The Android Keystore is known to fail
 *     transiently (right after boot, device momentarily locked); a one-off flake must not
 *     destroy the user's stored tokens.
 *  2. Retry also fails → the file is treated as genuinely undecryptable: wipe once, then
 *     open again.
 *  3. Post-wipe open fails too → propagate (genuinely unrecoverable).
 */
class SecretStorageRecoveryTest {

    @Test
    fun `does not wipe when the first open succeeds`() {
        var attempts = 0
        var wiped = false

        val result = openWithRecovery(
            open = { attempts++; "opened" },
            onCorrupt = { wiped = true }
        )

        assertEquals("opened", result)
        assertEquals(1, attempts)
        assertFalse("must not wipe a healthy store", wiped)
    }

    @Test
    fun `transient failure recovers via plain retry without wiping`() {
        var attempts = 0
        var wiped = false

        val result = openWithRecovery(
            open = {
                attempts++
                if (attempts == 1) throw IllegalStateException("transient Keystore flake")
                "opened"
            },
            onCorrupt = { wiped = true }
        )

        assertEquals("opened", result)
        assertEquals(2, attempts)
        assertFalse("a transient failure must NOT wipe the user's stored secrets", wiped)
    }

    @Test
    fun `persistent failure wipes once and reopens`() {
        var attempts = 0
        var wiped = false

        val result = openWithRecovery(
            open = {
                attempts++
                if (attempts <= 2) throw IllegalStateException("undecryptable after restore")
                "opened"
            },
            onCorrupt = { wiped = true }
        )

        assertEquals("opened", result)
        assertEquals(3, attempts)
        assertTrue("should have wiped the corrupt file before the final open", wiped)
    }

    @Test
    fun `propagates when even the post-wipe open fails`() {
        var wiped = false

        try {
            openWithRecovery<String>(
                open = { throw IllegalStateException("hard failure") },
                onCorrupt = { wiped = true }
            )
            fail("expected the final failure to propagate")
        } catch (e: IllegalStateException) {
            assertEquals("hard failure", e.message)
        }
        assertTrue("the wipe should still have been attempted", wiped)
    }
}
