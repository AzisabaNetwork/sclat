package be4rjp.sclat.world

import net.minecraft.server.v1_14_R1.WorldData

/**
 * This class won't persist automatically. Do it yourself.
 */
class MatchWorldSelector(
    private val worlds: MutableMap<String, WorldData> = mutableMapOf(),
) {
    /**
     * Add new match map to selector
     *
     * @param worldData
     */
    fun addMap(worldData: WorldData) {
        worlds[worldData.name] = worldData
    }

    /**
     * Is this map already exists added to this selector
     *
     * @param mapName target match map name
     */
    fun existMap(mapName: String) = worlds.containsKey(mapName)

    /**
     * Get a random match map
     *
     * @return selected world data
     */
    fun randomMap(): WorldData = worlds.entries.random().value

    /**
     * Get all match map names
     *
     * @return set of world names
     */
    fun allMapNames(): Set<String> = worlds.keys.toSet()

    /**
     * Get current match map count
     *
     * @return match map count
     */
    fun size(): Int = worlds.size
}
