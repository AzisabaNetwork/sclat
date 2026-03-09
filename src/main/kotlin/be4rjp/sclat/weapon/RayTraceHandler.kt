package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.plugin
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object RayTraceHandler {
    /**
     * Performs a funnel shot ray trace from [funnel] towards [targetLoc], applying damage to
     * the first hit player or armor stand.
     */
    fun performShot(
        player: Player,
        funnel: ArmorStand,
        targetLoc: Location,
    ) {
        val damage = 3.0
        val funloc = funnel.eyeLocation
        val direction =
            Vector(
                targetLoc.x - funloc.x,
                targetLoc.y - funloc.y,
                targetLoc.z - funloc.z,
            ).normalize()
        val rayTrace = RayTrace(funloc.toVector(), direction)
        val positions = rayTrace.traverse(4.0, 0.2)
        val maxDistSquared = 4.0

        loop@ for (vector in positions) {
            val position = vector.toLocation(player.location.world!!)
            if (player.location.world!!
                    .getBlockAt(position)
                    .type != Material.AIR
            ) {
                break
            }

            spawnInkParticles(player, position)

            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquared) {
                        if (rayTrace.intersects(BoundingBox(target as Entity), 4.0, 0.05)) {
                            giveDamage(player, target, damage, "killed")
                            player.playSound(player.location, Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)
                            object : BukkitRunnable() {
                                override fun run() {
                                    target.noDamageTicks = 0
                                }
                            }.runTaskLater(plugin, 1)
                            break@loop
                        }
                    }
                }
            }

            for (entity in player.world.entities) {
                if (entity !is ArmorStand) continue
                if (entity.location.distanceSquared(position) > maxDistSquared) continue
                if (!rayTrace.intersects(BoundingBox(entity as Entity), 4.0, 0.05)) continue
                if (applyDamageToArmorStand(entity, player, damage)) break@loop
            }
        }
    }

    private fun spawnInkParticles(
        player: Player,
        position: Location,
    ) {
        for (target in plugin.server.onlinePlayers) {
            if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
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
        }
    }

    /**
     * Applies damage to an armor stand hit during a funnel shot.
     * @return true if the outer ray traversal loop should break
     */
    private fun applyDamageToArmorStand(
        armorStand: ArmorStand,
        player: Player,
        damage: Double,
    ): Boolean {
        val name = armorStand.customName
        if (name == null) {
            ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
            return false
        }
        return when (name) {
            "SplashShield" -> {
                val ssdata = getSplashShieldDataFromArmorStand(armorStand) ?: return false
                if (getPlayerData(ssdata.player)!!.team != getPlayerData(player)!!.team) {
                    ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
                    armorStand.world.playSound(armorStand.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                    true
                } else {
                    false
                }
            }
            "Kasa" -> {
                val ssdata = getKasaDataFromArmorStand(armorStand) ?: return false
                if (getPlayerData(ssdata.player)!!.team != getPlayerData(player)!!.team) {
                    ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
                    armorStand.world.playSound(armorStand.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                    true
                } else {
                    false
                }
            }
            else -> {
                if (SclatUtil.isNumber(name) && name != "21" && name != "100" && armorStand.isVisible) {
                    player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.2f, 1.3f)
                }
                ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
                true
            }
        }
    }
}
