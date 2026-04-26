package be4rjp.sclat.manager

import be4rjp.sclat.api.player.PlayerReturn
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.time.delay
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.util.UUID

object PlayerReturnManager {
    private val playerReturnMap: MutableMap<UUID, PlayerReturn> = mutableMapOf()

    fun isReturned(uuid: UUID): Boolean = playerReturnMap.remove(uuid)?.flag ?: false

    fun runRemoveTask(plugin: JavaPlugin) {
        plugin.launch {
            delay(Duration.ofSeconds(2))
            playerReturnMap.entries.removeIf { (_, pr) -> pr.flag }
        }
    }

    fun addPlayerReturn(uuid: UUID) {
        playerReturnMap[uuid] = PlayerReturn(uuid.toString())
    }
}
