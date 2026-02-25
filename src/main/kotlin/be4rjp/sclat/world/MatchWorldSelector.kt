package be4rjp.sclat.world

import be4rjp.sclat.data.MapData

/**
 * This class won't persist automatically. Do it yourself.
 */
class MatchWorldSelector(
    private val worlds: MutableMap<String, MapData> = mutableMapOf(),
) {
    /**
     * Add new match map to selector
     *
     * @param MapData
     */
    fun addMap(mapData: MapData) {
        worlds[mapData.worldName!!] = mapData
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
    fun randomMap(): MapData = worlds.entries.random().value

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
