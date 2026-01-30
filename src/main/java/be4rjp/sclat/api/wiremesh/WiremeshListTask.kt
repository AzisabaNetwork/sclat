package be4rjp.sclat.api.wiremesh

import be4rjp.sclat.data.RegionBlocks
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import java.util.function.Consumer

/**
 *
 * @author Be4rJP
 */
class WiremeshListTask(
    private val firstPoint: Location,
    private val secondPoint: Location,
    trapDoor: Boolean,
    ironBars: Boolean,
    fence: Boolean,
) {
    private val blockList: MutableList<Block> = ArrayList<Block>()

    @JvmField
    val wiremeshsList: MutableList<Wiremesh?> = ArrayList<Wiremesh?>()
    private val blockDataMap: MutableMap<Block, BlockData> = mutableMapOf()

    init {
        // 先に対象のブロックとそのBlockDataを取得して保存しておく
        val list = RegionBlocks(firstPoint, secondPoint).getBlocks()

        for (block in list) {
            if (!blockList.contains(block) && (
                    (block.getType() == Material.IRON_TRAPDOOR && trapDoor) ||
                        (block.getType() == Material.IRON_BARS && ironBars) ||
                        (block.getType().toString().contains("FENCE") && fence)
                    )
            ) {
                val bData = block.getBlockData()
                blockDataMap.put(block, bData)
                blockList.add(block)
            }
        }

        // Wiremeshを作成してタスクを実行
        for (block in blockList) {
            val bData = blockDataMap.get(block)
            val wm = Wiremesh(block, block.getType(), bData!!)
            wiremeshsList.add(wm)
        }
    }

    fun stopTask() {
        wiremeshsList.forEach(Consumer { obj: Wiremesh? -> obj!!.stopTask() })
    }
}
