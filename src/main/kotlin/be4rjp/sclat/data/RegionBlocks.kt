package be4rjp.sclat.data

import org.apache.commons.lang.Validate
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.Vector

class RegionBlocks(
    firstPoint: Location,
    secondPoint: Location,
) {
    private val world: World?

    private val maximum: Vector
    private val minimum: Vector

    init {
        Validate.isTrue(
            firstPoint.world != null && firstPoint.world == secondPoint.world,
            "World can't be null or different.",
        )
        world = firstPoint.world
        val firstVector = firstPoint.toVector()
        val secondVector = secondPoint.toVector()
        maximum = Vector.getMaximum(firstVector, secondVector)
        minimum = Vector.getMinimum(firstVector, secondVector)
    }

    val blocks: MutableList<Block>
        get() {
            val blocks: MutableList<Block> = mutableListOf()
            for (y in minimum.blockY..maximum.blockY) {
                for (x in minimum.blockX..maximum.blockX) {
                    for (z in minimum.blockZ..maximum.blockZ) {
                        blocks.add(
                            world!!.getBlockAt(x, y, z),
                        )
                    }
                }
            }

            return blocks
        }
}
