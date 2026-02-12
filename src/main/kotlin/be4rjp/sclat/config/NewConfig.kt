package be4rjp.sclat.config

import be4rjp.sclat.extension.loadToml
import be4rjp.sclat.extension.saveToml
import be4rjp.sclat.sclatLogger
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
        sclatLogger.info("<<< All config loaded.")
    }

    fun save() {
        sclatLogger.info(">>> Saving config...")

        sclatLogger.info("<<< All config saved.")
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
