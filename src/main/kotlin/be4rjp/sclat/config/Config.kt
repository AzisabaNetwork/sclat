package be4rjp.sclat.config

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

/**
 *
 * @author Be4rJP
 */
class Config {
    var classConfig: FileConfiguration? = null
        private set

    @JvmField
    var config: FileConfiguration? = null
    var weaponConfig: FileConfiguration? = null
        private set
    var mapConfig: FileConfiguration? = null
        private set
    private var playersettings: FileConfiguration? = null
    var armorStandSettings: FileConfiguration? = null
        private set
    private var s: FileConfiguration? = null
    var servers: FileConfiguration? = null
        private set
    private var idCash: FileConfiguration? = null
    var emblemItems: FileConfiguration? = null
        private set
    var emblemUserdata: FileConfiguration? = null
        private set

    private val parent = File("plugins/Sclat")
    private val psf = File(parent, "class.yml")
    private val weaponf = File(parent, "mainnweapon.yml")
    private val mapf = File(parent, "maps.yml")
    private val conff = File(parent, "config.yml")
    private val playersettingsF = File(parent, "settings.yml")
    private val asf = File(parent, "armorstand.yml")
    private val sf = File(parent, "status.yml")
    private val serverFile = File(parent, "servers.yml")
    private val idCashFile = File(parent, "UUIDCash.yml")
    var emblemsFile: File = File(parent, "emblems.yml")
    private val emblemItemsFile = File(parent, "emblem_items.yml")
    private val emblemUserDataFile = File(parent, "emblem_userdata.yml")

    @Synchronized
    fun loadConfig() {
        this.classConfig = YamlConfiguration.loadConfiguration(psf)
        this.config = YamlConfiguration.loadConfiguration(conff)
        this.weaponConfig = YamlConfiguration.loadConfiguration(weaponf)
        this.mapConfig = YamlConfiguration.loadConfiguration(mapf)
        playersettings = YamlConfiguration.loadConfiguration(playersettingsF)
        this.armorStandSettings = YamlConfiguration.loadConfiguration(asf)
        s = YamlConfiguration.loadConfiguration(sf)
        servers = YamlConfiguration.loadConfiguration(serverFile)
        idCash = YamlConfiguration.loadConfiguration(idCashFile)
        tryCreateFile(emblemItemsFile)
        tryCreateFile(emblemUserDataFile)
        loadEmblemUserData()
        loadEmblemLoreData()
    }

    @Synchronized
    fun loadEmblemUserData() {
        emblemUserdata = YamlConfiguration.loadConfiguration(emblemUserDataFile)
    }

    @Synchronized
    fun loadEmblemLoreData() {
        emblemItems = YamlConfiguration.loadConfiguration(emblemItemsFile)
    }

    private fun tryCreateFile(targetFile: File) {
        try {
            if (!targetFile.exists()) targetFile.createNewFile()
        } catch (e: IOException) {
            Bukkit.getLogger().warning("Failed to create file: " + e)
        }
    }

    @Synchronized
    fun saveConfig() {
        try {
            playersettings!!.save(playersettingsF)
            s!!.save(sf)
            idCash!!.save(idCashFile)
            saveEmblemUserdata()
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to save config files!")
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun saveEmblemUserdata() {
        emblemUserdata!!.save(emblemUserDataFile)
    }

    val playerSettings: FileConfiguration
        get() = playersettings!!

    val playerStatus: FileConfiguration
        get() = s!!

    val uUIDCash: FileConfiguration
        get() = idCash!!

    companion object
}
