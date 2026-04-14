package be4rjp.sclat.emblem

import be4rjp.sclat.Sclat
import be4rjp.sclat.manager.PlayerStatusMgr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.function.Function

object EmblemManager {
    private fun newEmblemStack(
        displayName: String,
        lore: MutableList<String>,
        amount: Int,
    ): ItemStack {
        val stack = ItemStack(Material.EGG, amount)
        val meta = stack.itemMeta
        meta!!.setDisplayName(displayName)
        meta.lore = lore
        stack.itemMeta = meta
        return stack
    }

    var emblems: MutableList<EmblemData> = ArrayList()

    fun addEmblem(
        itemName: String,
        condition: Function<Player, Boolean>,
    ) {
        emblems.add(EmblemData(itemName, condition))
    }

    @JvmStatic
    fun handleInv(
        inventory: Inventory,
        player: Player,
    ) {
        val strUuid = player.uniqueId.toString()
        val userSection = Sclat.conf?.emblemUserdata!!.getConfigurationSection(strUuid)
        var cache: MutableSet<String> = HashSet()
        if (userSection != null) {
            cache = userSection.getKeys(false)
        }

        val newEmblems: MutableList<String> = ArrayList()
        for (emblem in emblems) {
            // === Condition check
            if (!cache.contains(emblem.itemName)) {
                // non-cached
                if (!emblem.condition.apply(player)) {
                    // non-matched
                    continue
                }
                // add new emblem
                newEmblems.add(emblem.itemName)
            }

            // get lore of item
            val lore = Sclat.conf?.emblemItems!!.getStringList(emblem.itemName)

            val amount = Sclat.conf?.emblemUserdata!!.getInt(strUuid + "." + emblem.itemName, 1)

            // add emblem to inventory
            inventory.addItem(newEmblemStack(emblem.itemName, lore, amount))
        }

        // On new emblem
        if (!newEmblems.isEmpty()) {
            // update cache
            newEmblems.forEach(
                Consumer { emblem: String? ->
                    Sclat.conf?.emblemUserdata!!.set("$strUuid.$emblem", 1)
                },
            )

            // player feedback
            player.sendMessage("${newEmblems.joinToString(", ")} の称号を手に入れました！")
        }
    }

    val dataMap: MutableMap<String, MutableMap<String, Int>>
        get() {
            val dataMap =
                HashMap<String, MutableMap<String, Int>>()
            for (uuid in Sclat.conf?.emblemUserdata!!.getKeys(false)) {
                val targetSection =
                    Sclat.conf?.emblemUserdata!!.getConfigurationSection(uuid) ?: continue
                for (emblemName in targetSection.getKeys(false)) {
                    if (!dataMap.containsKey(emblemName)) {
                        dataMap[emblemName] = HashMap()
                    }
                    dataMap[emblemName]!![uuid] = targetSection.getInt(emblemName, 1)
                }
            }
            return dataMap
        }

    init {
        addEmblem("撃墜王") { p -> PlayerStatusMgr.getKill(p) >= 10000 }

        addEmblem("エース") { p -> PlayerStatusMgr.getKill(p) >= 1000 }

        addEmblem("100人斬り") { p -> PlayerStatusMgr.getKill(p) >= 100 }

        // マイクラスクエア2025関連
        addEmblem("§a§lマイクラスクエア§2§l2025 §7§lSupportMedal") { p -> false }

        addEmblem("§a§lマイクラスクエア§2§l2025 §e§lSupportMedal") { p -> false }

        addEmblem("§a§lマイクラスクエア§2§l2025 §b§lSupportMedal") { p -> false }
    }
}
