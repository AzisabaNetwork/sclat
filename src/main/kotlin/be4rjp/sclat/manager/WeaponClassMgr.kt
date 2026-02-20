package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeapon
import be4rjp.sclat.data.DataMgr.setWeaponClass
import be4rjp.sclat.data.WeaponClass
import be4rjp.sclat.extension.component
import be4rjp.sclat.extension.editMeta
import be4rjp.sclat.extension.serializeString
import net.kyori.adventure.text.format.NamedTextColor
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

        // main weapon
        val mainWeapon = data.weaponClass?.mainWeapon!!
        val main =
            mainWeapon
                .weaponItemStack!!
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
                        .weaponItemStack!!
                        .clone(),
                )
        }

        // sub weapon
        val stack = SubWeaponMgr.getSubWeapon(player)
        player.inventory.setItem(2, stack)

        // super jump
        val co =
            ItemStack(Material.BOOK).editMeta {
                it.setDisplayName(component("スーパージャンプ").serializeString())
            }
        player.inventory.setItem(6, co)

        // set helmet to team colored one
        if (!data.isSquid) player.equipment?.helmet = getPlayerData(player)?.team?.teamColor?.bougu

        // set sp weapon when full-charged
        if (data.sPGauge == 100) SPWeaponMgr.setSPWeapon(player)

        // Todo: migrate WorkMode to enum?
        // When it's trial mode and non-tutorial
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial" &&
            !Sclat.tutorial
        ) {
            val join =
                ItemStack(Material.CHEST).editMeta {
                    it.setDisplayName(component("右クリックでメインメニューを開く", NamedTextColor.GOLD).serializeString())
                }
            player.inventory.setItem(7, join)
        }
    }
}
