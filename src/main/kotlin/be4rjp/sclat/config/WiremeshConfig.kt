package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class WiremeshConfig(
    val trapDoor: Boolean,
    val ironBars: Boolean,
    val fence: Boolean,
    val from: LocationConfig,
    val to: LocationConfig,
)
