package be4rjp.sclat.weapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object Swapper {
    @JvmStatic
    fun SwapperRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var sw_recharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }
                    // スワッパ―系
                    if (data.getWeaponClass().mainWeapon!!.getIsSwap()) {
                        if (data.getIsSneaking() && sw_recharge && (
                                p.getInventory().getItemInMainHand().getType()
                                    ==
                                    data
                                        .getWeaponClass()
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .getType()
                                )
                        ) {
                            data.stoprun = true
                            player.getInventory().clear()
                            p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)
                            sw_recharge = false
                            val swapset: BukkitRunnable =
                                object : BukkitRunnable() {
                                    // チャージャーとローラーのみ対応
                                    override fun run() {
                                        val swapname = data.getWeaponClass().mainWeapon!!.swap
                                        data.stoprun = false
                                        data.setWeaponClass(getWeaponClass(swapname))
                                        data.setCanRollerShoot(true)
                                        getPlayerData(p)!!.setIsUsingManeuver(false)
                                        if (getPlayerData(p)!!.getWeaponClass().mainWeapon!!.weaponType
                                            == "Shooter"
                                        ) {
                                            if (getPlayerData(p)!!
                                                    .getWeaponClass()
                                                    .mainWeapon!!
                                                    .slidingShootTick > 1
                                            ) {
                                                getPlayerData(p)!!.setIsUsingManeuver(true)
                                            } else {
                                                Shooter.ShooterRunnable(p)
                                            }
                                        }
                                        WeaponClassMgr.setWeaponClass(p)
                                    }
                                }
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    // クールタイムを管理しています
                                    override fun run() {
                                        sw_recharge = true
                                    }
                                }
                            swapset.runTaskLater(plugin, 5)
                            task.runTaskLater(plugin, 30)
                        }
                    }
                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }
}
