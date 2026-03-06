package be4rjp.sclat.util

import be4rjp.sclat.api.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
internal data class WithUUID(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
)
