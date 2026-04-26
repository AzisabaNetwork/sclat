package net.azisaba.sclat.core.config

import kotlinx.serialization.Serializable
import net.azisaba.sclat.core.data.WeaponData

@Serializable
data class WeaponConfig(
    var mainWeapon: MutableMap<String, WeaponData>,
)
