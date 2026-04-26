package be4rjp.sclat

import net.azisaba.sclat.core.DelegatedLogger
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.enchantments.EnchantmentWrapper
import org.bukkit.inventory.ItemStack

class Glow : EnchantmentWrapper(ENCHANT_NAME) {
    override fun canEnchantItem(item: ItemStack): Boolean = true

    override fun conflictsWith(other: Enchantment): Boolean = false

    override fun getItemTarget(): EnchantmentTarget = EnchantmentTarget.ALL

    override fun getMaxLevel(): Int = 10

    override fun getName(): String = ENCHANT_NAME

    override fun getStartLevel(): Int = 1

    fun enchantGlow(stack: ItemStack): ItemStack =
        stack.apply {
            addEnchantment(glow, 1)
        }

    fun removeGlow(stack: ItemStack): ItemStack = stack.apply { removeEnchantment(glow) }

    fun isGlowing(stack: ItemStack): Boolean = stack.enchantments.containsKey(glow)

    companion object {
        private const val ENCHANT_NAME = "sclatg"
        private val logger by DelegatedLogger()
        private val glow: Glow by lazy {
            Glow().apply {
                try {
                    Enchantment::class.java.getDeclaredField("acceptingNew").apply {
                        isAccessible = true
                        set(null, true)
                    }
                    @Suppress("UNCHECKED_CAST")
                    val hmap =
                        Enchantment::class.java
                            .getDeclaredField("byName")
                            .apply { isAccessible = true }
                            .let { it.get(it) } as MutableMap<String, Enchantment>
                    if (!hmap.containsKey(ENCHANT_NAME)) {
                        registerEnchantment(glow)
                    } else {
                        logger.warn("Glow enchantment is already registered... why?")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to register glow enchantment", e)
                }
            }
        }
    }
}
