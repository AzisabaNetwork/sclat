package be4rjp.sclat.world

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator

object WorldApi {
    /**
     * Check exists world folder
     *
     * @param worldName
     * @return
     */
    fun existWorldFolder(worldName: String): Boolean = Bukkit.getWorldContainer().resolve(worldName).exists()

    /**
     * Load the world
     *
     * @param worldName target world's name
     * @return loaded world instance. if failure, returns null.
     */
    fun loadWorld(worldName: String): World? = Bukkit.createWorld(WorldCreator(worldName))

    /**
     * Unload specific world
     *
     * @param world target world instance
     * @param save needs to save
     * @return is succeeded
     */
    fun unloadWorld(
        world: World,
        save: Boolean = false,
    ): Boolean = Bukkit.unloadWorld(world, save)

    /**
     * Unload specific world
     *
     * @param worldName target world's name
     * @param save needs to save
     * @return is succeeded. if world isn't found, returns null.
     */
    fun unloadWorld(
        worldName: String,
        save: Boolean = false,
    ): Boolean? = Bukkit.getWorld(worldName)?.let { Bukkit.unloadWorld(it, save) }
}
