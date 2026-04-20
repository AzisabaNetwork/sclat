package be4rjp.sclat.data

import net.azisaba.sclat.core.data.MainWeapon

/**
 *
 * @author Be4rJP
 */
class WeaponClass(
    val className: String?,
) {
    @JvmField
    var mainWeapon: MainWeapon? = null
    var subWeaponName: String? = null
    var sPWeaponName: String? = null
}
