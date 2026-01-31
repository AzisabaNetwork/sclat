package be4rjp.sclat.data

import org.bukkit.inventory.ItemStack

/**
 *
 * @author Be4rJP
 */
class MainWeapon(
    private val WeaponName: String?,
) {
    var weaponType: String? = null
    var weaponIteamStack: ItemStack? = null
        private set

    @JvmField
    var random: Double = 0.0

    @JvmField
    var maxRandom: Double = 0.0
    var distanceTick: Int = 0
    var shootSpeed: Double = 0.0
    var shootTick: Int = 0
    var paintRandom: Int = 0
    var maxPaintDis: Double = 0.0
    var needInk: Float = 0f

    @JvmField
    var damage: Double = 0.0
    var maxCharge: Int = 0
    var rollerShootQuantity: Int = 0
    var usingWalkSpeed: Float = 0f

    @JvmField
    var rollerWidth: Int = 0
    var canTatehuri: Boolean = false
    var rollerDamage: Double = 0.0
    var rollerNeedInk: Float = 0f
    var scope: Boolean = false
        set(v) {
            field = v
        }
    var blasterExHankei: Double = 0.0

    @JvmField
    var delay: Int = 0
    var coolTime: Int = 0
    var blasterExDamage: Double = 0.0

    @JvmField
    var isHude: Boolean = false
    var hudeRandom: Double = 0.0

    @JvmField
    var money: Int = 0

    @JvmField
    var isManeuver: Boolean = false
    var slidingShootTick: Int = 0
    var chargeRatio: Double = 1.0
    var inHoldSpeed: Float = 0.2f
    var canChargeKeep: Boolean = false
        set(v) {
            field = v
        }

    @JvmField
    var chargeKeepingTime: Int = 0
    var hanbunCharge: Boolean = false
        set(v) {
            field = `v`
        }
    var sPRate: Double = 1.0

    @JvmField
    var maxRandomCount: Int = 1

    @JvmField
    var level: Int = 0

    @JvmField
    var slideNeedINK: Float = 0.2f

    // チャージャーの非適性射程でのダメージ減少率の設定
    // チャージャーの非適性射程でのダメージ減少率の取得
    var decreaseRate: Double = 1.0 // チャージャーの非適性射程でのダメージ減少率

    // チャージャーの非適性射程の設定
    // チャージャーの非適性射程の取得
    var appDistance: Int = 0 // 非適性射程の距離

    // ガチャ武器かどうかを返す
    // ガチャ武器かどうかを返す
    @JvmField
    var islootbox: Boolean = false // ガチャ武器かどうか

    // 排出率を返す
    // 排出率を返す
    @JvmField
    var lootpro: Double = 0.0 // ガチャ排出率

    // スワッパー
    @JvmField
    var swap: String? = ""
    private var isswaper = false

    fun getIsSwap(): Boolean = isswaper

    fun setWeaponItemStack(`is`: ItemStack?) {
        this.weaponIteamStack = `is`
    }

    fun setIsSwap(Isswap: Boolean) {
        this.isswaper = Isswap
    }
}
