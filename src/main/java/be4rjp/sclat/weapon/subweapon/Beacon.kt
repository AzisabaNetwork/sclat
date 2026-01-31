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
        if (player.isOnGround() && getPlayerData(player)!!.isInMatch() &&
            player.getExp() >= 0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
        ) {
            val `as` = getBeaconFromplayer(player)
            `as`!!.setVisible(false)
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (`as`.getWorld() !== target.getWorld()) continue
                (target as CraftPlayer).getHandle().playerConnection.sendPacket(
                    PacketPlayOutEntityEquipment(
                        `as`.getEntityId(),
                        EnumItemSlot.HEAD,
                        CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                    ),
                )
            }
            `as`.teleport(player.getLocation().add(0.0, -0.45, 0.0))
            `as`.setCustomName("21")
            player.setExp(player.getExp() - (0.39 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat())
            val delay: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        `as`.setVisible(true)
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (`as`.getWorld() !== target.getWorld()) continue
                            (target as CraftPlayer)
                                .getHandle()
                                .playerConnection
                                .sendPacket(
                                    PacketPlayOutEntityEquipment(
                                        `as`.getEntityId(),
                                        EnumItemSlot.HEAD,
                                        CraftItemStack.asNMSCopy(ItemStack(Material.IRON_TRAPDOOR)),
                                    ),
                                )
                        }
                        `as`.getWorld().playSound(`as`.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                        // as.setHelmet(new ItemStack(Material.IRON_TRAPDOOR));
                    }
                }
            delay.runTaskLater(plugin, 10)
        } else if (player.getExp() < (0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.setCanUseSubWeapon(true)
                }
            }
        delay.runTaskLater(plugin, 20)
    }
}
