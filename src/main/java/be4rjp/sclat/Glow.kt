package be4rjp.sclat

import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.enchantments.EnchantmentWrapper
import org.bukkit.inventory.ItemStack

class Glow : EnchantmentWrapper("sclatg") {
    override fun canEnchantItem(item: ItemStack): Boolean = true

    override fun conflictsWith(other: Enchantment): Boolean = false

    override fun getItemTarget(): EnchantmentTarget = EnchantmentTarget.ALL

    override fun getMaxLevel(): Int = 10

    override fun getName(): String = "sclatg"

    override fun getStartLevel(): Int = 1

    fun enchantGlow(`is`: ItemStack): ItemStack {
        enableGlow()
        `is`.addEnchantment(glow!!, 1)
        return `is`
    }

    fun removeGlow(`is`: ItemStack): ItemStack {
        enableGlow()
        `is`.removeEnchantment(glow!!)
        return `is`
    }

    fun isGlowing(`is`: ItemStack): Boolean {
        enableGlow()
        return `is`.getEnchantments().containsKey(glow)
    }

    fun enableGlow() {
        try {
            if (glow == null) {
                glow = Glow()
                val f = Enchantment::class.java.getDeclaredField("acceptingNew")
                f.setAccessible(true)
                f.set(null, true)
                val hmapf = Enchantment::class.java.getDeclaredField("byName")
                hmapf.setAccessible(true)
                val hmap = hmapf.get(hmapf) as MutableMap<*, *>
                if (!hmap.containsKey("sclatg")) {
                    registerEnchantment(glow!!)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        private var glow: Glow? = null
    }
}
