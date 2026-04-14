package net.azisaba.sclat.core.status

data class ServerStatus(
    var isOnline: Boolean,
    val isMaintenance: Boolean,
    val displayName: String,
    var matchStartTime: Long,
    var playerCount: Int,
    var waitingEndTime: Long,
)
