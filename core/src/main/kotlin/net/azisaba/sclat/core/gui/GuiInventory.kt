package net.azisaba.sclat.core.gui

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class GuiInventory {
    val elementMap: MutableMap<Int, GuiElement> = mutableMapOf()

    fun item(
        slot: Int,
        material: Material,
        amount: Int = 1,
        onClick: Consumer<Inventory>,
    ) {
        if (elementMap.containsKey(slot)) {
            throw IllegalArgumentException("Slot $slot is already taken by ${elementMap[slot]}")
        }
        elementMap[slot] = GuiElement(ItemStack(material, amount), onClick)
    }

    fun onClick(
        slot: Int,
        inventory: Inventory,
    ) {
        elementMap[slot].let {
            if (it == null) {
            } else {
                it.onClick.accept(inventory)
            }
        }
    }
}
