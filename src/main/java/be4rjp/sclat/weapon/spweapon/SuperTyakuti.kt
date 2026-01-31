package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.Sphere.getXZCircle
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
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
    fun SuperTyakutiRunnable(player: Player) {
        player.getInventory().clear()
        getPlayerData(player)!!.setIsUsingSP(true)
        getPlayerData(player)!!.setIsUsingTyakuti(true)
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.5f)
        SPWeaponMgr.setSPCoolTimeAnimation(player, 40)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    try {
                        player.getInventory().clear()
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
                        if (i <= 27) player.setVelocity(vec)

                        if (i >= 5 && i <= 23) {
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon() &&
                                    o_player != player
                                ) {
                                    if (o_player.getWorld() === player.getWorld()) {
                                        if (o_player.getLocation().distanceSquared(
                                                player.getLocation(),
                                            ) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            val dustOptions =
                                                Particle.DustOptions(
                                                    getPlayerData(player)!!.team.teamColor!!.bukkitColor!!,
                                                    1f,
                                                )
                                            o_player.spawnParticle<Particle.DustOptions?>(
                                                Particle.REDSTONE,
                                                player.getEyeLocation().add(0.0, -0.5, 0.0),
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
                                    .getWorld()
                                    .getHighestBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockZ())
                                    .getLocation()
                            for (y in player.getLocation().getBlockY() downTo 1) {
                                val bl =
                                    Location(
                                        player.getWorld(),
                                        player.getLocation().getX(),
                                        y.toDouble(),
                                        player.getLocation().getZ(),
                                    )
                                if (bl.getBlock().getType() != Material.AIR) {
                                    bloc = bl
                                    break
                                }
                            }
                            val s_locs = getXZCircle(bloc.add(0.0, 1.0, 0.0), 7.0, 3.0, 40)
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeaponRegion()) {
                                    for (loc in s_locs) {
                                        if (o_player.getWorld() === loc.getWorld()) {
                                            if (o_player
                                                    .getLocation()
                                                    .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val bd =
                                                    getPlayerData(player)!!
                                                        .team
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

                        if (i >= 24 && player.isOnGround()) {
                            // 爆発音
                            player.getWorld().playSound(
                                player.getLocation(),
                                Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                                1.2f,
                                0.8f,
                            )
                            player.getWorld().playSound(
                                player.getLocation(),
                                Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED,
                                1.1f,
                                0.9f,
                            )

                            // 爆発エフェクト
                            createInkExplosionEffect(player.getLocation(), 7.0, 10, player)

                            val maxDist = 8.0
                            val maxDistSquared = 64.0 // 8^2
                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val p_locs: MutableList<Location> = getSphere(player.getLocation(), i.toDouble(), 10)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, player, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== player.getWorld()) continue
                                if (target.getLocation().distanceSquared(player.getLocation()) <= maxDistSquared) {
                                    val damage = (maxDist - target.getLocation().distance(player.getLocation())) * 15
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.getGameMode() == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "spWeapon")

                                        // AntiNoDamageTime
                                        val task: BukkitRunnable =
                                            object : BukkitRunnable() {
                                                var p: Player = target

                                                override fun run() {
                                                    target.setNoDamageTicks(0)
                                                }
                                            }
                                        task.runTaskLater(plugin, 1)
                                    }
                                }
                            }

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distanceSquared(player.getLocation()) <= maxDistSquared) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.getCustomName() != null) {
                                            val damage = (
                                                (maxDist - `as`.getLocation().distance(player.getLocation())) *
                                                    15
                                                )
                                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        }
                                    }
                                }
                            }
                            WeaponClassMgr.setWeaponClass(player)
                            getPlayerData(player)!!.setIsUsingSP(false)
                            getPlayerData(player)!!.setIsUsingTyakuti(false)
                            cancel()
                        }

                        if (i == 500 || player.getGameMode() == GameMode.SPECTATOR ||
                            !getPlayerData(player)!!.isInMatch()
                        ) {
                            if (i == 500 && player.getGameMode() == GameMode.ADVENTURE &&
                                getPlayerData(player)!!.isInMatch()
                            ) {
                                WeaponClassMgr.setWeaponClass(player)
                            }
                            getPlayerData(player)!!.setIsUsingSP(false)
                            getPlayerData(player)!!.setIsUsingTyakuti(false)
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
