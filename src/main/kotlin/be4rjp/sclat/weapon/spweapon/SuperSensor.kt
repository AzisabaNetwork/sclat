package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.plugin
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object SuperSensor {
    @JvmStatic
    fun SuperSensorRunnable(player: Player) {
        getPlayerData(player)!!.isUsingSP = true
        SPWeaponMgr.setSPCoolTimeAnimation(player, 200)
        for (o_player in plugin.server.onlinePlayers) {
            if (getPlayerData(player)!!.team != getPlayerData(o_player)!!.team &&
                getPlayerData(o_player)!!.isInMatch
            ) {
                o_player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
            }
        }
        for (`as` in player.world.entities) {
            if (`as`.customName != null) {
                if (`as` is ArmorStand &&
                    (`as`.customName != "Path") &&
                    (`as`.customName != "21") &&
                    (`as`.customName != "100") &&
                    (`as`.customName != "SplashShield") &&
                    (`as`.customName != "Kasa")
                ) {
                    `as`.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
                }
            }
        }
        val sound: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                    getPlayerData(player)!!.isUsingSP = false
                }
            }
        sound.runTaskLater(plugin, 200)
    }
}
