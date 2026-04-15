package net.azisaba.sclat.core.status

data class ServerStatus(
    var isOnline: Boolean,
    var isMaintenance: Boolean,
    var displayName: String,
    var matchStartTime: Long,
    var playerCount: Int,
    var waitingEndTime: Long,
    var runningMatch: Boolean,
)
