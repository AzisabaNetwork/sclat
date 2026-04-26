package net.azisaba.sclat.core.config

import net.azisaba.sclat.core.DelegatedLogger
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
    private val configFile: File = File(plugin.dataFolder, file)

    fun saveDefaultConfig() {
        if (configFile.exists()) return

        plugin.saveResource(file, false)
        logger.info("デフォルトの設定ファイルを書き込みました。")
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
            logger.error("Could not save config to $configFile", ex)
        }
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)

        val defConfigStream = plugin.getResource(file) ?: return

        config?.setDefaults(
            YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream, StandardCharsets.UTF_8)),
        )
    }

    companion object {
        private val logger by DelegatedLogger()
    }
}
