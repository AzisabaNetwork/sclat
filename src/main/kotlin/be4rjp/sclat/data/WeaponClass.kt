package be4rjp.sclat.data

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
