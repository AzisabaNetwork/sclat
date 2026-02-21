package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class WiremeshConfig(
    val trapDoor: Boolean = false,
    val ironBars: Boolean = false,
    val fence: Boolean = false,
    val from: LocationConfig = LocationConfig(),
    val to: LocationConfig = LocationConfig(),
)
