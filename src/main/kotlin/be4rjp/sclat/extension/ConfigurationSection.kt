package be4rjp.sclat.extension

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

fun ConfigurationSection.getLocation(
    path: String,
    world: World,
): Location {
    val x = getDouble("$path.X") + 0.5
    val y = getDouble("$path.Y")
    val z = getDouble("$path.Z") + 0.5
    return Location(world, x, y, z)
}

fun ConfigurationSection.getLocationWithYaw(
    path: String,
    world: World,
): Location {
    val loc = getLocation(path, world)
    loc.yaw = getInt("$path.Yaw").toFloat()
    return loc
}

fun ConfigurationSection.getLocationWithPitch(
    path: String,
    world: World,
): Location {
    val loc = getLocationWithYaw(path, world)
    loc.pitch = getInt("$path.Pitch", 0).toFloat()
    return loc
}

fun ConfigurationSection.getSection(path: String): ConfigurationSection? = getConfigurationSection(path)
