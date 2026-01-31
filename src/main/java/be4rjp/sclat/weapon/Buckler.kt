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
    fun BucklerRunnable(player: Player) {
        val delay3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var Etime: Int = 80
                var Ctime: Int = 120
                var bk_recharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }
                    if (data.getIsSneaking() && bk_recharge && player.getGameMode() == GameMode.ADVENTURE &&
                        (
                            p.getInventory().getItemInMainHand().getType()
                                ==
                                data
                                    .getWeaponClass()
                                    .mainWeapon!!
                                    .weaponIteamStack!!
                                    .getType()
                            )
                    ) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Etime, 0))
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, Ctime, 0))
                        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f)
                        bk_recharge = false
                        val healtask: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    bk_recharge = true
                                }
                            }
                        healtask.runTaskLater(plugin, Ctime.toLong())
                    }
                }
            }
        delay3.runTaskTimer(plugin, 0, 1)
    }
}
