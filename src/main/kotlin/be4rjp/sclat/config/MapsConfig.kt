package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class MapsConfig(
    val maps: Map<String, MapConfig> = mapOf("SimpleMapForTesting" to MapConfig()),
)
