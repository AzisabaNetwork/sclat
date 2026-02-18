package be4rjp.sclat.extension

import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.Component

fun Component.serializeString() = BukkitComponentSerializer.legacy().serialize(this)
