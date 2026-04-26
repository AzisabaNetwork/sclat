package net.azisaba.sclat.core.config

import net.azisaba.sclat.core.DelegatedLogger
import net.azisaba.sclat.core.extension.loadToml
import net.azisaba.sclat.core.extension.saveToml
import net.azisaba.sclat.core.utils.DailyRefreshSet
import java.io.File
import java.util.function.Supplier

/**
 * Migrating from Config.kt
 */
object NewConfig {
    private val logger by DelegatedLogger()
    private val parent = File("plugins/Sclat")
    lateinit var loginBonusReward: LoginBonusRewardConfig
        private set

    lateinit var loginBonusRefreshSet: DailyRefreshSet
        private set

    fun load() {
        logger.info(">>> Loading config...")
        loginBonusReward = loadTomlConfig("login_bonus", ::LoginBonusRewardConfig)
        loginBonusRefreshSet = loadTomlConfig("login_bonus_claimed", ::DailyRefreshSet)
        logger.info("<<< All config loaded.")
    }

    fun save() {
        logger.info(">>> Saving config...")
        saveTomlConfig("login_bonus_claimed", loginBonusRefreshSet)
        logger.info("<<< All config saved.")
    }

    private inline fun <reified T> saveTomlConfig(
        name: String,
        value: T,
    ) {
        parent.resolve("$name.toml").let { file ->
            saveToml(file, value)
            logger.info("-> Config ${file.name} saved!")
        }
    }

    private inline fun <reified T> loadTomlConfig(
        name: String,
        default: Supplier<T>,
    ): T =
        parent.resolve("$name.toml").let { file ->
            if (!file.exists()) {
                saveToml(file, default.get()).also {
                    logger.warn("${file.name} was missing. Wrote default config.")
                }
            }
            loadToml<T>(file).also {
                logger.info("-> Config ${file.name} loaded!")
            }
        }
}
