package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EntityArmorStand
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object Barrier {
    @JvmStatic
    fun barrierRunnable(player: Player) {
        getPlayerData(player)!!.isUsingSP = true
        val data = getPlayerData(player)
        // data.setArmor(Double.MAX_VALUE);
        SPWeaponMgr.setSPCoolTimeAnimation(player, 100)

        // エフェクトとアーマー解除
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var list: MutableList<EntityArmorStand?> = ArrayList()
                var c: Int = 0

                override fun run() {
                    if (!data!!.isInMatch || (player.gameMode != GameMode.ADVENTURE) || !p.isOnline) {
                        data.armor = 0.0
                        getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    if (c == 0) data.armor = Double.MAX_VALUE
                    val loc = p.location.add(0.0, 0.5, 0.0)

                    val sLocs = getSphere(loc, 2.0, 23)
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon() && o_player != player) {
                            val dustOptions =
                                Particle.DustOptions(
                                    data.team!!.teamColor!!.bukkitColor!!,
                                    1f,
                                )
                            getPlayerData(p)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                            for (e_loc in sLocs) {
                                if (o_player.world === e_loc.world) {
                                    if (o_player
                                            .location
                                            .distanceSquared(e_loc) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            e_loc,
                                            0,
                                            0.0,
                                            0.0,
                                            0.0,
                                            70.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (c == 25) {
                        data.armor = 0.0
                        // p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                        getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 4)
    }
}
