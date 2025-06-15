package be4rjp.sclat.emblem;

import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import static be4rjp.sclat.Main.conf;

public class EmblemManager {
    private static ItemStack newEmblemStack(String displayName, List<String> lore) {
        ItemStack stack = new ItemStack(Material.EGG);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static List<EmblemData> emblems = new ArrayList<>();

    public static void addEmblem(String itemName, Function<Player, Boolean> condition) {
        emblems.add(new EmblemData(itemName, condition));
    }

    public static void handleInv(Inventory inventory, Player player) {
        List<String> cache = conf.getEmblems().getStringList(player.getUniqueId().toString());
        List<String> newEmblems = new ArrayList<>();
        for (EmblemData emblem : emblems) {
            // === Condition check
            if (!cache.contains(emblem.itemName)) {
                // non-cached
                if (!emblem.condition.apply(player)) {
                    // non-matched
                    continue;
                }
                // add new emblem
                newEmblems.add(emblem.itemName);
            }

            // get lore of item
            List<String> lore = conf.getEmblemItems().getStringList(emblem.itemName);

            // add emblem to inventory
            inventory.addItem(newEmblemStack(emblem.itemName, lore));
        }

        // On new emblem
        if (!newEmblems.isEmpty()) {
            // update cache
            ArrayList<String> newEmblemCache = new ArrayList<>(cache);
            newEmblemCache.addAll(newEmblems);
            conf.getEmblems().set(player.getUniqueId().toString(), newEmblemCache);

            // player feedback
            StringJoiner sj = new StringJoiner(", ");
            newEmblems.forEach(sj::add);
            player.sendMessage(sj + " の称号を手に入れました！");
        }
    }

    static {
        addEmblem(
                "撃墜王",
                p -> PlayerStatusMgr.getKill(p) >= 10000
        );

        addEmblem(
                "エース",
                p -> PlayerStatusMgr.getKill(p) >= 1000
        );

        addEmblem(
                "100人斬り",
                p -> PlayerStatusMgr.getKill(p) >= 100
        );

        // マイクラスクエア2025関連
        addEmblem(
                "§a§lマイクラスクエア§2§l2025 §7§lSupportMedal",
                p -> false
        );

        addEmblem(
                "§a§lマイクラスクエア§2§l2025 §e§lSupportMedal",
                p -> false
        );

        addEmblem(
                "§a§lマイクラスクエア§2§l2025 §b§lSupportMedal",
                p -> false
        );
    }
}
