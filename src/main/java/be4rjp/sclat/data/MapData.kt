package be4rjp.sclat.data

import be4rjp.sclat.api.wiremesh.WiremeshListTask
import org.bukkit.Location

/**
 *
 * @author Be4rJP
 */
class MapData(
    val mapName: String?,
) {
    var team0Loc: Location? = null

    var team1Loc: Location? = null

    var intro: Location? = null

    var isUsed: Boolean = false

    var worldName: String? = null

    var team0Intro: Location? = null

    var team1Intro: Location? = null

    var resultLoc: Location? = null

    var introMoveX: Double = 0.0

    var introMoveY: Double = 0.0

    var introMoveZ: Double = 0.0

    private var canpaintBBlock = false

    var taikibayso: Location? = null
        private set

    var noBlockLocation: Location? = null

    var canAreaBattle: Boolean = false
        set(v) {
            field = `v`
        }

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
}
