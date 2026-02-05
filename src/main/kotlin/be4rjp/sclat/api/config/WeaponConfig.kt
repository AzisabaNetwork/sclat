package be4rjp.sclat.api.config

import be4rjp.sclat.api.data.WeaponData
import kotlinx.serialization.Serializable

@Serializable
data class WeaponConfig(
    var mainWeapon: MutableMap<String, WeaponData>,
)
