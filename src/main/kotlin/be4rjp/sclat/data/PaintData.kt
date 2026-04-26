package be4rjp.sclat.data

import net.azisaba.sclat.core.team.SclatTeam
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
    var team: SclatTeam? = null

    var originalState: BlockState? = null

    @JvmField
    var blockData: BlockData? = null

    fun setOrigianlType(material: Material?) {
        this.originalType = material
    }
}
