package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object Blinder {
    fun blinderRunnable(player: Player) {
        val p = player
        val reach = 35

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 8)
        if (p.exp > 0.36f || getPlayerData(player)!!.isBombRush) {
            if (!getPlayerData(player)!!.isBombRush) {
                p.exp = player.exp - 0.35f
            }
            shootblind(p, reach)
        } else {
            p.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            p.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }

    fun shootblind(
        player: Player,
        reach: Int,
    ) {
        if (player.gameMode == GameMode.SPECTATOR) return
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions: ArrayList<Vector> = rayTrace.traverse(reach.toDouble(), 0.15)
        loop@ for (i in positions.indices) {
            val position = positions[i].toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                break
            }
            // if(i<8) {
            // PaintMgr.PaintHightestBlock(position, player, false, true);
            // }
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) continue
                if (target.world === position.world) {
                    if (target.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                        val bd =
                            getPlayerData(player)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                        target.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                    }
                }
            }

            val maxDistSquad = 4.0 // 2*2
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(target as Entity), reach.toDouble(), 0.2)) {
                            val weaponType =
                                getPlayerData(target)!!
                                    .weaponClass!!
                                    .mainWeapon!!
                                    .weaponType
                            var effecttime = 40
                            if (weaponType == "Charger" ||
                                weaponType == "Spinner" ||
                                getPlayerData(target)!!.weaponClass!!.mainWeapon!!.isManeuver
                            ) {
                                effecttime += 25
                            } else if (weaponType == "Blaster" || weaponType == "Hound") {
                                effecttime += 10
                            } else if (weaponType == "Roller" ||
                                weaponType == "Slosher" ||
                                weaponType == "Bucket"
                            ) {
                                effecttime -= 10
                            }
                            if (getPlayerData(target)!!.isUsingSP ||
                                getPlayerData(target)!!.armor > 0
                            ) {
                                target.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 1))
                                player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, 1.2f, 1.3f)
                            } else if (i > 85 && !target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                                target.addPotionEffect(
                                    PotionEffect(
                                        PotionEffectType.BLINDNESS,
                                        (effecttime * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toInt(),
                                        1,
                                    ),
                                )
                                target.addPotionEffect(
                                    PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70 + effecttime, 1),
                                )
                                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_HURT, 1.2f, 1.3f)
                            } else if (target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                                player.playSound(player.location, Sound.ENTITY_BLAZE_HURT, 1.2f, 1.3f)
                            } else {
                                getPlayerData(target)!!.poison = true
                                poisonRunnable3(
                                    target,
                                    (effecttime * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP) - 10).toInt(),
                                )
                                target.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1))
                                player.playSound(player.location, Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)
                            }
                            break@loop
                        }
                    }
                }
            }

            for (`as` in player.world.entities) {
                if (`as` is ArmorStand) {
                    if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(`as` as Entity), (reach).toDouble(), 0.2)) {
                            if (`as`.customName != null) {
                                if (`as`.customName == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        `as`
                                            .world
                                            .playSound(`as`.location, Sound.ENTITY_BLAZE_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.customName == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        `as`
                                            .world
                                            .playSound(`as`.location, Sound.ENTITY_BLAZE_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.customName!!)) {
                                        if (`as`.customName != "21" &&
                                            `as`.customName != "100"
                                        ) {
                                            if (`as`.isVisible) {
                                                if (i > 85) {
                                                    player.playSound(
                                                        player.location,
                                                        Sound.ENTITY_ENDER_DRAGON_HURT,
                                                        1.2f,
                                                        1.3f,
                                                    )
                                                } else {
                                                    player.playSound(
                                                        player.location,
                                                        Sound.ENTITY_PLAYER_HURT,
                                                        1.2f,
                                                        1.3f,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    break@loop
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun poisonRunnable3(
        player: Player?,
        delay: Int,
    ) {
        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.poison = false
                }
            }
        cooltime.runTaskLater(plugin, delay.toLong())
    }
}
