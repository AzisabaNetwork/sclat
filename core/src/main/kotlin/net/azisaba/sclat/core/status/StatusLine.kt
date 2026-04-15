package net.azisaba.sclat.core.status

import net.azisaba.sclat.core.extension.currentTimeSeconds

object StatusLine {
    fun getLine(status: ServerStatus): String? {
        if (status.isMaintenance || !status.isOnline) return null
        return " ${status.displayName}: §r" +
            if (status.runningMatch) {
                val elapsed = currentTimeSeconds().minus(status.matchStartTime)
                "${status.playerCount}§e人が試合中" +
                    if (elapsed < 10000) " §r(${elapsed.div(60)}:${String.format("%02d", elapsed.mod(60))})" else ""
            } else {
                "${status.playerCount}§a人が待機中" +
                    if (status.waitingEndTime != 0L) {
                        " §r(§b${status.waitingEndTime.minus(currentTimeSeconds())}§r秒後に開始)"
                    } else {
                        ""
                    }
            }
    }
}
