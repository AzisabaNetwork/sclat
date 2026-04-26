package net.azisaba.sclat.core

import be4rjp.dadadachecker.DADADACheckerAPI
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import net.azisaba.sclat.core.config.SclatConfig
import org.bukkit.plugin.java.JavaPlugin

class Sclat : JavaPlugin() {
    private lateinit var protocolManager: ProtocolManager
    private lateinit var dadadaCheckerApi: DADADACheckerAPI
    private lateinit var config: SclatConfig

    override fun onEnable() {
        slfLogger.info("Welcome to Sclat! (=^･ω･^=)")

        if (!Plugins.onInit(server.pluginManager)) {
            slfLogger.error("✘ Failed to check depend plugins. Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }
        slfLogger.info("✔ Checked depend plugins.")

        protocolManager = ProtocolLibrary.getProtocolManager()
        dadadaCheckerApi = DADADACheckerAPI(this)
        slfLogger.info("✔ Initialized ProtocolLib and DADADA Checker API.")
    }

    override fun onDisable() {
        slfLogger.info("Bye bye! (ﾉ´ヮ`)ﾉ*: ･ﾟ")
    }

    companion object {
        // use slfLogger instead because logger was already taken by JavePlugin
        private val slfLogger by DelegatedLogger()
    }
}
