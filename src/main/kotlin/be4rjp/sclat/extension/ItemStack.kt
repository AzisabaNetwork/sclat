package be4rjp.sclat.extension

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.function.Consumer

fun ItemStack.editMeta(applyFunc: Consumer<ItemMeta>): ItemStack =
    apply {
        itemMeta = itemMeta?.apply { applyFunc.accept(this) }
    }
