package be4rjp.sclat.extension

import be4rjp.sclat.server
import net.kyori.adventure.text.ComponentLike
import org.bukkit.Server

fun Server.broadcastMessage(component: ComponentLike) = server.broadcastMessage(component.asComponent().serializeString())
