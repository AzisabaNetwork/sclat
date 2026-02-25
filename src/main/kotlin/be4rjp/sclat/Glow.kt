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

    fun enchantGlow(stack: ItemStack): ItemStack =
        stack.apply {
            addEnchantment(glow, 1)
        }

    fun removeGlow(stack: ItemStack): ItemStack =
        stack.apply {
            removeEnchantment(glow)
        }

    fun isGlowing(stack: ItemStack): Boolean = stack.enchantments.containsKey(glow)

    companion object {
        private var glow: Glow =
            Glow().also {
                Enchantment::class.java.getDeclaredField("acceptingNew").apply {
                    isAccessible = true
                    set(null, true)
                }
                val registered =
                    Enchantment::class.java
                        .getDeclaredField("byName")
                        .apply {
                            isAccessible = true
                        }.let { it.get(it) as MutableMap<*, *> }
                        .containsKey("sclatg")
                if (!registered) {
                    registerEnchantment(it)
                }
            }
    }
}
