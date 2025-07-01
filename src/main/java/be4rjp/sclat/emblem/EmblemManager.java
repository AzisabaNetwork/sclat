package be4rjp.sclat.emblem;

import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import static be4rjp.sclat.Main.conf;

public class EmblemManager {
    private static ItemStack newEmblemStack(String displayName, List<String> lore, int amount) {
        ItemStack stack = new ItemStack(Material.EGG, amount);
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
        String strUuid = player.getUniqueId().toString();
        ConfigurationSection userSection = conf.getEmblemUserdata().getConfigurationSection(strUuid);
        Set<String> cache = new HashSet<>();
        if(userSection != null) {
            cache = userSection.getKeys(false);
        }

        List<String> newEmblems = new ArrayList<>();
        for(EmblemData emblem: emblems) {
            // === Condition check
            if(!cache.contains(emblem.itemName)) {
                // non-cached
                if(!emblem.condition.apply(player)) {
                    // non-matched
                    continue;
                }
                // add new emblem
                newEmblems.add(emblem.itemName);
            }

            // get lore of item
            List<String> lore = conf.getEmblemItems().getStringList(emblem.itemName);

            int amount = conf.getEmblemUserdata().getInt(strUuid + "." + emblem.itemName, 1);

            // add emblem to inventory
            inventory.addItem(newEmblemStack(emblem.itemName, lore, amount));
        }

        // On new emblem
        if(!newEmblems.isEmpty()) {
            // update cache
            newEmblems.forEach(_emblem -> {
                conf.getEmblemUserdata().set(strUuid + "." + _emblem, 1);
            });

            // player feedback
            StringJoiner sj = new StringJoiner(", ");
            newEmblems.forEach(sj::add);
            player.sendMessage(sj + " の称号を手に入れました！");
        }
    }

    public static Map<String, List<String>> getDataMap() {
        HashMap<String, List<String>> dataMap = new HashMap<>();
        for(String uuid: conf.getEmblemUserdata().getKeys(false)) {
            List<String> emblems = conf.getEmblemUserdata().getStringList(uuid);
            for(String _emblemName: emblems) {
                if(!dataMap.containsKey(_emblemName)) {
                    dataMap.put(_emblemName, new ArrayList<>());
                }
                dataMap.get(_emblemName).add(uuid);
            }
        }
        return dataMap;
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
