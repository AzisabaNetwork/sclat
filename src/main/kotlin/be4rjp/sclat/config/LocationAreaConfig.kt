package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class LocationAreaConfig(
    val from: LocationConfig = LocationConfig(),
    val to: LocationConfig = LocationConfig(),
)
