package be4rjp.sclat.extension

import org.bukkit.World
import org.bukkit.entity.ArmorStand

val World.armorstands: MutableCollection<ArmorStand>
    get() = this.getEntitiesByClass(ArmorStand::class.java)
