package be4rjp.sclat.api.player

import be4rjp.sclat.data.Match
import be4rjp.sclat.data.WeaponClass
import net.azisaba.sclat.core.data.MainWeapon
import net.azisaba.sclat.core.player.PlayerSettings
import net.azisaba.sclat.core.team.SclatTeam
import net.minecraft.server.v1_14_R1.ItemStack
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
class PlayerData(
    var player: Player?,
) {
    @JvmField
    var settings: PlayerSettings? = null
    var classname: String? = null

    @JvmField
    var match: Match? = null

    @JvmField
    var team: SclatTeam? = null

    @JvmField
    var isInMatch: Boolean = false

    @JvmField
    var weaponClass: WeaponClass? = null

    @JvmField
    var mainweapon: MainWeapon? = null

    // public void setMainWeapon(MainWeapon mainweapon){this.mainweapon =
    // mainweapon;}
    // public MainWeapon getMainWeapon(){return mainweapon;}
    @JvmField
    var matchLocation: Location? = null

    @JvmField
    var playerNumber: Int = 0

    @JvmField
    var canShoot: Boolean = true

    @JvmField
    var tick: Int = 0

    @JvmField
    var isSquid: Boolean = false

    @JvmField
    var isOnInk: Boolean = false

    @JvmField
    var isHolding: Boolean = false

    @JvmField
    var isJoined: Boolean = false

    @JvmField
    var canPaint: Boolean = false

    @JvmField
    var killCount: Int = 0

    @JvmField
    var paintCount: Int = 0

    @JvmField
    var armor: Double = 0.0

    @JvmField
    var sPGauge: Int = 0

    @JvmField
    var isBombRush: Boolean = false

    @JvmField
    var isSP: Boolean = false

    @JvmField
    var canUseSubWeapon: Boolean = true

    @JvmField
    var isCharging: Boolean = false

    @JvmField
    var poison: Boolean = false

    @JvmField
    var canRollerShoot: Boolean = true

    @JvmField
    var playerMapLoc: Location? = null

    @JvmField
    var isUsingSP: Boolean = false

    @JvmField
    var canCharge: Boolean = true

    @JvmField
    var isDead: Boolean = false

    @JvmField
    var servername: String? = ""

    @JvmField
    var playerHead: ItemStack? = null

    @JvmField
    var isOnPath: Boolean = false

    @JvmField
    var isUsingMM: Boolean = false

    @JvmField
    var isPoisonCoolTime: Boolean = false

    @JvmField
    var isSneaking: Boolean = false

    @JvmField
    var isUsingManeuver: Boolean = false

    @JvmField
    var isSliding: Boolean = false

    @JvmField
    var gearNumber: Int = 0

    @JvmField
    var isUsingJetPack: Boolean = false

    @JvmField
    var doChargeKeep: Boolean = false

    @JvmField
    var mainItemGlow: Boolean = false

    @JvmField
    var isUsingAmehurashi: Boolean = false

    @JvmField
    var isUsingTyakuti: Boolean = false

    @JvmField
    var isJumping: Boolean = false

    @JvmField
    var canFly: Boolean = false

    @JvmField
    var trapCount: Int = 0

    @JvmField
    var playerGroundLocation: Location? = null
    var isUsingSS: Boolean = false

    var vehicleVector: Vector? = Vector(0, 0, 0)

    @JvmField
    var speed: Double = 0.0
    var fov: Float = 0.1f
    var armorlist: ArrayList<ArmorStand?> = ArrayList()

    @JvmField
    var lastAttack: Player? = player
    var stoprun: Boolean = false

    fun getArmorlist(n: Int): ArmorStand? = this.armorlist[n]

    fun setServerName(server: String?) {
        this.servername = server
    }

    fun setArmorlist(n: ArmorStand?) {
        this.armorlist.add(n)
    }

    fun subArmorlist(n: ArmorStand?) {
        this.armorlist.remove(n)
    }

    fun reflectionDoChargeKeep() {
        this.doChargeKeep = !this.doChargeKeep
    }

    fun addKillCount() {
        this.killCount++
    }

    fun addPaintCount() {
        this.paintCount++
    }

    fun addSPGauge() {
        this.sPGauge++
    }

    fun resetSPGauge() {
        this.sPGauge = 0
    }

    fun addTrapCount() {
        this.trapCount++
    }

    fun reset() {
        this.isInMatch = false
        this.playerNumber = 0
        this.tick = 0
        this.isInMatch = false
        this.isOnInk = false
        this.isHolding = false
        this.isJoined = false
        this.canPaint = false
        this.canShoot = true
        this.killCount = 0
        this.paintCount = 0
        this.armor = 0.0
        this.sPGauge = 0
        this.isSP = false
        this.canUseSubWeapon = true
        this.isCharging = false
        this.poison = false
        this.canRollerShoot = true
        this.canCharge = true
        this.isDead = false
        this.isOnPath = false
        this.isSneaking = false
        this.isUsingManeuver = false
        this.isSliding = false
        this.isUsingJetPack = false
        this.mainItemGlow = false
        this.isUsingAmehurashi = false
        this.isUsingTyakuti = false
        this.trapCount = 0
        this.isUsingSS = false
        this.vehicleVector = Vector(0, 0, 0)
        this.isJumping = false
        this.canFly = false
        this.speed = 0.0
        this.fov = 0.1f
        this.armorlist = ArrayList()
        this.stoprun = false
    }
}
