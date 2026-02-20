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
        val weaponClassSection = Sclat.conf?.classConfig?.getConfigurationSection("WeaponClass") ?: return
        for (classname in weaponClassSection.getKeys(false)) {
            val weaponSection = weaponClassSection.getConfigurationSection(classname) ?: continue

            // create weapon class instance
            val wc =
                WeaponClass(classname).apply {
                    this.mainWeapon = getWeapon(weaponSection.getString("MainWeaponName"))
                    this.subWeaponName = weaponSection.getString("SubWeaponName")
                    this.sPWeaponName = weaponSection.getString("SPWeaponName")
                }

            setWeaponClass(classname, wc)
        }
    }

    @JvmStatic
    fun setWeaponClass(player: Player) {
        player.inventory.clear()
        val data = getPlayerData(player)!!
        val mainWeapon =
            data
                .weaponClass
                ?.mainWeapon!!
        val main =
            mainWeapon
                .weaponIteamStack!!
                .clone()
                .apply {
                    // Todo: migrate to custom nbt
                    if (data.mainItemGlow) {
                        Sclat.glow!!.enchantGlow(this)
                        this.addEnchantment(Sclat.glow!!, 1)
                    }
                }

        player.inventory.setItem(0, main)
        if (mainWeapon.isManeuver) {
            player
                .inventory
                .setItem(
                    40,
                    mainWeapon
                        .weaponIteamStack!!
                        .clone(),
                )
        }
        val stack = SubWeaponMgr.getSubWeapon(player)
        player.inventory.setItem(2, stack)
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
