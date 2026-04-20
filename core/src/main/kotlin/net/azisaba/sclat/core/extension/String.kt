package net.azisaba.sclat.core.extension

import org.bukkit.Material

fun String.toMaterial(): Material? = Material.getMaterial(this)
