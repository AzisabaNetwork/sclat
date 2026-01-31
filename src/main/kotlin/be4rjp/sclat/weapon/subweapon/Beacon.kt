package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.data.DataMgr.getBeaconFromplayer
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object Beacon {
    @JvmStatic
    fun setBeacon(player: Player) {
        if (player.isOnGround && getPlayerData(player)!!.isInMatch &&
            player.exp >= 0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
        ) {
            val `as` = getBeaconFromplayer(player)
            `as`!!.isVisible = false
            for (target in plugin.server.onlinePlayers) {
                if (`as`.world !== target.world) continue
                (target as CraftPlayer).handle.playerConnection.sendPacket(
                    PacketPlayOutEntityEquipment(
                        `as`.entityId,
                        EnumItemSlot.HEAD,
                        CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                    ),
                )
            }
            `as`.teleport(player.location.add(0.0, -0.45, 0.0))
            `as`.customName = "21"
            player.exp = player.exp - (0.39 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()
            val delay: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        `as`.isVisible = true
                        for (target in plugin.server.onlinePlayers) {
                            if (`as`.world !== target.world) continue
                            (target as CraftPlayer)
                                .handle
                                .playerConnection
                                .sendPacket(
                                    PacketPlayOutEntityEquipment(
                                        `as`.entityId,
                                        EnumItemSlot.HEAD,
                                        CraftItemStack.asNMSCopy(ItemStack(Material.IRON_TRAPDOOR)),
                                    ),
                                )
                        }
                        `as`.world.playSound(`as`.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                        // as.setHelmet(new ItemStack(Material.IRON_TRAPDOOR));
                    }
                }
            delay.runTaskLater(plugin, 10)
        } else if (player.exp < (0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.canUseSubWeapon = true
                }
            }
        delay.runTaskLater(plugin, 20)
    }
}
