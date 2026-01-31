package be4rjp.sclat.api.player

import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
class PlayerSettings(val player: Player?) {
    private var ShowEffect_MainWeaponInk = true
    private var ShowEffect_ChargerLine = true
    private var ShowEffect_SPWeapon = true
    private var ShowEffect_SPWeaponRegion = true
    private var ShowSnowBall = true
    private var ShowEffect_Squid = true
    private var ShowEffect_BombEx = true
    private var ShowEffect_Bomb = true
    private var PlayBGM = true
    private var doChargeKeep = true

    fun ShowEffect_MainWeaponInk(): Boolean {
        return this.ShowEffect_MainWeaponInk
    }

    fun ShowEffect_ChargerLine(): Boolean {
        return this.ShowEffect_ChargerLine
    }

    fun ShowEffect_SPWeapon(): Boolean {
        return this.ShowEffect_SPWeapon
    }

    fun ShowEffect_SPWeaponRegion(): Boolean {
        return this.ShowEffect_SPWeaponRegion
    }

    fun ShowSnowBall(): Boolean {
        return this.ShowSnowBall
    }

    fun ShowEffect_Squid(): Boolean {
        return this.ShowEffect_Squid
    }

    fun ShowEffect_BombEx(): Boolean {
        return this.ShowEffect_BombEx
    }

    fun ShowEffect_Bomb(): Boolean {
        return this.ShowEffect_Bomb
    }

    fun PlayBGM(): Boolean {
        return this.PlayBGM
    }

    fun doChargeKeep(): Boolean {
        return this.doChargeKeep
    }

    fun S_ShowEffect_MainWeaponInk() {
        this.ShowEffect_MainWeaponInk = !ShowEffect_MainWeaponInk
    }

    fun S_ShowEffect_ChargerLine() {
        this.ShowEffect_ChargerLine = !ShowEffect_ChargerLine
    }

    fun S_ShowEffect_SPWeapon() {
        this.ShowEffect_SPWeapon = !ShowEffect_SPWeapon
    }

    fun S_ShowEffect_SPWeaponRegion() {
        this.ShowEffect_SPWeaponRegion = !ShowEffect_SPWeaponRegion
    }

    fun S_ShowSnowBall() {
        this.ShowSnowBall = !ShowSnowBall
    }

    fun S_ShowEffect_Squid() {
        this.ShowEffect_Squid = !ShowEffect_Squid
    }

    fun S_ShowEffect_BombEx() {
        this.ShowEffect_BombEx = !ShowEffect_BombEx
    }

    fun S_ShowEffect_Bomb() {
        this.ShowEffect_Bomb = !ShowEffect_Bomb
    }

    fun S_PlayBGM() {
        this.PlayBGM = !PlayBGM
    }

    fun S_doChargeKeep() {
        this.doChargeKeep = !doChargeKeep
    }
}
