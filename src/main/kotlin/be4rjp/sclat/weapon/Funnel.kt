package be4rjp.sclat.weapon

import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object Funnel {
    var funnelMaxHP: Int = 10
    var funnelMaxHP2: Int = 3
    var funnelSpeed: Double = 1.0

    /**
     * Fires a ray from [funnel] towards [targetLoc] and applies damage to the first target hit.
     * Delegates all ray tracing logic to [RayTraceHandler].
     */
    fun funnelShot(
        player: Player,
        funnel: ArmorStand,
        taegetloc: Location,
    ) {
        if (player.gameMode == org.bukkit.GameMode.SPECTATOR) return
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        RayTraceHandler.performShot(player, funnel, taegetloc)
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
            } catch (e: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.location
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd!!.x,
                                    loct.y - locd!!.y + 1.5,
                                    loct.z - locd!!.z,
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions[i].toLocation(p.location.world!!)
                                spawnPursuitParticle(player, data!!, position)
                                i++
                            }
                        } catch (e: Exception) {
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
                if (ArmorStandManager.hashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (ArmorStandManager.hashPlayer[getPlayerData(player)!!.getArmorlist(ai)] == target) {
                        rate += 1.5
                    }
                }
            } catch (e: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.location
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd!!.x,
                                    loct.y - locd!!.y + 1.5,
                                    loct.z - locd!!.z,
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length().toInt().toDouble(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions[i].toLocation(p.location.world!!)
                                spawnPursuitParticle(player, data!!, position)
                                i++
                            }
                        } catch (e: Exception) {
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
                if (ArmorStandManager.hashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate -= 1
                }
                if (ArmorStandManager.hashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate -= 1
                }
            } catch (e: Exception) {
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

    internal fun funAmoA(stand: ArmorStand?): Boolean {
        var count = 0
        for (entry in ArmorStandManager.hashArmorstand.entries) {
            if (entry.value === stand) {
                count++
            }
        }
        return count < 3
    }

    /** Spawns a pursuit-line particle at [position] for the player and nearby observers. */
    private fun spawnPursuitParticle(
        player: Player,
        data: PlayerData,
        position: Location,
    ) {
        val dustOptions = Particle.DustOptions(data.team!!.teamColor!!.bukkitColor!!, 1f)
        if (player.world === position.world &&
            player.location.distanceSquared(position) < be4rjp.sclat.Sclat.particleRenderDistanceSquared
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
                    target.location.distanceSquared(position) < be4rjp.sclat.Sclat.particleRenderDistanceSquared
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
