package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.min

object Funnel {
    var funnelMaxHP: Int = 10
    var funnelMaxHP2: Int = 3
    var funnelSpeed: Double = 1.0

    /**
     * Fires a ray from [funnel] towards [targetLoc] and applies damage to the first target hit.
     * Delegates all ray tracing logic to [RayTraceHandler].
     *
     * @param player The player who fired the funnel shot.
     * @param funnel The armor stand representing the funnel being fired.
     * @param targetLoc The location the funnel is being fired towards.
     */
    fun funnelShot(
        player: Player,
        funnel: ArmorStand,
        targetLoc: Location,
    ) {
        if (player.gameMode == org.bukkit.GameMode.SPECTATOR) return
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        RayTraceHandler.performShot(player, funnel, targetLoc)
    }

    /**
     * Spawns the three funnel groups for [player] and starts the floating/targeting task.
     * Delegates lifecycle management to [FunnelTask].
     */
    @JvmStatic
    fun funnelFloat(player: Player) {
        FunnelTask(player).start()
    }

    fun funnelPursuit(
        player: Player,
        target: ArmorStand,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (ArmorStandManager.hashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (ArmorStandManager.hashArmorstand[getPlayerData(player)!!.getArmorlist(ai)] == target) {
                        rate += 1.5
                    }
                }
            } catch (_: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val loct: Location = target.location

                override fun run() {
                    val playerData = getPlayerData(player)!!
                    for (ai in 0..2) {
                        try {
                            val locd = playerData.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd.x,
                                    loct.y - locd.y + 1.5,
                                    loct.z - locd.z,
                                )
                            val rayTrace = RayTrace(locd.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length(), 0.4)
                            val veclength = min(vec.length() / 2, 12.0)
                            for (i in 0 until veclength.toInt()) {
                                val position = positions[i].toLocation(player.location.world!!)
                                spawnPursuitParticle(player, playerData, position)
                            }
                        } catch (e: Exception) {
                            sclatLogger.error("Failed to perform funnel pursuit ray trace for player", e)
                        }
                    }
                }
            }
        task.runTaskLater(plugin, 1)
        return rate
    }

    fun funnelPursuitPlayer(
        player: Player,
        target: Player,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (ArmorStandManager.hashPlayer[getPlayerData(player)!!.getArmorlist(ai)] == target) {
                    rate += 1.5
                }
            } catch (_: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val loct: Location = target.location

                override fun run() {
                    val data: PlayerData =
                        getPlayerData(player) ?: run {
                            sclatLogger.warn("Failed to get player data for funnel pursuit particle spawning task, aborting task")
                            return
                        }

                    for (ai in 0..2) {
                        try {
                            val locd = data.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd.x,
                                    loct.y - locd.y + 1.5,
                                    loct.z - locd.z,
                                )
                            val rayTrace = RayTrace(locd.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length().toInt().toDouble(), 0.4)
                            val veclength = min(vec.length() / 2, 12.0)
                            for (i in 0 until veclength.toInt()) {
                                val position = positions[i].toLocation(player.location.world!!)
                                spawnPursuitParticle(player, data, position)
                            }
                        } catch (e: Exception) {
                            sclatLogger.error("Failed to perform funnel pursuit ray trace for player", e)
                        }
                    }
                }
            }
        task.runTaskLater(plugin, 1)
        return rate
    }

    fun funnelamount(player: Player?): Int {
        var rate = 3
        for (ai in 0..2) {
            try {
                val armorList = getPlayerData(player)!!.getArmorlist(ai)
                if (ArmorStandManager.hashPlayer.containsKey(armorList)) {
                    rate -= 1
                }
                if (ArmorStandManager.hashArmorstand.containsKey(armorList)) {
                    rate -= 1
                }
            } catch (_: Exception) {
                rate -= 1
            }
        }
        return rate
    }

    internal fun funAmoP(player: Player?): Boolean {
        var count = 0
        for (entry in ArmorStandManager.hashPlayer.entries) {
            if (entry.value === player) {
                count++
            }
        }
        return count < 3
    }

    internal fun funAmoA(stand: ArmorStand?): Boolean = ArmorStandManager.hashArmorstand.count { it.value === stand } < 3

    /** Spawns a pursuit-line particle at [position] for the player and nearby observers. */
    private fun spawnPursuitParticle(
        player: Player,
        data: PlayerData,
        position: Location,
    ) {
        val dustOptions = Particle.DustOptions(data.team!!.teamColor!!.bukkitColor!!, 1f)
        if (player.world === position.world &&
            player.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared
        ) {
            player.spawnParticle<Particle.DustOptions?>(
                Particle.REDSTONE,
                position,
                1,
                0.0,
                0.0,
                0.0,
                3.0,
                dustOptions,
            )
        }
        for (target in plugin.server.onlinePlayers) {
            if (target == player || getPlayerData(target)!!.settings!!.showEffectChargerLine()) {
                if (target.world === player.world &&
                    target.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared
                ) {
                    target.spawnParticle<Particle.DustOptions?>(
                        Particle.REDSTONE,
                        position,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        3.0,
                        dustOptions,
                    )
                }
            }
        }
    }
}
