package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.Color
import be4rjp.sclat.data.DataMgr.addColorList
import be4rjp.sclat.data.DataMgr.setColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

/**
 *
 * @author Be4rJP
 */
object ColorMgr {
    @Synchronized
    fun SetupColor() {
        val blue = Color("Blue")
        blue.wool = (Material.BLUE_WOOL)
        blue.concrete = (Material.BLUE_CONCRETE)
        blue.glass = (Material.BLUE_STAINED_GLASS)
        blue.colorCode = "§9"
        blue.bukkitColor = org.bukkit.Color.BLUE
        val bh = ItemStack(Material.LEATHER_HELMET, 1)
        val bhm = bh.getItemMeta() as LeatherArmorMeta?
        bhm!!.setColor(org.bukkit.Color.BLUE)
        bh.setItemMeta(bhm)
        blue.bougu = (bh)
        blue.chatColor = ChatColor.BLUE
        if (Sclat.colors.isEmpty() || Sclat.colors.contains("Blue")) {
            setColor("Blue", blue)
            addColorList(blue)
        }

        val aqua = Color("Aqua")
        aqua.wool = (Material.LIGHT_BLUE_WOOL)
        aqua.concrete = (Material.LIGHT_BLUE_CONCRETE)
        aqua.glass = (Material.LIGHT_BLUE_STAINED_GLASS)
        aqua.colorCode = "§b"
        aqua.bukkitColor = org.bukkit.Color.AQUA
        val ah = ItemStack(Material.LEATHER_HELMET, 1)
        val ahm = ah.getItemMeta() as LeatherArmorMeta?
        ahm!!.setColor(org.bukkit.Color.AQUA)
        ah.setItemMeta(ahm)
        aqua.bougu = (ah)
        aqua.chatColor = ChatColor.AQUA
        if (Sclat.colors.isEmpty() || Sclat.colors.contains("Aqua")) {
            setColor("Aqua", aqua)
            addColorList(aqua)
        }

        val orange = Color("Orange")
        orange.wool = (Material.ORANGE_WOOL)
        orange.concrete = (Material.ORANGE_CONCRETE)
        orange.glass = (Material.ORANGE_STAINED_GLASS)
        orange.colorCode = "§6"
        orange.bukkitColor = org.bukkit.Color.ORANGE
        val oh = ItemStack(Material.LEATHER_HELMET, 1)
        val ohm = oh.getItemMeta() as LeatherArmorMeta?
        ohm!!.setColor(org.bukkit.Color.ORANGE)
        oh.setItemMeta(ohm)
        orange.bougu = (oh)
        orange.chatColor = ChatColor.GOLD
        if (Sclat.colors.isEmpty() || Sclat.colors.contains("Orange")) {
            setColor("Orange", orange)
            addColorList(orange)
        }

        val lime = Color("Lime")
        lime.wool = (Material.LIME_WOOL)
        lime.concrete = (Material.LIME_CONCRETE)
        lime.glass = (Material.LIME_STAINED_GLASS)
        lime.colorCode = "§a"
        lime.bukkitColor = org.bukkit.Color.LIME
        val lh = ItemStack(Material.LEATHER_HELMET, 1)
        val lhm = lh.getItemMeta() as LeatherArmorMeta?
        lhm!!.setColor(org.bukkit.Color.LIME)
        lh.setItemMeta(lhm)
        lime.bougu = (lh)
        lime.chatColor = ChatColor.GREEN
        if (Sclat.colors.isEmpty() || Sclat.colors.contains("Lime")) {
            setColor("Lime", lime)
            addColorList(lime)
        }

        val y = Color("Yellow")
        y.wool = (Material.YELLOW_WOOL)
        y.concrete = (Material.YELLOW_CONCRETE)
        y.glass = (Material.YELLOW_STAINED_GLASS)
        y.colorCode = "§e"
        y.bukkitColor = org.bukkit.Color.YELLOW
        val yh = ItemStack(Material.LEATHER_HELMET, 1)
        val yhm = yh.getItemMeta() as LeatherArmorMeta?
        yhm!!.setColor(org.bukkit.Color.YELLOW)
        yh.setItemMeta(yhm)
        y.bougu = (yh)
        y.chatColor = ChatColor.YELLOW
        if (Sclat.colors.isEmpty() || Sclat.colors.contains("Yellow")) {
            setColor("Yellow", y)
            addColorList(y)
        }
    }
}
