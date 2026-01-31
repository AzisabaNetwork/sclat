package be4rjp.sclat.data

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
class KasaData(
    @JvmField val player: Player?,
) {
    var armorStandList: MutableList<ArmorStand> = mutableListOf()

    @JvmField
    var damage: Double = 0.0
}
