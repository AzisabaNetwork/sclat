package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class LocationConfig(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val yaw: Double? = null,
    val pitch: Double? = null,
)
