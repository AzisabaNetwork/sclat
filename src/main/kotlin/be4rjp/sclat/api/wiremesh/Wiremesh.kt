package be4rjp.sclat.api.wiremesh

import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.EntityFallingBlock
import net.minecraft.server.v1_14_R1.IBlockData
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata
import net.minecraft.server.v1_14_R1.PacketPlayOutMount
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntity
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.Random

/**
 *
 * @author Be4rJP
 */
class Wiremesh(
    private val plugin: JavaPlugin,
    private val block: Block,
    private val originalType: Material,
    private val blockData: BlockData,
) : BukkitRunnable() {
    private val fb: EntityFallingBlock
    private val `as`: EntityArmorStand
    private val ibd: IBlockData?

    private val playerList: MutableList<Player?> = ArrayList()

    private val despawn = true
    private val spawn = false

    init {
        this.ibd = (blockData as CraftBlockData).state

        block.type = Material.AIR

        val nmsWorld = (block.world as CraftWorld).handle
        val loc = block.location
        val blockData = block.blockData
        val ibd = (blockData as CraftBlockData).state
        fb = EntityFallingBlock(nmsWorld, loc.x + 0.5, loc.y - 0.02, loc.z + 0.5, ibd)
        fb.isNoGravity = true
        fb.ticksLived = 1

        `as` = EntityArmorStand(nmsWorld, loc.x + 0.5, loc.y, loc.z + 0.5)
        `as`.isNoGravity = true
        `as`.isMarker = true
        `as`.isInvisible = true
        fb.startRiding(`as`)

        for (player in plugin.server.onlinePlayers) {
            if (block.world !== player.world) continue

            player.sendBlockChange(block.location, blockData)
        }
    }

    override fun run() {
        try {
            playerList.removeIf { player: Player? -> !player!!.isOnline }

            for (player in plugin.server.onlinePlayers) {
                if (block.world !== player.world) continue

                // 透過条件
                val `is` = player.inventory.itemInMainHand.type == Material.AIR

                val entityPlayer = (player as CraftPlayer).handle

                if (block.location.distanceSquared(player.location) <= 25) { // 5*5
                    if (`is`) {
                        player.sendBlockChange(block.location, Material.AIR.createBlockData())
                    } else {
                        player.sendBlockChange(block.location, blockData)
                    }

                    if (!playerList.contains(player)) {
                        val fbPacket =
                            PacketPlayOutSpawnEntity(
                                fb,
                                net.minecraft.server.v1_14_R1.Block
                                    .getCombinedId(ibd),
                            )
                        entityPlayer.playerConnection.sendPacket(fbPacket)
                        val asPacket = PacketPlayOutSpawnEntityLiving(`as`)
                        entityPlayer.playerConnection.sendPacket(asPacket)
                        val dataWatcher = fb.dataWatcher
                        val metadata =
                            PacketPlayOutEntityMetadata(
                                fb.bukkitEntity.entityId,
                                dataWatcher,
                                true,
                            )
                        entityPlayer.playerConnection.sendPacket(metadata)
                        val mount = PacketPlayOutMount(`as`)
                        entityPlayer.playerConnection.sendPacket(mount)

                        playerList.add(player)
                    }
                } else {
                    if (Random().nextInt(5) == 0) {
                        player.sendBlockChange(block.location, blockData)
                    }

                    if (playerList.contains(player)) {
                        val fbPacket =
                            PacketPlayOutEntityDestroy(
                                fb.bukkitEntity.entityId,
                            )
                        entityPlayer.playerConnection.sendPacket(fbPacket)
                        val asPacket =
                            PacketPlayOutEntityDestroy(
                                `as`.bukkitEntity.entityId,
                            )
                        entityPlayer.playerConnection.sendPacket(asPacket)
                        player.sendBlockChange(block.location, blockData)

                        playerList.remove(player)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTask() {
        this.runTaskTimerAsynchronously(plugin, 0, 5)
    }

    fun stopTask() {
        this.cancel()
        this.block.type = originalType
        this.block.blockData = blockData
    }
}
