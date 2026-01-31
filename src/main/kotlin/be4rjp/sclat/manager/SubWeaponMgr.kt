package be4rjp.sclat.manager

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.weapon.spweapon.JetPack.shootJetPack
import be4rjp.sclat.weapon.spweapon.LitterFiveG.chargeLitterFiveG
import be4rjp.sclat.weapon.spweapon.LitterFiveG.shootLitterFiveG
import be4rjp.sclat.weapon.spweapon.QuadroArms.quadroCooltime
import be4rjp.sclat.weapon.spweapon.SuperShot.shot
import be4rjp.sclat.weapon.spweapon.SwordMord.attackSword
import be4rjp.sclat.weapon.subweapon.Beacon.setBeacon
import be4rjp.sclat.weapon.subweapon.Boomerang.boomerangRunnable
import be4rjp.sclat.weapon.subweapon.CurlingBomb.curlingBombRunnable
import be4rjp.sclat.weapon.subweapon.FloaterBomb.floaterBombRunnable
import be4rjp.sclat.weapon.subweapon.KBomb.kBomRunnable
import be4rjp.sclat.weapon.subweapon.Poison.poisonRunnable
import be4rjp.sclat.weapon.subweapon.QuickBomb.quickBomRunnable
import be4rjp.sclat.weapon.subweapon.Sensor.sensorRunnable
import be4rjp.sclat.weapon.subweapon.SplashBomb.SplashBomRunnable
import be4rjp.sclat.weapon.subweapon.SplashShield.splashShieldThrowRunnable
import be4rjp.sclat.weapon.subweapon.Sprinkler.SprinklerRunnable
import be4rjp.sclat.weapon.subweapon.Trap.useTrap
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 *
 * @author Be4rJP
 */
object SubWeaponMgr {
    fun getSubWeapon(player: Player?): ItemStack {
        val data = getPlayerData(player)
        // player.sendMessage(data.weaponClass.getSubWeaponName());
        var stack: ItemStack? = null
        var ism: ItemMeta? = null

        when (data!!.weaponClass!!.subWeaponName) {
            "スプラッシュボム" -> {
                stack = ItemStack(data.team!!.teamColor!!.glass!!)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("スプラッシュボム")
            }

            "クイックボム" -> {
                stack = ItemStack(data.team!!.teamColor!!.wool!!)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("クイックボム")
            }

            "フローターボム" -> {
                stack = ItemStack(data.team!!.teamColor!!.wool!!)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("フローターボム")
            }

            "ブーメランボム" -> {
                stack = ItemStack(Material.IRON_NUGGET)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("ブーメランボム")
            }

            "センサー" -> {
                stack = ItemStack(Material.DISPENSER)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("センサー")
            }

            "ポイズン" -> {
                stack = ItemStack(Material.PRISMARINE)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("ポイズン")
            }

            "キューバンボム" -> {
                stack = ItemStack(data.team!!.teamColor!!.concrete!!)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("キューバンボム")
            }

            "ビーコン" -> {
                stack = ItemStack(Material.IRON_TRAPDOOR)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("ビーコン")
            }

            "スプリンクラー" -> {
                stack = ItemStack(Material.BIRCH_FENCE_GATE)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("スプリンクラー")
            }

            "スプラッシュシールド" -> {
                stack = ItemStack(Material.ACACIA_FENCE)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("スプラッシュシールド")
            }

            "カーリングボム" -> {
                stack = ItemStack(Material.QUARTZ_SLAB)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("カーリングボム")
            }

            "トラップ" -> {
                stack = ItemStack(Material.MUSIC_DISC_STAL)
                ism = stack.getItemMeta()
                ism!!.setDisplayName("トラップ")
            }
        }
        stack?.itemMeta = ism
        // player.getInventory().setItem(2, is);
        return stack ?: ItemStack(Material.AIR)
    }

    fun UseSubWeapon(
        player: Player,
        name: String,
    ) {
        val data = getPlayerData(player)
        if (!data!!.canUseSubWeapon) return
        if (player.getGameMode() == GameMode.SPECTATOR) return

        when (name) {
            "右クリックで弾を発射" -> {
                shootJetPack(player)
                data.canUseSubWeapon = (false)
            }

            "右クリックで発射！" -> {
                shot(player)
                data.canUseSubWeapon = (false)
            }

            "右クリックで斬撃、シフトで防御" -> {
                attackSword(player)
                data.canUseSubWeapon = (false)
            }

            "Quadro-BLUE" -> {
                quadroCooltime(player, 1)
                data.canUseSubWeapon = (false)
            }

            "Quadro-GREEN" -> {
                quadroCooltime(player, 2)
                data.canUseSubWeapon = (false)
            }

            "Quadro-RED" -> {
                quadroCooltime(player, 3)
                data.canUseSubWeapon = (false)
            }

            "Quadro-WHITE" -> {
                quadroCooltime(player, 4)
                data.canUseSubWeapon = (false)
            }

            "右クリックで射撃!" -> {
                if (data.settings!!.showEffectChargerLine()) {
                    shootLitterFiveG(player)
                    data.canUseSubWeapon = (false)
                } else {
                    chargeLitterFiveG(player)
                    data.canUseSubWeapon = (true)
                }
            }
        }

        if (data.isUsingJetPack) return

        when (name) {
            "スプラッシュボム" -> {
                SplashBomRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "クイックボム" -> {
                quickBomRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "フローターボム" -> {
                floaterBombRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "センサー" -> {
                sensorRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "ポイズン" -> {
                poisonRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "キューバンボム" -> {
                kBomRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "ビーコン" -> {
                setBeacon(player)
                data.canUseSubWeapon = (false)
            }

            "スプリンクラー" -> {
                SprinklerRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "スプラッシュシールド" -> {
                splashShieldThrowRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "カーリングボム" -> {
                curlingBombRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "ブーメランボム" -> {
                boomerangRunnable(player)
                data.canUseSubWeapon = (false)
            }

            "トラップ" -> {
                useTrap(player)
                data.canUseSubWeapon = (false)
            }
        }
    }
}
