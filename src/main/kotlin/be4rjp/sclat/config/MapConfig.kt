package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class MapConfig(
    val worldName: String,
    val intro: LocationConfig,
    val team0: LocationConfig,
    val team1: LocationConfig,
    val team0IntroLoc: LocationConfig,
    val team1IntroLoc: LocationConfig,
    val resultLoc: LocationConfig,
    val waitLoc: LocationConfig,
    val noBlockLoc: LocationConfig,
    val path: Map<String, LocationAreaConfig>,
    val area: Map<String, LocationAreaConfig>,
    val wiremesh: WiremeshConfig?,
    val canPaintBarrierBlock: Boolean = false,
)
