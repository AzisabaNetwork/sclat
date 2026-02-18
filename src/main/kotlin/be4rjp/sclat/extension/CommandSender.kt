package be4rjp.sclat.extension

import be4rjp.sclat.adventure
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender

fun CommandSender.sendMessage(component: ComponentLike) = adventure.sender(this).sendMessage(component)
