package be4rjp.sclat.config

data class MapsConfig(
    val maps: Map<String, MapConfig> = mapOf("SimpleMapForTesting" to MapConfig()),
)
