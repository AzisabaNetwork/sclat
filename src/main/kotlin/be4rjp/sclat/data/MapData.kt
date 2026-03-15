package be4rjp.sclat.data

import be4rjp.sclat.api.wiremesh.WiremeshListTask
import org.bukkit.Location

/**
 * Map metadata helper types. These store coordinates and world name without
 * creating Bukkit runtime objects. Runtime fields remain on MapData but are
 * left null until MapLoader instantiates them.
 */
data class LocMeta(
    val worldName: String?,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
)

data class PathMeta(
    val from: LocMeta,
    val to: LocMeta,
)

data class AreaMeta(
    val from: LocMeta,
    val to: LocMeta,
)

data class WiremeshMeta(
    val from: LocMeta,
    val to: LocMeta,
    val trapDoor: Boolean,
    val ironBars: Boolean,
    val fence: Boolean,
)

/**
 * Runtime map data. Metadata fields (XXXMeta) are populated at startup; the
 * runtime fields (Locations, Path/Area objects, WiremeshListTask) remain
 * nullable and are populated by MapLoader when the map is loaded for a match.
 */
class MapData(
    val mapName: String?,
) {
    // Metadata (parsed at startup)
    var team0LocMeta: LocMeta? = null
    var team1LocMeta: LocMeta? = null
    var introMeta: LocMeta? = null
    var team0IntroMeta: LocMeta? = null
    var team1IntroMeta: LocMeta? = null
    var resultLocMeta: LocMeta? = null
    var waitLocMeta: LocMeta? = null
    var noBlockLocMeta: LocMeta? = null
    var introMoveX: Double = 0.0
    var introMoveY: Double = 0.0
    var introMoveZ: Double = 0.0
    var pathMetaList: MutableList<PathMeta> = ArrayList()
    var areaMetaList: MutableList<AreaMeta> = ArrayList()
    var wiremeshMeta: WiremeshMeta? = null

    // Runtime (populated when MapLoader loads the map)
    var team0Loc: Location? = null
    var team1Loc: Location? = null
    var intro: Location? = null
    var isUsed: Boolean = false
    var worldName: String? = null
    var team0Intro: Location? = null
    var team1Intro: Location? = null
    var resultLoc: Location? = null
    private var canpaintBBlock = false
    var taikibayso: Location? = null
        private set
    var noBlockLocation: Location? = null
    var canAreaBattle: Boolean = false
    val pathList: MutableList<Path?> = ArrayList<Path?>()
    val areaList: MutableList<Area?> = ArrayList<Area?>()
    var wiremeshListTask: WiremeshListTask? = null
    var voidY: Int = 0

    fun canPaintBBlock(): Boolean = this.canpaintBBlock

    fun setCanPaintBBlock(v: Boolean) {
        this.canpaintBBlock = v
    }

    fun setTaikibasyo(basyo: Location?) {
        this.taikibayso = basyo
    }

    fun addPath(path: Path?) {
        this.pathList.add(path)
    }

    fun addArea(area: Area?) {
        this.areaList.add(area)
    }

    // Helpers for metadata
    fun addPathMeta(pathMeta: PathMeta) {
        this.pathMetaList.add(pathMeta)
    }

    fun addAreaMeta(areaMeta: AreaMeta) {
        this.areaMetaList.add(areaMeta)
    }
}
