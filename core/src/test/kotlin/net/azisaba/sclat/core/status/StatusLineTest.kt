package net.azisaba.sclat.core.status

import io.kotest.core.spec.style.StringSpec
import net.azisaba.sclat.core.extension.currentTimeSeconds
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StatusLineTest :
    StringSpec({
        "returns null when maintenance" {
            val status =
                ServerStatus(
                    isOnline = true,
                    isMaintenance = true,
                    displayName = "srv",
                    matchStartTime = 0L,
                    playerCount = 0,
                    waitingEndTime = 0L,
                    runningMatch = false,
                )
            assertNull(StatusLine.getLine(status))
        }

        "returns null when offline" {
            val status =
                ServerStatus(
                    isOnline = false,
                    isMaintenance = false,
                    displayName = "srv",
                    matchStartTime = 0L,
                    playerCount = 0,
                    waitingEndTime = 0L,
                    runningMatch = false,
                )
            assertNull(StatusLine.getLine(status))
        }

        "waiting without waitingEndTime shows waiting and no countdown" {
            val status =
                ServerStatus(
                    isOnline = true,
                    isMaintenance = false,
                    displayName = "Test",
                    matchStartTime = 0L,
                    playerCount = 5,
                    waitingEndTime = 0L,
                    runningMatch = false,
                )
            val line = StatusLine.getLine(status)
            val l = assertNotNull(line)
            val expected = " ${status.displayName}: §r${status.playerCount}§a人が待機中"
            assertEquals(l, expected, "expected='$expected' but was='$l'")
        }

        "waiting with waitingEndTime shows countdown exact string" {
            val now = currentTimeSeconds()
            val wait = 42L
            val status =
                ServerStatus(
                    isOnline = true,
                    isMaintenance = false,
                    displayName = "W",
                    matchStartTime = 0L,
                    playerCount = 3,
                    waitingEndTime = now + wait,
                    runningMatch = false,
                )
            // compute expected using the same captured `now` to avoid second-boundary drift
            val remaining = status.waitingEndTime - now
            val expected = " ${status.displayName}: §r${status.playerCount}§a人が待機中 §r(§b$remaining§r秒後に開始)"
            val line = StatusLine.getLine(status)
            val l2 = assertNotNull(line)
            assertEquals(l2, expected, "expected='$expected' but was='$l2'")
        }

        "running match shows elapsed time when small exact string" {
            val now = currentTimeSeconds()
            val elapsedSet = 125L // 2:05
            val status =
                ServerStatus(
                    isOnline = true,
                    isMaintenance = false,
                    displayName = "R",
                    matchStartTime = now - elapsedSet,
                    playerCount = 8,
                    waitingEndTime = 0L,
                    runningMatch = true,
                )
            // compute expected using captured `now` so elapsed is deterministic
            val elapsed = now - status.matchStartTime
            val minutes = elapsed / 60
            val seconds = elapsed % 60
            val expected = " ${status.displayName}: §r${status.playerCount}§e人が試合中 §r($minutes:${String.format("%02d", seconds)})"
            val line = StatusLine.getLine(status)
            val l3 = assertNotNull(line)
            assertEquals(l3, expected, "expected='$expected' but was='$l3'")
        }

        "running match long elapsed hides elapsed display exact string" {
            val now = currentTimeSeconds()
            val status =
                ServerStatus(
                    isOnline = true,
                    isMaintenance = false,
                    displayName = "RL",
                    matchStartTime = now - 20000L,
                    playerCount = 2,
                    waitingEndTime = 0L,
                    runningMatch = true,
                )
            val expected = " ${status.displayName}: §r${status.playerCount}§e人が試合中"
            val line = StatusLine.getLine(status)
            val l4 = assertNotNull(line)
            assertEquals(l4, expected, "expected='$expected' but was='$l4'")
        }
    })
