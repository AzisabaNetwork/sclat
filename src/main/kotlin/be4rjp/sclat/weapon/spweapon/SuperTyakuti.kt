package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.shape.Sphere.getSphere
import net.azisaba.sclat.core.shape.Sphere.getXZCircle
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object SuperTyakuti {
    @JvmStatic
    fun superTyakutiRunnable(player: Player) {
        player.inventory.clear()
        getPlayerData(player)!!.isUsingSP = true
        getPlayerData(player)!!.isUsingTyakuti = true
        player.world.playSound(player.location, Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.5f)
        SPWeaponMgr.setSPCoolTimeAnimation(player, 40)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    try {
                        player.inventory.clear()
                        var vec = Vector(0, 0, 0)
                        when (i) {
                            1 -> {
                                vec = Vector(0, 3, 0)
                            }

                            2 -> {
                                vec = Vector(0.0, 2.5, 0.0)
                            }

                            3 -> {
                                vec = Vector(0, 2, 0)
                            }

                            4 -> {
                                vec = Vector(0, 1, 0)
                            }

                            24 -> {
                                vec = Vector(0.0, -0.5, 0.0)
                            }

                            25 -> {
                                vec = Vector(0, -1, 0)
                            }

                            26 -> {
                                vec = Vector(0, -2, 0)
                            }

                            27 -> {
                                vec = Vector(0, -4, 0)
                            }

                            else -> {}
                        }
                        if (i <= 27) player.velocity = vec

                        if (i in 5..23) {
                            for (o_player in plugin.server.onlinePlayers) {
                                if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon() &&
                                    o_player != player
                                ) {
                                    if (o_player.world === player.world) {
                                        if (o_player.location.distanceSquared(
                                                player.location,
                                            ) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            val dustOptions =
                                                Particle.DustOptions(
                                                    getPlayerData(player)!!.team!!.teamColor!!.bukkitColor!!,
                                                    1f,
                                                )
                                            o_player.spawnParticle<Particle.DustOptions?>(
                                                Particle.REDSTONE,
                                                player.eyeLocation.add(0.0, -0.5, 0.0),
                                                5,
                                                0.5,
                                                0.4,
                                                0.5,
                                                5.0,
                                                dustOptions,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (i == 2) SuperArmor.setArmor(player, 60.0, 38, false)

                        // 範囲エフェクト
                        if (i % 5 == 0) {
                            var bloc =
                                player
                                    .world
                                    .getHighestBlockAt(player.location.blockX, player.location.blockZ)
                                    .location
                            for (y in player.location.blockY downTo 1) {
                                val bl =
                                    Location(
                                        player.world,
                                        player.location.x,
                                        y.toDouble(),
                                        player.location.z,
                                    )
                                if (bl.block.type != Material.AIR) {
                                    bloc = bl
                                    break
                                }
                            }
                            val sLocs = getXZCircle(bloc.add(0.0, 1.0, 0.0), 7.0, 3.0, 40)
                            for (o_player in plugin.server.onlinePlayers) {
                                if (getPlayerData(o_player)!!.settings!!.showEffectSPWeaponRegion()) {
                                    for (loc in sLocs) {
                                        if (o_player.world === loc.world) {
                                            if (o_player
                                                    .location
                                                    .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val bd =
                                                    getPlayerData(player)!!
                                                        .team!!
                                                        .teamColor!!
                                                        .wool!!
                                                        .createBlockData()
                                                o_player.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    loc,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    1.0,
                                                    bd,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (i >= 24 && player.isOnGround) {
                            // 爆発音
                            player.world.playSound(
                                player.location,
                                Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                                1.2f,
                                0.8f,
                            )
                            player.world.playSound(
                                player.location,
                                Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED,
                                1.1f,
                                0.9f,
                            )

                            // 爆発エフェクト
                            createInkExplosionEffect(player.location, 7.0, 10, player)

                            val maxDist = 8.0
                            val maxDistSquared = 64.0 // 8^2
                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val pLocs: MutableList<Location> = getSphere(player.location, i.toDouble(), 10)
                                    for (loc in pLocs) {
                                        PaintMgr.paint(loc, player, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== player.world) continue
                                if (target.location.distanceSquared(player.location) <= maxDistSquared) {
                                    val damage = (maxDist - target.location.distance(player.location)) * 15
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "spWeapon")

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
                                if (`as`.location.distanceSquared(player.location) <= maxDistSquared) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            val damage = (
                                                (maxDist - `as`.location.distance(player.location)) *
                                                    15
                                            )
                                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        }
                                    }
                                }
                            }
                            WeaponClassMgr.setWeaponClass(player)
                            getPlayerData(player)!!.isUsingSP = false
                            getPlayerData(player)!!.isUsingTyakuti = false
                            cancel()
                        }

                        if (i == 500 ||
                            player.gameMode == GameMode.SPECTATOR ||
                            !getPlayerData(player)!!.isInMatch
                        ) {
                            if (i == 500 &&
                                player.gameMode == GameMode.ADVENTURE &&
                                getPlayerData(player)!!.isInMatch
                            ) {
                                WeaponClassMgr.setWeaponClass(player)
                            }
                            getPlayerData(player)!!.isUsingSP = false
                            getPlayerData(player)!!.isUsingTyakuti = false
                            cancel()
                        }
                        i++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
