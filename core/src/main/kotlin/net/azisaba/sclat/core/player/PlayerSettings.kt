package net.azisaba.sclat.core.player

import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
class PlayerSettings(
    val player: Player?,
) {
    private var showEffectMainWeaponInk = true
    private var showEffectChargerLine = true
    private var showEffectSPWeapon = true
    private var showEffectSPWeaponRegion = true
    private var showSnowBall = true
    private var showEffectSquid = true
    private var showEffectBombEx = true
    private var showEffectBomb = true
    private var playBGM = true
    private var doChargeKeep = true

    fun showEffectMainWeaponInk(): Boolean = this.showEffectMainWeaponInk

    fun showEffectChargerLine(): Boolean = this.showEffectChargerLine

    fun showEffectSPWeapon(): Boolean = this.showEffectSPWeapon

    fun showEffectSPWeaponRegion(): Boolean = this.showEffectSPWeaponRegion

    fun showSnowBall(): Boolean = this.showSnowBall

    fun showEffectSquid(): Boolean = this.showEffectSquid

    fun showEffectBombEx(): Boolean = this.showEffectBombEx

    fun showEffectBomb(): Boolean = this.showEffectBomb

    fun playBGM(): Boolean = this.playBGM

    fun doChargeKeep(): Boolean = this.doChargeKeep

    fun sShowEffectMainWeaponInk() {
        this.showEffectMainWeaponInk = !showEffectMainWeaponInk
    }

    fun sShowEffectChargerLine() {
        this.showEffectChargerLine = !showEffectChargerLine
    }

    fun sShowEffectSPWeapon() {
        this.showEffectSPWeapon = !showEffectSPWeapon
    }

    fun sShowEffectSPWeaponRegion() {
        this.showEffectSPWeaponRegion = !showEffectSPWeaponRegion
    }

    fun sShowSnowBall() {
        this.showSnowBall = !showSnowBall
    }

    fun sShowEffectSquid() {
        this.showEffectSquid = !showEffectSquid
    }

    fun sShowEffectBombEx() {
        this.showEffectBombEx = !showEffectBombEx
    }

    fun sShowEffectBomb() {
        this.showEffectBomb = !showEffectBomb
    }

    fun sPlayBGM() {
        this.playBGM = !playBGM
    }

    fun sDoChargeKeep() {
        this.doChargeKeep = !doChargeKeep
    }
}
