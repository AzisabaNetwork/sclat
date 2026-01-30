package be4rjp.sclat.data

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.team.Team
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class Path(
    val fromLocation: Location?,
    val toLocation: Location?,
) {
    private var team: Team? = null
    var armorStand: ArmorStand? = null
    private var setTeamed = false

    fun getTeam(): Team? = this.team

    fun setTeam(t: Team?) {
        this.team = t
        for (target in Sclat.getPlugin().server.onlinePlayers) {
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
                            armorStand!!.getEntityId(),
                            EnumItemSlot.HEAD,
                            CraftItemStack.asNMSCopy(ItemStack(team!!.teamColor.glass)),
                        ),
                    )
            }
        }
        if (t != null) {
            armorStand!!
                .getWorld()
                .playSound(armorStand!!.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
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
            task.runTaskLater(Sclat.getPlugin(), 3600)
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
