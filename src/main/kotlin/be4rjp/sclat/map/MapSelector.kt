package be4rjp.sclat.map

/**
 * Selector for match maps. It manages the maps and their usage status.
 */
object MapSelector {
    private val maps: MutableSet<String> = mutableSetOf()
    private val usingMaps: MutableSet<String> = mutableSetOf()

    fun addMap(mapName: String) {
        maps.add(mapName)
    }

    fun removeMap(mapName: String) {
        maps.remove(mapName)
    }

    fun getAllMaps(): Set<String> = maps.toSet()

    /**
     * get random map and mark its using.
     *
     * @return the name of the selected map, or null if no maps are available
     */
    fun selectRandomMap(): String {
        if (maps.isEmpty()) throw RuntimeException("No maps available.")
        return maps.filter { s -> !usingMaps.contains(s) }.random().also { markUsed(it) }
    }

    fun markUsed(mapName: String) {
        usingMaps.add(mapName)
    }

    fun markUnused(mapName: String) {
        usingMaps.remove(mapName)
    }
}
