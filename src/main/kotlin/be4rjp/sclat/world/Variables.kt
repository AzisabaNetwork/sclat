package be4rjp.sclat.world

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.plugin.java.JavaPlugin

internal val multiverseCore
    get() = JavaPlugin.getPlugin(MultiverseCore::class.java)
