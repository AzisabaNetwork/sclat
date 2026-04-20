package be4rjp.sclat.config

import be4rjp.sclat.loginbonus.LoginBonus
import be4rjp.sclat.sclatLogger
import net.azisaba.sclat.core.extension.loadToml
import net.azisaba.sclat.core.extension.saveToml
import net.azisaba.sclat.core.utils.DailyRefreshSet
import java.io.File
import java.util.function.Supplier

/**
 * Migrating from Config.kt
 */
object NewConfig {
    private val parent = File("plugins/Sclat")
    lateinit var loginBonusReward: LoginBonusRewardConfig
        private set

    fun load() {
        sclatLogger.info(">>> Loading config...")
        loginBonusReward = loadTomlConfig("login_bonus", ::LoginBonusRewardConfig)
        LoginBonus.refreshSet = loadTomlConfig("login_bonus_claimed", ::DailyRefreshSet)
        sclatLogger.info("<<< All config loaded.")
    }

    fun save() {
        sclatLogger.info(">>> Saving config...")
        saveTomlConfig("login_bonus_claimed", LoginBonus.refreshSet)
        sclatLogger.info("<<< All config saved.")
    }

    private inline fun <reified T> saveTomlConfig(
        name: String,
        value: T,
    ) {
        parent.resolve("$name.toml").let { file ->
            saveToml(file, value)
            sclatLogger.info("-> Config ${file.name} saved!")
        }
    }

    private inline fun <reified T> loadTomlConfig(
        name: String,
        default: Supplier<T>,
    ): T =
        parent.resolve("$name.toml").let { file ->
            if (!file.exists()) {
                saveToml(file, default.get()).also {
                    sclatLogger.warn("${file.name} was missing. Wrote default config.")
                }
            }
            loadToml<T>(file).also {
                sclatLogger.info("-> Config ${file.name} loaded!")
            }
        }
}
