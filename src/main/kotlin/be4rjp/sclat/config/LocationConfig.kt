package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class LocationConfig(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double?,
    val pitch: Double?,
)
