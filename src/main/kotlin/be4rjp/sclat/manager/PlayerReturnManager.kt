package be4rjp.sclat.manager

import be4rjp.sclat.api.player.PlayerReturn
import be4rjp.sclat.extension.bukkitTask
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger

object PlayerReturnManager {
    var list: MutableList<PlayerReturn> = ArrayList()

    fun isReturned(uuid: String?): Boolean = list.removeIf { pr -> pr.uUID == uuid }

    fun runRemoveTask() {
        bukkitTask {
            try {
                list.removeIf { pr: PlayerReturn? -> !pr!!.flag }
            } catch (e: Exception) {
                sclatLogger.error("An error occurred in removing player return", e)
            }
        }.runTaskTimer(plugin, 0, 200)
    }

    fun addPlayerReturn(uuid: String?) {
        list.add(PlayerReturn(uuid))
    }
}
