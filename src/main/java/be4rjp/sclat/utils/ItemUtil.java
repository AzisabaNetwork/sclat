package be4rjp.sclat.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.function.Consumer;

public class ItemUtil {
    public static ItemStack getStack(Material material, String displayName, String... lore) {
        return getStack(material, 1, displayName, lore);
    }

    public static ItemStack getStack(Material material, int count, String displayName, String... lore) {
        ItemStack stack = new ItemStack(material, count);

        // edit item metadata
        editMeta(stack, meta -> {
            meta.setDisplayName(displayName);
            if(lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
        });

        // return edited stack
        return stack;
    }

    public static ItemStack setLore(ItemStack stack, String... lore) {
        editMeta(stack, meta -> {
            meta.setLore(Arrays.asList(lore));
        });
        return stack;
    }

    public static ItemStack editMeta(ItemStack targetStack, Consumer<ItemMeta> metaEditor) {
        ItemMeta meta = targetStack.getItemMeta();
        metaEditor.accept(meta);
        targetStack.setItemMeta(meta);
        return targetStack;
    }

    /**
     * returns stack (BLACK_STAINED_GLASS_PANE named ".")
     * @return ItemStack for filling UI blank.
     */
    public static ItemStack getUIBlank() {
        return getUIBlank(Material.BLACK_STAINED_GLASS_PANE);
    }

    public static ItemStack getUIBlank(Material material) {
        return ItemUtil.getStack(
                material,
                "."
        );
    }
}
