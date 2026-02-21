package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class MapConfig(
    val worldName: String = "newmap",
    val intro: LocationConfig = LocationConfig(),
    val team0: LocationConfig = LocationConfig(),
    val team1: LocationConfig = LocationConfig(),
    val team0IntroLoc: LocationConfig = LocationConfig(),
    val team1IntroLoc: LocationConfig = LocationConfig(),
    val resultLoc: LocationConfig = LocationConfig(),
    val waitLoc: LocationConfig = LocationConfig(),
    val noBlockLoc: LocationConfig = LocationConfig(),
    val path: Map<String, LocationAreaConfig> = mapOf("pathA" to LocationAreaConfig()),
    val area: Map<String, LocationAreaConfig> = mapOf("areaA" to LocationAreaConfig()),
    val wiremesh: WiremeshConfig? = null,
    val canPaintBarrierBlock: Boolean? = null,
)
