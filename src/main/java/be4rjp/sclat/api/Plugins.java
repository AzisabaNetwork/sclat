package be4rjp.sclat.api;

import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.ArrayList;

public enum Plugins {
	DADADACHECKER("DADADAChecker"), LUNACHAT("LunaChat",
			false), NOTEBLOCKAPI("NoteBlockAPI"), PROTOCOLLIB("ProtocolLib");

	public final String pluginName;
	public final boolean isRequired;
	private Boolean _isLoaded = null;
	Plugins(String pluginName) {
		this(pluginName, true);
	}
	Plugins(String pluginName, boolean isRequired) {
		this.pluginName = pluginName;
		this.isRequired = isRequired;
	}

	public boolean isLoaded() {
		if (_isLoaded == null) {
			_isLoaded = Bukkit.getPluginManager().isPluginEnabled(pluginName);
		}
		return _isLoaded;
	}

	/**
	 * To support plugman load.
	 */
	private void resetLoadedState() {
		_isLoaded = null;
	}

	/**
	 * Initialize check
	 * 
	 * @return is init-check succeeded
	 */
	public static boolean onInit(Logger logger) {
		ArrayList<String> missingPlugins = new ArrayList<>();
		for (Plugins plugin : Plugins.values()) {
			plugin.resetLoadedState();
			if (!plugin.isLoaded() && plugin.isRequired) {
				missingPlugins.add(plugin.pluginName);
			}
		}

		// If some required plugins are missing
		if (!missingPlugins.isEmpty()) {
			logger.error("Some plugins are missing. Please install or enable.");
			logger.error("*** Missing required plugins ***");
			missingPlugins.forEach(p -> logger.error("- {}", p));
			logger.error("********************************");
			return false;
		}

		return true;
	}
}
