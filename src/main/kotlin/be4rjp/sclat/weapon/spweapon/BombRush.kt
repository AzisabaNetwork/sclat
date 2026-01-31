package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.plugin
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object BombRush {
    @JvmStatic
    fun bombRushRunnable(player: Player?) {
        val data = getPlayerData(player)
        data!!.isBombRush = true
        data.isUsingSP = true
        SPWeaponMgr.setSPCoolTimeAnimation(player!!, 120)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    data.isBombRush = false
                    // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                    data.isUsingSP = false
                }
            }
        task.runTaskLater(plugin, 120)
    }
}
