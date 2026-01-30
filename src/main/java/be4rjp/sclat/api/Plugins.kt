package be4rjp.sclat.api

import org.bukkit.Bukkit
import org.slf4j.Logger
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

    private var _isLoaded: Boolean? = null

    val isLoaded: Boolean
        get() {
            if (_isLoaded == null) {
                _isLoaded = Bukkit.getPluginManager().isPluginEnabled(pluginName)
            }
            return _isLoaded!!
        }

    /**
     * To support plugman load.
     */
    private fun resetLoadedState() {
        _isLoaded = null
    }

    companion object {
        /**
         * Initialize check
         *
         * @return is init-check succeeded
         */
        @JvmStatic
        fun onInit(logger: Logger): Boolean {
            val missingPlugins = ArrayList<String?>()
            for (plugin in entries) {
                plugin.resetLoadedState()
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
