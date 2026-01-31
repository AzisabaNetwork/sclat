package be4rjp.sclat.data

import be4rjp.sclat.api.team.Team
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData

/**
 *
 * @author Be4rJP
 */
class PaintData(
    val block: Block?,
) {
    @JvmField
    var match: Match? = null

    var originalType: Material? = null
        private set

    @JvmField
    var team: Team? = null

    var originalState: BlockState? = null

    @JvmField
    var blockData: BlockData? = null

    fun setOrigianlType(material: Material?) {
        this.originalType = material
    }
}
