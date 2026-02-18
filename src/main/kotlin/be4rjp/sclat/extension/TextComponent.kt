package be4rjp.sclat.extension

import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

fun component(
    message: String,
    color: TextColor? = null,
) = Component.text(message).let { if (color != null) it.color(color) else it }

fun TextComponent.serializeString() = BukkitComponentSerializer.legacy().serialize(this)
