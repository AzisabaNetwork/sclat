package net.azisaba.sclat.core.config

import kotlinx.serialization.Serializable

@Serializable
data class PlayerStatus(
    var money: Int,
    var level: Int,
    var rank: Int,
    var weaponClass: MutableList<String>,
    var gearList: MutableList<Int>,
    var gear: Int,
    var kill: Int,
    var paint: Int,
    var equipClass: String,
    var tutorial: Int = 0,
    var ticket: Int = 0, // For gacha
) {
    companion object {
        fun default(defaultWeaponClass: String): PlayerStatus =
            PlayerStatus(
                money = 10000,
                level = 0,
                rank = 0,
                gear = 0,
                kill = 0,
                paint = 0,
                tutorial = 0,
                ticket = 0,
                gearList = mutableListOf(0),
                equipClass = defaultWeaponClass,
                weaponClass = mutableListOf(defaultWeaponClass),
            )
    }
}
