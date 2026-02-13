@file:UseSerializers(UUIDSerializer::class)

package be4rjp.sclat.api.utils

import be4rjp.sclat.api.serializer.UUIDSerializer
import be4rjp.sclat.extension.ZONE_TOKYO
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Serializable
data class DailyRefreshSet(
    var nextResetEpoch: Long = 0L,
    var uuids: MutableSet<UUID> = mutableSetOf(),
) {
    private fun calculateNextReset(): Long =
        ZonedDateTime
            .now(ZONE_TOKYO)
            .truncatedTo(ChronoUnit.DAYS) // today 00:00:00
            .plusDays(1) // tomorrow 00:00:00
            .toInstant()
            .toEpochMilli()

    private fun checkRefresh() {
        if (System.currentTimeMillis() >= nextResetEpoch) {
            uuids.clear()
            nextResetEpoch = calculateNextReset()
        }
    }

    operator fun plus(uuid: UUID) {
        checkRefresh()
        uuids.add(uuid)
    }

    operator fun minus(uuid: UUID) {
        checkRefresh()
        uuids.remove(uuid)
    }

    operator fun contains(uuid: UUID) = uuids.contains(uuid)
}
