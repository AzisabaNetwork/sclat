package be4rjp.sclat.data

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class SplashShieldData(
    @JvmField val player: Player?,
) {
    @JvmField
    var task: BukkitRunnable? = null
    var armorStandList: MutableList<ArmorStand> = mutableListOf()

    @JvmField
    var damage: Double = 0.0

    @JvmField
    var isDeploy: Boolean = false
}
