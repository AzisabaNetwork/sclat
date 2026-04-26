package net.azisaba.sclat.core.weapon

// Todo: impl
interface Weapon {
    fun onInit() {}

    fun onDestroy() {}

    fun onClick() {}

    fun onTick() {}

    fun onHeldTick() {}

    fun onHeld() {}

    fun onUnHeld() {}
}
