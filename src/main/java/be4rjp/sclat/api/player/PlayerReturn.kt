package be4rjp.sclat.api.player

import be4rjp.sclat.Sclat
import org.bukkit.scheduler.BukkitRunnable

class PlayerReturn(val uUID: String?) {
    private val task: BukkitRunnable
    var flag: Boolean = true
        private set

    init {
        this.task = object : BukkitRunnable() {
            override fun run() {
                flag = false
            }
        }
        this.task.runTaskLater(Sclat.getPlugin(), 400)
    }
}
