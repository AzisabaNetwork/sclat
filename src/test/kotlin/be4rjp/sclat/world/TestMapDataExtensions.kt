package be4rjp.sclat.world

import be4rjp.sclat.data.MapData

class TestMapData(
    val mapName: String,
    val worldName: String? = mapName,
) {
    fun getName() = mapName
    fun getWorldName() = worldName
}

fun MapData?.toTestMapData(): TestMapData? {
    if (this == null) return null
    return TestMapData(mapName ?: "", worldName ?: mapName)
}

fun TestMapData.toMapData(): MapData {
    return object : MapData(mapName) {}
}

fun MutableList<TestMapData>.toMapDataList(): MutableList<MapData> {
    return this.map { it.toMapData() }.toMutableList()
}

fun MapData?.toTestMapDataOrNull(): TestMapData? {
    return this?.let { TestMapData(it.mapName ?: "", it.worldName ?: it.mapName) }
}

fun Collection<MapData>.toTestMapDataList(): List<TestMapData> {
    return this.map { TestMapData(it.mapName ?: "", it.worldName ?: it.mapName) }
}

fun MapData?.getTestMapName(): String? {
    return this?.mapName
}

fun TestMapData?.getTestWorldName(): String? {
    return this?.worldName
}