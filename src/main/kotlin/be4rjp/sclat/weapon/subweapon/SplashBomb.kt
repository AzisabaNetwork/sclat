package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object SplashBomb {
    @JvmStatic
    fun SplashBomRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0
                var gc: Int = 0
                var drop: Item? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            if (!getPlayerData(player)!!.isBombRush) p.exp = p.exp - 0.59f
                            val bom = ItemStack(getPlayerData(p)!!.team!!.teamColor!!.glass!!).clone()
                            val bom_m = bom.itemMeta
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bom_m
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = p.eyeLocation.direction
                        }

                        if (gc >= 10 && gc < 20) {
                            if (gc % 2 == 0) {
                                player
                                    .world
                                    .playSound(drop!!.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                            }
                        }

                        if (gc == 30) {
                            // 爆発音
                            player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.location, 5.0, 15, player)

                            val maxDist = 4.0

                            // バリアをはじく
                            repelBarrier(drop!!.location, maxDist, player)

                            // 塗る
                            var i = 0
                            while (i <= maxDist) {
                                val p_locs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 14)
                                for (loc in p_locs) {
                                    PaintMgr.paint(loc, p, false)
                                }
                                i++
                            }

                            // 攻撃判定の処理
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distance(drop!!.location) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.location.distance(drop!!.location)) * 14 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "subWeapon")

                                        // AntiNoDamageTime
                                        val task: BukkitRunnable =
                                            object : BukkitRunnable() {
                                                var p: Player = target

                                                override fun run() {
                                                    target.noDamageTicks = 0
                                                }
                                            }
                                        task.runTaskLater(plugin, 1)
                                    }
                                }
                            }

                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(drop!!.location) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            val damage = (maxDist - `as`.location.distance(drop!!.location)) * 12
                                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                        }
                                    }
                                }
                            }
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ボムの視認用エフェクト
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.ShowEffect_Bomb()) {
                                if (o_player.world === drop!!.location.world) {
                                    if (o_player
                                            .location
                                            .distanceSquared(drop!!.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            drop!!.location,
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

                        if (c > 500) {
                            drop!!.remove()
                            cancel()
                            return
                        }

                        if (drop!!.isOnGround) gc++
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
                        plugin.logger.warning(e.message)
                    }
                }
            }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 10)

        if (player.exp > 0.6 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
