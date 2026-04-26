package be4rjp.sclat.api.wiremesh

import be4rjp.sclat.data.RegionBlocks
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.DelegatedLogger
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.scheduler.BukkitRunnable

class WiremeshListTask(
    private val firstPoint: Location,
    private val secondPoint: Location,
    trapDoor: Boolean,
    ironBars: Boolean,
    fence: Boolean,
) {
    private val blockList: MutableList<Block> = ArrayList()

    @JvmField
    val wiremeshsList: MutableList<Wiremesh?> = ArrayList()
    private val blockDataMap: MutableMap<Block, BlockData> = mutableMapOf()

    private var builderTask: BukkitRunnable? = null

    // stopRequested signals that a stop has been requested and the builder
    // should abort at the next convenient point. This avoids races between
    // the builder and MapLoader attempting to cancel it.
    private var stopRequested: Boolean = false
    val totalBlocks: Int

    init {
        val list = RegionBlocks(firstPoint, secondPoint).blocks

        for (block in list) {
            val isCandidate =
                (block.type == Material.IRON_TRAPDOOR && trapDoor) ||
                    (block.type == Material.IRON_BARS && ironBars) ||
                    (block.type.toString().contains("FENCE") && fence)

            if (!blockList.contains(block) && isCandidate) {
                val bData = block.blockData
                blockDataMap[block] = bData
                blockList.add(block)
            }
        }

        totalBlocks = blockList.size
    }

    fun startBuilding(batchSize: Int = 100) {
        if (builderTask != null) return

        builderTask =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        var processed = 0
                        while (processed < batchSize && blockList.isNotEmpty() && !stopRequested) {
                            val block = blockList.removeAt(0)
                            val bData = blockDataMap[block] ?: continue
                            val wm = Wiremesh(plugin, block, block.type, bData)
                            try {
                                wm.startTask()
                            } catch (e: Exception) {
                                // ignore
                            }
                            wiremeshsList.add(wm)
                            processed++
                        }

                        if (blockList.isEmpty() || stopRequested) {
                            logger.info("WiremeshListTask: finished building wiremesh (count=${wiremeshsList.size})")
                            this.cancel()
                            builderTask = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        this.cancel()
                        builderTask = null
                    }
                }
            }

        builderTask!!.runTaskTimer(plugin, 0L, 1L)
        logger.info("WiremeshListTask: started incremental build (total=$totalBlocks)")
    }

    fun stopTask() {
        // Request the builder to stop and cancel scheduled runs. We keep the
        // method idempotent and resilient to races by cancelling again when
        // a watcher observes no more work.
        stopRequested = true
        try {
            builderTask?.cancel()
        } catch (_: Exception) {
        }
        // Clear the reference so callers checking builderTask know there's no
        // scheduled task left. The actual builder may still be running this
        // tick and may append a small number of additional Wiremesh objects;
        // callers should re-invoke stopTask() after waiting for the builder to
        // finish to ensure those are also stopped.
        builderTask = null

        try {
            for (obj in wiremeshsList) obj?.stopTask()
        } catch (_: Exception) {
        }
    }

    /**
     * Returns true when the builder is either scheduled or there are pending
     * candidate blocks to process.
     */
    fun isWorking(): Boolean = (builderTask != null) || blockList.isNotEmpty()

    companion object {
        private val logger by DelegatedLogger()
    }
}
