package be4rjp.sclat.data

import be4rjp.sclat.api.SclatUtil
import net.minecraft.server.v1_14_R1.PacketPlayOutMultiBlockChange
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class BlockUpdater(
    private val plugin: JavaPlugin,
) {
    private val blocklist: MutableMap<Block, Material> = mutableMapOf()
    private val blocks: MutableList<Block> = mutableListOf()
    private val task: BukkitRunnable
    private var maxBlock = 30

    init {
        task =
            object : BukkitRunnable() {
                var c: Int = 0
                var i: Int = 0

                override fun run() {
                    try {
                        val tb = blocks.subList(c, blocks.size)

                        val chunkBlockMap: MutableMap<Chunk, MutableList<Block>> = mutableMapOf()

                        loop@ for (block in tb) {
                            // Sclat.setBlockByNMS(block, blocklist.get(block), true);
                            if (block.location.chunk.isLoaded) {
                                try {
                                    // Sclat.setBlockByNMSChunk(block, blocklist.get(block), true);

                                    val list: MutableList<Block> = ArrayList()
                                    val up = block.getRelative(BlockFace.UP)
                                    val west = block.getRelative(BlockFace.WEST)
                                    val east = block.getRelative(BlockFace.EAST)
                                    val south = block.getRelative(BlockFace.SOUTH)
                                    val north = block.getRelative(BlockFace.NORTH)
                                    val down = block.getRelative(BlockFace.DOWN)
                                    list.add(up)
                                    list.add(west)
                                    list.add(east)
                                    list.add(south)
                                    list.add(north)
                                    list.add(down)

                                    check@ for (cb in list) {
                                        if (cb.type == Material.AIR) {
                                            // Sclat.sendBlockChangeForAllPlayer(block, blocklist.get(block));
                                            chunkBlockMap
                                                .computeIfAbsent(block.chunk) { chunk: Chunk? -> mutableListOf() }
                                                .add(block)
                                            continue
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                            } else {
                            }
                            // block.setType(blocklist.get(block));
                            c++
                            i++
                            if (i == maxBlock) {
                                i = 0
                                break
                            }
                        }

                        // Use multi block change
                        for (entry in chunkBlockMap.entries) {
                            val chunk: Chunk = entry.key
                            val blocks: MutableList<Block> = entry.value

                            val positionArray = ShortArray(blocks.size)
                            for ((i, block) in blocks.withIndex()) {
                                positionArray[i] =
                                    ((block.x and 0xF) shl 12 or ((block.z and 0xF) shl 8) or block.y).toShort()
                            }
                            val packet =
                                PacketPlayOutMultiBlockChange(
                                    positionArray.size,
                                    positionArray,
                                    (chunk as CraftChunk).handle,
                                )
                            for (target in plugin.server.onlinePlayers) {
                                if (target.world === chunk.world) {
                                    (target as CraftPlayer).handle.playerConnection.sendPacket(packet)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
    }

    fun setBlock(
        block: Block,
        material: Material,
    ) {
        if (this.blocks.contains(block)) {
            if (this.blocklist[block] == material) {
                return
            }
        }

        if (!this.blocks.contains(block)) {
            this.blocklist[block] = material
            this.blocks.add(block)

            if (block.location.chunk.isLoaded) {
                try {
                    SclatUtil.setBlockByNMSChunk(block, blocklist[block]!!, true)
                } catch (e: Exception) {
                }
            } else {
                try {
                    SclatUtil.setBlockByNMS(block, blocklist[block]!!, true)
                    // Main.getPlugin().getServer().broadcastMessage("ChangeBlockByNMS!!");
                } catch (e: Exception) {
                }
            }
        } else {
            if (this.blocklist[block] != material) {
                // this.blocklist.put(block, material);
                this.blocklist.replace(block, material)
                this.blocks.add(block)

                if (block.location.chunk.isLoaded) {
                    try {
                        SclatUtil.setBlockByNMSChunk(block, blocklist[block]!!, true)
                    } catch (e: Exception) {
                    }
                } else {
                    try {
                        SclatUtil.setBlockByNMS(block, blocklist[block]!!, true)
                        // Main.getPlugin().getServer().broadcastMessage("ChangeBlockByNMS!!");
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun start() {
        task.runTaskTimer(plugin, 0, 2)
    }

    fun stop() {
        task.cancel()
    }

    fun setMaxBlockInOneTick(i: Int) {
        this.maxBlock = i
    }
}
