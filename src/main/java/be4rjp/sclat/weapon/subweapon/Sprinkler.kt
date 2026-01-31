package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.getSprinklerFromplayer
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.data.DataMgr.snowballNameMap
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Sprinkler {
    @JvmStatic
    fun SprinklerRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var cb: Boolean = false
                var l: Location = p.getLocation()
                var cc: Int = 0
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null
                var ndn: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            if (!getPlayerData(player)!!.getIsBombRush()) {
                                p.setExp(
                                    p.getExp() - (0.59 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat(),
                                )
                            }
                            val bom = ItemStack(Material.BIRCH_FENCE_GATE).clone()
                            val bom_m = bom.getItemMeta()
                            ndn = notDuplicateNumber
                            bom_m!!.setLocalizedName(ndn.toString())
                            bom.setItemMeta(bom_m)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), bom)
                            drop!!.setVelocity(p.getEyeLocation().getDirection())
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball?>(Snowball::class.java)
                            ball!!.setVelocity(Vector(0, 0, 0))
                            setSnowballIsHit(ball, false)
                            ball!!.setCustomName(ndn.toString())
                            snowballNameMap.put(ndn.toString(), ball)
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                val connection = (o_player as CraftPlayer).getHandle().playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                            }
                            p_vec = p.getEyeLocation().getDirection()
                        }

                        ball = snowballNameMap.get(ndn.toString())

                        if (!drop!!.isOnGround() &&
                            !(
                                drop!!.getVelocity().getX() == 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() != 0.0
                                ) &&
                            !(
                                drop!!.getVelocity().getX() != 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() == 0.0
                                )
                        ) {
                            ball!!.setVelocity(drop!!.getVelocity())
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround()) {
                            val `as` = getSprinklerFromplayer(player)
                            `as`!!.setVisible(false)
                            `as`.setHelmet(ItemStack(Material.AIR))
                            `as`.teleport(drop!!.getLocation().add(0.0, -0.4, 0.0))
                            `as`.setCustomName("21")
                            Sprinkler.SprinklerRunnable2(`as`, p)
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // 視認用エフェクト
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (o_player.getWorld() === drop!!.getLocation().getWorld()) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            drop!!.getLocation(),
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            50.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }

                        c++
                        x = drop!!.getLocation().getX()
                        z = drop!!.getLocation().getZ()

                        if (c > 1000) {
                            drop!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        cancel()
                        drop!!.remove()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.setCanUseSubWeapon(true)
                }
            }
        cooltime.runTaskLater(plugin, 8)

        if (player.getExp() > (0.6 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()) {
            task.runTaskTimer(
                plugin,
                0,
                1,
            )
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }

    fun SprinklerRunnable2(
        `as`: ArmorStand,
        player: Player?,
    ) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (target in plugin.getServer().getOnlinePlayers()) {
                        if (`as`.getWorld() !== target.getWorld()) continue
                        (target as CraftPlayer).getHandle().playerConnection.sendPacket(
                            PacketPlayOutEntityEquipment(
                                `as`.getEntityId(),
                                EnumItemSlot.HEAD,
                                CraftItemStack.asNMSCopy(
                                    ItemStack(getPlayerData(player)!!.team.teamColor!!.glass!!),
                                ),
                            ),
                        )
                    }
                    `as`.getWorld().playSound(`as`.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                    // as.setHelmet(new
                    // ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().glass));
                }
            }
        delay.runTaskLater(plugin, 10)
    }
}
