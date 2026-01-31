package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.plugin
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object SuperArmor {
    @JvmStatic
    fun setArmor(
        player: Player,
        armor: Double,
        delay: Long,
        effect: Boolean,
    ) {
        if (effect) {
            if (armor != 60.0) {
                getPlayerData(player)!!.isUsingSP = true
                SPWeaponMgr.setSPCoolTimeAnimation(player, delay.toInt())
                if (armor == 30.0) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 50, 0))
                }
            }
        }

        val data = getPlayerData(player)
        if (armor > data!!.armor) data.armor = armor

        // エフェクト
        val effect_r: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    if (!data.isInMatch || player.gameMode != GameMode.ADVENTURE) {
                        if (armor != 60.0 || armor != 1.0) getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon() && o_player != player) {
                            if (o_player.world === player.world) {
                                if (o_player
                                        .location
                                        .distanceSquared(player.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            data.team.teamColor!!.bukkitColor!!,
                                            1f,
                                        )
                                    o_player.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        player.eyeLocation,
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
                    if (data.armor <= 0) {
                        player.playSound(player.location, Sound.BLOCK_GLASS_BREAK, 3.5f, 1.8f)
                        player.sendMessage("§c§l！ アーマーが破壊された ！")
                        cancel()
                    }
                }
            }
        if (effect) effect_r.runTaskTimer(plugin, 0, 1)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    data.armor = 0.0
                    if (effect) {
                        effect_r.cancel()
                        getPlayerData(player)!!.isUsingSP = false
                        // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                    }
                }
            }
        task.runTaskLater(plugin, delay)
    }

    fun setRegeneArmor(
        player: Player,
        armor: Double,
        delay: Long,
        regene: Double,
        effect: Boolean,
    ) {
        if (effect) {
            getPlayerData(player)!!.isUsingSP = true
            SPWeaponMgr.setSPCoolTimeAnimation(player, delay.toInt())
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 64, 0))
        }

        val data = getPlayerData(player)
        if (armor > data!!.armor) data.armor = regene

        val rgene_r: BukkitRunnable =
            object : BukkitRunnable() {
                var c: Int = 0
                var regenearmor: Int = 1
                var canregene: Boolean = true
                var beforeArmor: Double = 0.0

                override fun run() {
                    if (!data.isInMatch || player.gameMode != GameMode.ADVENTURE) {
                        getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    if ((c >= 1 && beforeArmor > data.armor)) {
                        canregene = false
                    }
                    if (canregene) {
                        regenearmor = 1 + c / 4
                        if (data.armor + regenearmor < armor) {
                            data.armor = data.armor + regenearmor
                        } else {
                            data.armor = armor
                            canregene = false
                        }
                    }
                    beforeArmor = data.armor
                    if (data.armor <= 0) {
                        cancel()
                    }
                    c++
                }
            }
        rgene_r.runTaskTimer(plugin, 0, 2)
        // エフェクト
        val effect_r: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    if (!data.isInMatch || player.gameMode != GameMode.ADVENTURE) {
                        getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon() && o_player != player) {
                            if (o_player.world === player.world) {
                                if (o_player
                                        .location
                                        .distanceSquared(player.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            data.team.teamColor!!.bukkitColor!!,
                                            1f,
                                        )
                                    o_player.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        player.eyeLocation,
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
                    if (data.armor <= 0) {
                        player.playSound(player.location, Sound.BLOCK_GLASS_BREAK, 3.5f, 1.8f)
                        player.sendMessage("§c§l！ アーマーが破壊された ！")
                        cancel()
                    }
                }
            }
        if (effect) effect_r.runTaskTimer(plugin, 0, 1)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    data.armor = 0.0
                    if (effect) {
                        effect_r.cancel()
                        getPlayerData(player)!!.isUsingSP = false
                        // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                    }
                }
            }
        task.runTaskLater(plugin, delay)
    }
}
