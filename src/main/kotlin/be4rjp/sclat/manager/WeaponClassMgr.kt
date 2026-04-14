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
    fun weaponClassSetup() {
        for (classname in Sclat.conf!!
            .classConfig!!
            .getConfigurationSection("WeaponClass")!!
            .getKeys(false)) {
            val weaponName =
                Sclat.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".MainWeaponName")
            val subWeaponName =
                Sclat.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".SubWeaponName")
            val spWeaponName =
                Sclat.conf!!
                    .classConfig!!
                    .getString("WeaponClass." + classname + ".SPWeaponName")
            val wc = WeaponClass(classname)
            wc.mainWeapon = (getWeapon(weaponName))
            wc.subWeaponName = subWeaponName
            wc.sPWeaponName = spWeaponName

            setWeaponClass(classname, wc)
        }
    }

    @JvmStatic
    fun setWeaponClass(player: Player) {
        player.inventory.clear()
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
        player.inventory.setItem(0, main)
        if (data.weaponClass!!.mainWeapon!!.isManeuver) {
            player
                .inventory
                .setItem(
                    40,
                    data.weaponClass!!
                        .mainWeapon!!
                        .weaponIteamStack!!
                        .clone(),
                )
        }
        val `is` = SubWeaponMgr.getSubWeapon(player)
        player.inventory.setItem(2, `is`)
        val co = ItemStack(Material.BOOK)
        val meta = co.itemMeta
        meta!!.setDisplayName("スーパージャンプ")
        co.itemMeta = meta
        player.inventory.setItem(6, co)
        if (!data.isSquid) player.equipment!!.helmet = getPlayerData(player)!!.team!!.teamColor!!.bougu

        if (data.sPGauge == 100) SPWeaponMgr.setSPWeapon(player)

        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial" &&
            !Sclat.tutorial
        ) {
            val join = ItemStack(Material.CHEST)
            val joinmeta = join.itemMeta
            joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
            join.itemMeta = joinmeta
            player.inventory.setItem(7, join)
        }
    }
}
