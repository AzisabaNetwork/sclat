package be4rjp.sclat.api.enums

enum class SclatDamageType(
    name: String,
) {
    MAIN_WEAPON("killed"),
    SUB_WEAPON("subWeapon"),
    SP_WEAPON("spWeapon"),
    WATER("water"),
    FALL("fall"),
}
