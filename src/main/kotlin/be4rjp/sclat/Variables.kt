package be4rjp.sclat

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal lateinit var plugin: Sclat

internal val server: Server
    get() = plugin.server

internal val sclatLogger: Logger = LoggerFactory.getLogger("Sclat")

internal val adventure: BukkitAudiences by lazy { BukkitAudiences.create(plugin) }
