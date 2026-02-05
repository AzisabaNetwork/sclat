package be4rjp.sclat.manager

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.setMainWeapon
import be4rjp.sclat.data.MainWeapon
import be4rjp.sclat.extension.toMaterial
import be4rjp.sclat.sclatLogger
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
        val mainWeaponSection =
            Sclat.conf?.weaponConfig?.getConfigurationSection("MainWeapon") ?: run {
                sclatLogger.error("Failed to get MainWeapon section from mainweapon.yml")
                return
            }
        for (weaponname in mainWeaponSection.getKeys(false)) {
            val section =
                mainWeaponSection.getConfigurationSection(weaponname) ?: run {
                    sclatLogger.warn("Failed to get section for $weaponname")
                    continue
                }

            val weaponType = section.getString("WeaponType")
            val weaponMaterial: Material? = section.getString("WeaponMaterial")?.toMaterial()
            val random = section.getDouble("ShootRandom")
            val distick = section.getInt("ShootDistance")
            val shootspeed = section.getDouble("ShootSpeed")
            val shoottick = section.getInt("ShootTick")
            val paintrandom = section.getInt("PaintRandom")
            val maxpaintdis = section.getInt("MaxPaintDistance").toDouble()
            val needink = section.getDouble("NeedInk").toFloat()
            val damage = section.getDouble("Damage")
            val maxcharge = section.getInt("MaxCharge")
            val rollershootQuantity = section.getInt("RollerShootQuantity")
            val usinwalkspeed = section.getDouble("UsingWalkSpeed").toFloat()
            val rollerWidth = section.getInt("RollerWidth")
            val tatehuri = section.getBoolean("RollerTatehuri")
            val rollerdamage = section.getDouble("RollerDamage")
            val rollerneedink = section.getDouble("RollerNeedInk").toFloat()
            val exh = section.getInt("BlasterExHankei", 0).toDouble()
            val delay = section.getInt("Delay")
            val cooltime = section.getInt("Cooltime")
            val exd = section.getDouble("BlasterExDamage")
            val hude = section.getBoolean("IsBrush")
            val huder = section.getDouble("BrushRandom")
            val man = section.getBoolean("IsManeuver", false)
            val slST = section.getInt("SlidingShootTick", 1)
            val cr = section.getDouble("ChargeRatio", 1.0)
            val ck = section.getBoolean("CanChargeKeep", false)
            val ckt = section.getInt("ChargeKeepingTime", 0)
            val hc = section.getBoolean("HanbunChargeKeep", false)
            val sn = section.getDouble("SlideNeedInk", 0.2).toFloat()

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

            if (section.contains("SPRate")) {
                mw.sPRate = section.getDouble("SPRate")
            }

            if (section.contains("MaxRandom")) {
                mw.maxRandom =
                    (
                        section.getDouble("MaxRandom")
                    )
            }

            if (section.contains("MaxRandomCount")) {
                mw.maxRandomCount =
                    (
                        section.getInt("MaxRandomCount")
                    )
            }

            if (section.contains("Scope")) {
                mw.scope = section.getBoolean("Scope")
            }

            if (section.contains("DecreaseRate")) {
                // チャージャーの非適性ダメージ減少率
                mw.decreaseRate = section.getDouble("DecreaseRate")
            }

            if (section.contains("AppDistance")) {
                // チャージャーの非適性距離
                mw.appDistance = section.getInt("AppDistance")
            }

            if (section.contains("Money")) {
                mw.money =
                    (
                        section.getInt("Money")
                    )
            } else {
                mw.money = (0)
            }

            if (section.contains("Level")) {
                mw.level =
                    (
                        section.getInt("Level")
                    )
            } else {
                mw.level = (0)
            }

            if (section.contains("InHoldWalkSpeed")) {
                mw.inHoldSpeed =
                    section
                        .getDouble("InHoldWalkSpeed")
                        .toFloat()
            } else {
                mw.inHoldSpeed =
                    Sclat.conf!!
                        .config!!
                        .getDouble("PlayerWalkSpeed")
                        .toFloat()
            }

            if (section.contains("IsLootBox")) { // ガチャ武器用
                mw.islootbox =
                    (
                        section.getBoolean("IsLootBox")
                    )
            }
            if (section.contains("LootPro")) { // ガチャ武器用排出率
                mw.lootpro = (
                    section.getDouble("LootPro")
                )
            }
            if (section.contains("IsSwapper")) { // スワッパ―判別
                mw.setIsSwap(
                    section.getBoolean("IsSwapper"),
                )
            }
            if (section.contains("SwapWeapon")) { // スワッパ―スワップ先
                mw.swap = (
                    section.getString("SwapWeapon")
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
            if (itemname.length >= wname.length) return wname == itemname.take(wname.length)
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
