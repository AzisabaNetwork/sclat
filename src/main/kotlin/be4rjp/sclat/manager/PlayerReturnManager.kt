package be4rjp.sclat.manager

import be4rjp.sclat.api.player.PlayerReturn
import be4rjp.sclat.plugin
import org.bukkit.scheduler.BukkitRunnable

object PlayerReturnManager {
    var list: MutableList<PlayerReturn> = ArrayList<PlayerReturn>()

    fun isReturned(uuid: String?): Boolean {
        for (pr in list) {
            if (pr.uUID == uuid) {
                list.remove(pr)
                return true
            }
        }
        return false
    }

    fun runRemoveTask() {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        list.removeIf { pr: PlayerReturn? -> !pr!!.flag }
                    } catch (e: Exception) {
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 200)
    }

    fun addPlayerReturn(uuid: String?) {
        val pr = PlayerReturn(uuid)
        list.add(pr)
    }
}
