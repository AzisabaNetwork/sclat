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

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }
                    // スワッパ―系
                    if (data.weaponClass?.mainWeapon!!.getIsSwap()) {
                        if (data.isSneaking &&
                            sw_recharge &&
                            (
                                p.inventory.itemInMainHand.type
                                    ==
                                    data
                                        .weaponClass
                                        ?.mainWeapon!!
                                        .weaponIteamStack!!
                                        .type
                            )
                        ) {
                            data.stoprun = true
                            player.inventory.clear()
                            p.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)
                            sw_recharge = false
                            val swapset: BukkitRunnable =
                                object : BukkitRunnable() {
                                    // チャージャーとローラーのみ対応
                                    override fun run() {
                                        val swapname = data.weaponClass?.mainWeapon!!.swap
                                        data.stoprun = false
                                        data.weaponClass = getWeaponClass(swapname)
                                        data.canRollerShoot = true
                                        getPlayerData(p)!!.isUsingManeuver = false
                                        if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType
                                            == "Shooter"
                                        ) {
                                            if (getPlayerData(p)!!
                                                    .weaponClass
                                                    ?.mainWeapon!!
                                                    .slidingShootTick > 1
                                            ) {
                                                getPlayerData(p)!!.isUsingManeuver = true
                                            } else {
                                                Shooter.shooterRunnable(p)
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
