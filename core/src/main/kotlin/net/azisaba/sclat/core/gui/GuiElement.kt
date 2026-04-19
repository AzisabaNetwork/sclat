package net.azisaba.sclat.core.gui

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

data class GuiElement(
    val stack: ItemStack,
    val onClick: Consumer<Inventory>,
) {
    fun item(
        material: Material,
        amount: Int = 1,
        onClick: Consumer<Inventory>,
    ) = GuiElement(ItemStack(material, amount), onClick)
}
