package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeapon
import be4rjp.sclat.data.DataMgr.setWeaponClass
import be4rjp.sclat.data.WeaponClass
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 *
 * @author Be4rJP
 */
object WeaponClassMgr {
    @Synchronized
    fun WeaponClassSetup() {
        for (classname in Sclat.Companion.conf!!
            .classConfig!!
            .getConfigurationSection("WeaponClass")!!
            .getKeys(false)) {
            val WeaponName =
                Sclat.Companion.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".MainWeaponName")
            val SubWeaponName =
                Sclat.Companion.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".SubWeaponName")
            val SPWeaponName =
                Sclat.Companion.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".SPWeaponName")
            val wc = WeaponClass(classname)
            wc.mainWeapon = (getWeapon(WeaponName))
            wc.subWeaponName = SubWeaponName
            wc.sPWeaponName = SPWeaponName

            setWeaponClass(classname, wc)
        }
    }

    @JvmStatic
    fun setWeaponClass(player: Player) {
        player.getInventory().clear()
        val data = getPlayerData(player)
        val main =
            data!!
                .weaponClass!!
                .mainWeapon!!
                .weaponIteamStack!!
                .clone()
        if (data.mainItemGlow) {
            Sclat.glow!!.enchantGlow(main)
            main.addEnchantment(Sclat.glow!!, 1)
        }
        player.getInventory().setItem(0, main)
        if (data.weaponClass!!.mainWeapon!!.isManeuver) {
            player
                .getInventory()
                .setItem(
                    40,
                    data.weaponClass!!
                        .mainWeapon!!
                        .weaponIteamStack!!
                        .clone(),
                )
        }
        val `is` = SubWeaponMgr.getSubWeapon(player)
        player.getInventory().setItem(2, `is`)
        val co = ItemStack(Material.BOOK)
        val meta = co.getItemMeta()
        meta!!.setDisplayName("スーパージャンプ")
        co.setItemMeta(meta)
        player.getInventory().setItem(6, co)
        if (!data.isSquid) player.getEquipment()!!.setHelmet(getPlayerData(player)!!.team!!.teamColor!!.bougu)

        if (data.sPGauge == 100) SPWeaponMgr.setSPWeapon(player)

        if (Sclat.Companion.conf!!
                .config!!
                .getString("WorkMode") == "Trial" &&
            !Sclat.tutorial
        ) {
            val join = ItemStack(Material.CHEST)
            val joinmeta = join.getItemMeta()
            joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
            join.setItemMeta(joinmeta)
            player.getInventory().setItem(7, join)
        }
    }
}
