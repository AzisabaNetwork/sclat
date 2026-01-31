package be4rjp.sclat.manager

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.setMainWeapon
import be4rjp.sclat.data.MainWeapon
import be4rjp.sclat.weapon.Blaster.shootBlaster
import be4rjp.sclat.weapon.Brush
import be4rjp.sclat.weapon.Bucket.shootBucket
import be4rjp.sclat.weapon.Burst.burstCooltime
import be4rjp.sclat.weapon.Kasa.shootKasa
import be4rjp.sclat.weapon.Roller
import be4rjp.sclat.weapon.Slosher.shootSlosher
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 *
 * @author Be4rJP
 */
object MainWeaponMgr {
    @Synchronized
    fun setupMainWeapon() {
        for (weaponname in Sclat.conf!!
            .weaponConfig!!
            .getConfigurationSection("MainWeapon")!!
            .getKeys(false)) {
            val weaponType =
                Sclat.conf!!
                    .weaponConfig!!
                    .getString("MainWeapon." + weaponname + ".WeaponType")
            val weaponMaterial =
                Material
                    .getMaterial(
                        Sclat.conf!!
                            .weaponConfig!!
                            .getString("MainWeapon." + weaponname + ".WeaponMaterial")!!,
                    )
            val random =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".ShootRandom")
            val distick =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".ShootDistance")
            val shootspeed =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".ShootSpeed")
            val shoottick =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".ShootTick")
            val paintrandom =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".PaintRandom")
            val maxpaintdis =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".MaxPaintDistance")
                    .toDouble()
            val needink =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".NeedInk")
                    .toFloat()
            val damage =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".Damage")
            val maxcharge =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".MaxCharge")
            val rollershootQuantity =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".RollerShootQuantity")
            val usinwalkspeed =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".UsingWalkSpeed")
                    .toFloat()
            val rollerWidth =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".RollerWidth")
            val tatehuri =
                Sclat.conf!!
                    .weaponConfig!!
                    .getBoolean("MainWeapon." + weaponname + ".RollerTatehuri")
            val rollerdamage =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".RollerDamage")
            val rollerneedink =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".RollerNeedInk")
                    .toFloat()
            var exh = 0.0
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".BlasterExHankei")
            ) {
                exh =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getInt("MainWeapon." + weaponname + ".BlasterExHankei")
                        .toDouble()
            }
            val delay =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".Delay")
            val cooltime =
                Sclat.conf!!
                    .weaponConfig!!
                    .getInt("MainWeapon." + weaponname + ".Cooltime")
            val exd =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".BlasterExDamage")
            val hude =
                Sclat.conf!!
                    .weaponConfig!!
                    .getBoolean("MainWeapon." + weaponname + ".IsBrush")
            val huder =
                Sclat.conf!!
                    .weaponConfig!!
                    .getDouble("MainWeapon." + weaponname + ".BrushRandom")
            var man = false
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".IsManeuver")
            ) {
                man =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getBoolean("MainWeapon." + weaponname + ".IsManeuver")
            }
            var slST = 1
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".SlidingShootTick")
            ) {
                slST =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getInt("MainWeapon." + weaponname + ".SlidingShootTick")
            }

            var cr = 1.0
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".ChargeRatio")
            ) {
                cr =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getDouble("MainWeapon." + weaponname + ".ChargeRatio")
            }

            var ck = false
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".CanChargeKeep")
            ) {
                ck =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getBoolean("MainWeapon." + weaponname + ".CanChargeKeep")
            }

            var ckt = 0
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".ChargeKeepingTime")
            ) {
                ckt =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getInt("MainWeapon." + weaponname + ".ChargeKeepingTime")
            }

            var hc = false
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".HanbunChargeKeep")
            ) {
                hc =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getBoolean("MainWeapon." + weaponname + ".HanbunChargeKeep")
            }

            var sn = 0.2f
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".SlideNeedInk")
            ) {
                sn =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getDouble("MainWeapon." + weaponname + ".SlideNeedInk")
                    ).toFloat()
            }

            val mw = MainWeapon(weaponname)
            mw.weaponType = (weaponType)
            val `is` = ItemStack(weaponMaterial!!)
            val itemMeta = `is`.itemMeta
            itemMeta!!.setDisplayName(weaponname)
            `is`.itemMeta = itemMeta
            mw.setWeaponItemStack(`is`)
            mw.random = (random)
            mw.distanceTick = distick
            mw.shootSpeed = shootspeed
            mw.shootTick = shoottick
            mw.paintRandom = paintrandom
            mw.maxPaintDis = maxpaintdis
            mw.needInk = needink
            mw.damage = (damage)
            mw.maxCharge = maxcharge
            mw.rollerShootQuantity = rollershootQuantity
            mw.usingWalkSpeed = usinwalkspeed
            mw.rollerWidth = (rollerWidth)
            mw.rollerDamage = rollerdamage
            mw.rollerNeedInk = rollerneedink
            mw.canTatehuri = tatehuri
            mw.blasterExHankei = exh
            mw.delay = (delay)
            mw.coolTime = cooltime
            mw.blasterExDamage = exd
            mw.isHude = (hude)
            mw.hudeRandom = huder
            mw.isManeuver = (man)
            mw.slidingShootTick = slST
            mw.chargeRatio = cr
            mw.canChargeKeep = ck
            mw.chargeKeepingTime = (ckt)
            mw.hanbunCharge = hc
            mw.slideNeedINK = (sn)

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".SPRate")
            ) {
                mw.sPRate =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getDouble("MainWeapon." + weaponname + ".SPRate")
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".MaxRandom")
            ) {
                mw.maxRandom =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getDouble("MainWeapon." + weaponname + ".MaxRandom")
                    )
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".MaxRandomCount")
            ) {
                mw.maxRandomCount =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getInt("MainWeapon." + weaponname + ".MaxRandomCount")
                    )
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".Scope")
            ) {
                mw.scope =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getBoolean("MainWeapon." + weaponname + ".Scope")
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".DecreaseRate")
            ) {
                // チャージャーの非適性ダメージ減少率
                mw.decreaseRate =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getDouble("MainWeapon." + weaponname + ".DecreaseRate")
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".AppDistance")
            ) {
                // チャージャーの非適性距離
                mw.appDistance =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getInt("MainWeapon." + weaponname + ".AppDistance")
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".Money")
            ) {
                mw.money =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getInt("MainWeapon." + weaponname + ".Money")
                    )
            } else {
                mw.money = (0)
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".Level")
            ) {
                mw.level =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getInt("MainWeapon." + weaponname + ".Level")
                    )
            } else {
                mw.level = (0)
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".InHoldWalkSpeed")
            ) {
                mw.inHoldSpeed =
                    Sclat.conf!!
                        .weaponConfig!!
                        .getDouble("MainWeapon." + weaponname + ".InHoldWalkSpeed")
                        .toFloat()
            } else {
                mw.inHoldSpeed =
                    Sclat.conf!!
                        .config!!
                        .getDouble("PlayerWalkSpeed")
                        .toFloat()
            }

            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".IsLootBox")
            ) { // ガチャ武器用
                mw.islootbox =
                    (
                        Sclat.conf!!
                            .weaponConfig!!
                            .getBoolean("MainWeapon." + weaponname + ".IsLootBox")
                    )
            }
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".LootPro")
            ) { // ガチャ武器用排出率
                mw.lootpro = (
                    Sclat.conf!!
                        .weaponConfig!!
                        .getDouble("MainWeapon." + weaponname + ".LootPro")
                )
            }
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".IsSwapper")
            ) { // スワッパ―判別
                mw.setIsSwap(
                    Sclat.conf!!
                        .weaponConfig!!
                        .getBoolean("MainWeapon." + weaponname + ".IsSwapper"),
                )
            }
            if (Sclat.conf!!
                    .weaponConfig!!
                    .contains("MainWeapon." + weaponname + ".SwapWeapon")
            ) { // スワッパ―スワップ先
                mw.swap = (
                    Sclat.conf!!
                        .weaponConfig!!
                        .getString("MainWeapon." + weaponname + ".SwapWeapon")
                )
            }
            setMainWeapon(weaponname, mw)
        }
    }

    fun equalWeapon(player: Player): Boolean {
        try {
            val data = getPlayerData(player)
            val wname =
                data!!
                    .weaponClass!!
                    .mainWeapon!!
                    .weaponIteamStack!!
                    .itemMeta!!
                    .displayName
            if (player
                    .inventory
                    .itemInMainHand
                    .itemMeta
                    ?.displayName == null
            ) {
                return false
            }
            val itemname =
                player
                    .inventory
                    .itemInMainHand
                    .itemMeta!!
                    .displayName
            if (itemname.length >= wname.length) return wname == itemname.substring(0, wname.length)
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun useMainWeapon(player: Player) {
        if (equalWeapon(player)) {
            Sclat.dadadaCheckerAPI!!.fireClickEvent(player)

            val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)

            val data = getPlayerData(player)
            if (data!!.canCharge) data.tick = 0
            if (data.weaponClass!!.mainWeapon!!.weaponType != "Shooter" && data.weaponClass!!.mainWeapon!!.weaponType != "Blaster") {
                data.isHolding =
                    true
            }
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Blaster") shootBlaster(player)
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Burst") burstCooltime(player)
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Roller") {
                if (data.weaponClass!!.mainWeapon!!.isHude) {
                    if (data.canShoot || clickType == ClickType.RENDA) {
                        data.canShoot = false
                        Brush.shootPaintRunnable(player)
                    }
                } else {
                    if (data.canShoot) {
                        data.canShoot = false
                        Roller.shootPaintRunnable(player)
                    }
                }
            }
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Bucket") shootBucket(player)
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Slosher") shootSlosher(player)
            if (data.weaponClass!!.mainWeapon!!.weaponType == "Kasa" ||
                data.weaponClass!!.mainWeapon!!.weaponType == "Camping"
            ) {
                shootKasa(player)
            }
        }
    }
}
