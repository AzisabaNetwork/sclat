package be4rjp.sclat.weapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

object Buckler {
    @JvmStatic
    fun bucklerRunnable(player: Player) {
        val delay3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var eTime: Int = 80
                var cTime: Int = 120
                var bkRecharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }
                    if (data.isSneaking &&
                        bkRecharge &&
                        player.gameMode == GameMode.ADVENTURE &&
                        (
                            p.inventory.itemInMainHand.type
                                ==
                                data
                                    .weaponClass
                                    ?.mainWeapon!!
                                    .weaponItemStack!!
                                    .type
                        )
                    ) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, eTime, 0))
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, cTime, 0))
                        p.world.playSound(p.location, Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f)
                        bkRecharge = false
                        val healtask: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    bkRecharge = true
                                }
                            }
                        healtask.runTaskLater(plugin, cTime.toLong())
                    }
                }
            }
        delay3.runTaskTimer(plugin, 0, 1)
    }
}
