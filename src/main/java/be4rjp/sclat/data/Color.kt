package be4rjp.sclat.data

import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 *
 * @author Be4rJP
 */
class Color(
    val colorName: String?,
) {
    var colorCode: String? = null
    var isUsed: Boolean = false

    @JvmField
    var wool: Material? = null
    var bukkitColor: Color? = null

    @JvmField
    var glass: Material? = null

    @JvmField
    var concrete: Material? = null

    @JvmField
    var bougu: ItemStack? = null
    var chatColor: ChatColor? = null
}
