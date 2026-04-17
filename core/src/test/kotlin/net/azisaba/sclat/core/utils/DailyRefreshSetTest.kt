package net.azisaba.sclat.core.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DailyRefreshSetTest {

    private lateinit var dailyRefreshSet: DailyRefreshSet

    @BeforeEach
    fun setUp() {
        dailyRefreshSet = DailyRefreshSet()
    }

    @Test
    fun `test adding UUID`() {
        val uuid = UUID.randomUUID()
        dailyRefreshSet + uuid
        assertTrue(uuid in dailyRefreshSet)
    }

    @Test
    fun `test removing UUID`() {
        val uuid = UUID.randomUUID()
        dailyRefreshSet + uuid
        dailyRefreshSet - uuid
        assertFalse(uuid in dailyRefreshSet)
    }

    @Test
    fun `test refresh clears UUIDs`() {
        val uuid = UUID.randomUUID()
        dailyRefreshSet + uuid
        dailyRefreshSet.nextResetEpoch = System.currentTimeMillis() - 1 // Force refresh
        dailyRefreshSet + UUID.randomUUID() // Trigger refresh
        assertFalse(uuid in dailyRefreshSet)
    }

    @Test
    fun `test next reset epoch updates`() {
        val initialResetEpoch = dailyRefreshSet.nextResetEpoch
        dailyRefreshSet.nextResetEpoch = System.currentTimeMillis() - 1 // Force refresh
        dailyRefreshSet + UUID.randomUUID() // Trigger refresh
        assertTrue(dailyRefreshSet.nextResetEpoch > initialResetEpoch)
    }
}