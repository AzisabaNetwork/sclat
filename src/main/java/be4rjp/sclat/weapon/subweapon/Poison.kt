package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Poison {
    @JvmStatic
    fun PoisonRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            p_vec = p.getEyeLocation().getDirection()
                            if (!getPlayerData(player)!!.getIsBombRush()) p.setExp(p.getExp() - 0.39f)
                            val bom = ItemStack(Material.PRISMARINE).clone()
                            val bom_m = bom.getItemMeta()
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.setItemMeta(bom_m)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), bom)
                            drop!!.setVelocity(p_vec!!)
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball>(Snowball::class.java)
                            ball!!.setVelocity(Vector(0, 0, 0))
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                val connection = (o_player as CraftPlayer).getHandle().playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                            }
                            p_vec = p.getEyeLocation().getDirection()
                        }

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
                            // 半径

                            val maxDist = 5 * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)

                            // 爆発音
                            player.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 2f)

                            // 爆発エフェクト
                            val s_locs = getSphere(drop!!.getLocation(), maxDist, 15)
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_BombEx()) {
                                    for (loc in s_locs) {
                                        if (o_player.getWorld() === loc.getWorld()) {
                                            if (o_player
                                                    .getLocation()
                                                    .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions = Particle.DustOptions(Color.BLACK, 1f)
                                                o_player.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    loc,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    1.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // あたり判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== p.getWorld()) continue
                                if (target.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (getPlayerData(player)!!.team.iD !=
                                        getPlayerData(target)!!
                                            .team
                                            .iD
                                    ) {
                                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 80, 2))
                                        getPlayerData(target)!!.setPoison(true)
                                        PoisonRunnable2(target)
                                    }
                                }
                            }

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (`as`.getCustomName() != null) {
                                        if (`as`.getCustomName() == null) continue
                                        if (`as` is ArmorStand && (`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                            (`as`.getCustomName() != "100") &&
                                            (`as`.getCustomName() != "SplashShield") &&
                                            (`as`.getCustomName() != "Kasa")
                                        ) {
                                            `as`
                                                .addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 80, 1))
                                        }
                                    }
                                }
                            }

                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ボムの視認用エフェクト
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
                        drop!!.remove()
                        cancel()
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

        if (player.getExp() > 0.4 || getPlayerData(player)!!.getIsBombRush()) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }

    fun PoisonRunnable2(player: Player?) {
        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.setPoison(false)
                }
            }
        cooltime.runTaskLater(plugin, 80)
    }
}
