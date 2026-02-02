package be4rjp.sclat.api.config

import be4rjp.sclat.sclatLogger
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class CustomConfig(
    private val plugin: Plugin,
    private val file: String,
) {
    private var config: FileConfiguration? = null
    private val configFile: File

    init {
        configFile = File(plugin.dataFolder, file)
    }

    fun saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(file, false)
        }
    }

    fun getConfig(): FileConfiguration? {
        if (config == null) {
            reloadConfig()
        }
        return config
    }

    fun saveConfig() {
        if (config == null) return
        try {
            getConfig()!!.save(configFile)
        } catch (ex: IOException) {
            sclatLogger.error("Could not save config to " + configFile, ex)
        }
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)

        val defConfigStream = plugin.getResource(file)
        if (defConfigStream == null) {
            return
        }

        config!!.setDefaults(
            YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream, StandardCharsets.UTF_8)),
        )
    }
}
