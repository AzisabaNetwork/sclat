package net.azisaba.sclat.core.data

import net.azisaba.sclat.core.team.SclatTeam
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class Path(
    private val plugin: JavaPlugin,
    val fromLocation: Location?,
    val toLocation: Location?,
) {
    private var team: SclatTeam? = null
    var armorStand: ArmorStand? = null
    private var setTeamed = false

    fun getTeam(): SclatTeam? = this.team

    fun setTeam(t: SclatTeam?) {
        this.team = t
        for (target in plugin.server.onlinePlayers) {
            if (armorStand!!.world !== target.world) continue
            if (t == null) {
                (target as CraftPlayer)
                    .handle
                    .playerConnection
                    .sendPacket(
                        PacketPlayOutEntityEquipment(
                            armorStand!!.entityId,
                            EnumItemSlot.HEAD,
                            CraftItemStack.asNMSCopy(ItemStack(Material.WHITE_STAINED_GLASS)),
                        ),
                    )
            } else {
                (target as CraftPlayer)
                    .handle
                    .playerConnection
                    .sendPacket(
                        PacketPlayOutEntityEquipment(
                            armorStand!!.entityId,
                            EnumItemSlot.HEAD,
                            CraftItemStack.asNMSCopy(ItemStack(team!!.teamColor!!.glass!!)),
                        ),
                    )
            }
        }
        if (t != null) {
            armorStand!!
                .world
                .playSound(armorStand!!.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        } else {
            return
        }

        if (!setTeamed) {
            setTeamed = true
            val task: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        team = null
                        setTeamed = false
                    }
                }
            task.runTaskLater(plugin, 3600)
        }
    }

    fun stop() {
        armorStand?.remove()
    }

    fun reset() {
        this.team = null
        this.armorStand = null
    }
}
