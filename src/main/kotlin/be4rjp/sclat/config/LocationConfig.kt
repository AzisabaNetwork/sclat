package be4rjp.sclat.config

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class LocationConfig(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val yaw: Float? = null,
    val pitch: Float? = null,
) {
    fun toLocation(world: World): Location {
        val loc = Location(world, x, y, z)
        if (yaw != null) loc.yaw = yaw
        if (pitch != null) loc.pitch = pitch
        return loc
    }
}
