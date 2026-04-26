package net.azisaba.sclat.core

import org.bukkit.plugin.PluginManager
import java.util.function.Consumer

enum class Plugins(
    val pluginName: String,
    val isRequired: Boolean = true,
) {
    DADADACHECKER("DADADAChecker"),
    LUNACHAT(
        "LunaChat",
        false,
    ),
    NOTEBLOCKAPI("NoteBlockAPI"),
    PROTOCOLLIB("ProtocolLib"),
    ;

    var isLoaded: Boolean = false
        private set

    companion object {
        private val logger by DelegatedLogger()

        /**
         * Initialize check
         *
         * @return is init-check succeeded
         */
        @JvmStatic
        fun onInit(pluginManager: PluginManager): Boolean {
            val missingPlugins = ArrayList<String>()
            for (plugin in entries) {
                plugin.isLoaded = pluginManager.isPluginEnabled(plugin.pluginName)
                if (!plugin.isLoaded && plugin.isRequired) {
                    missingPlugins.add(plugin.pluginName)
                }
            }

            // If some required plugins are missing
            if (!missingPlugins.isEmpty()) {
                logger.error("Some plugins are missing. Please install or enable.")
                logger.error("*** Missing required plugins ***")
                missingPlugins.forEach(Consumer { p: String? -> logger.error("- {}", p) })
                logger.error("********************************")
                return false
            }

            return true
        }
    }
}
