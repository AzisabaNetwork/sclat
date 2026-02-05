package be4rjp.sclat.extension

import org.bukkit.Material

fun String.toMaterial(): Material? = Material.getMaterial(this)
