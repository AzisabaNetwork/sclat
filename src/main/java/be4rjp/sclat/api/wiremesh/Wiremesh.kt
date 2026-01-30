package be4rjp.sclat.api.wiremesh

import be4rjp.sclat.Sclat
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
import org.bukkit.scheduler.BukkitRunnable
import java.util.Random

/**
 *
 * @author Be4rJP
 */
class Wiremesh(private val block: Block, private val originalType: Material, private val blockData: BlockData) :
    BukkitRunnable() {
    private val fb: EntityFallingBlock
    private val `as`: EntityArmorStand
    private val ibd: IBlockData?

    private val playerList: MutableList<Player?> = ArrayList<Player?>()

    private val despawn = true
    private val spawn = false

    init {
        this.ibd = (blockData as CraftBlockData).getState()

        block.setType(Material.AIR)

        val nmsWorld = (block.getWorld() as CraftWorld).getHandle()
        val loc = block.getLocation()
        val blockData = block.getBlockData()
        val ibd = (blockData as CraftBlockData).getState()
        fb = EntityFallingBlock(nmsWorld, loc.getX() + 0.5, loc.getY() - 0.02, loc.getZ() + 0.5, ibd)
        fb.setNoGravity(true)
        fb.ticksLived = 1

        `as` = EntityArmorStand(nmsWorld, loc.getX() + 0.5, loc.getY(), loc.getZ() + 0.5)
        `as`.setNoGravity(true)
        `as`.setMarker(true)
        `as`.setInvisible(true)
        fb.startRiding(`as`)

        for (player in Sclat.getPlugin().getServer().getOnlinePlayers()) {
            if (block.getWorld() !== player.getWorld()) continue

            player.sendBlockChange(block.getLocation(), blockData)
        }
    }

    override fun run() {
        try {
            playerList.removeIf { player: Player? -> !player!!.isOnline() }

            for (player in Sclat.getPlugin().getServer().getOnlinePlayers()) {
                if (block.getWorld() !== player.getWorld()) continue

                // 透過条件
                val `is` = player.getInventory().getItemInMainHand().getType() == Material.AIR

                val entityPlayer = (player as CraftPlayer).getHandle()

                if (block.getLocation().distanceSquared(player.getLocation()) <= 25 /* 5*5 */) {
                    if (`is`) {
                        player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData())
                    } else {
                        player.sendBlockChange(block.getLocation(), blockData)
                    }

                    if (!playerList.contains(player)) {
                        val fbPacket = PacketPlayOutSpawnEntity(
                            fb,
                            net.minecraft.server.v1_14_R1.Block.getCombinedId(ibd),
                        )
                        entityPlayer.playerConnection.sendPacket(fbPacket)
                        val asPacket = PacketPlayOutSpawnEntityLiving(`as`)
                        entityPlayer.playerConnection.sendPacket(asPacket)
                        val dataWatcher = fb.getDataWatcher()
                        val metadata = PacketPlayOutEntityMetadata(
                            fb.getBukkitEntity().getEntityId(),
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
                        player.sendBlockChange(block.getLocation(), blockData)
                    }

                    if (playerList.contains(player)) {
                        val fbPacket = PacketPlayOutEntityDestroy(
                            fb.getBukkitEntity().getEntityId(),
                        )
                        entityPlayer.playerConnection.sendPacket(fbPacket)
                        val asPacket = PacketPlayOutEntityDestroy(
                            `as`.getBukkitEntity().getEntityId(),
                        )
                        entityPlayer.playerConnection.sendPacket(asPacket)
                        player.sendBlockChange(block.getLocation(), blockData)

                        playerList.remove(player)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTask() {
        this.runTaskTimerAsynchronously(Sclat.getPlugin(), 0, 5)
    }

    fun stopTask() {
        this.cancel()
        this.block.setType(originalType)
        this.block.setBlockData(blockData)
    }
}
