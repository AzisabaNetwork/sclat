package be4rjp.sclat.api.async

import be4rjp.sclat.plugin
import org.bukkit.scheduler.BukkitRunnable

@Deprecated("Use mccoroutines instead of this class.")
abstract class AsyncTask : Runnable {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        this.isCanceled = true
    }

    fun runTask() {
        AsyncThreadManager.randomTickThread.runTask(this)
    }

    fun runTaskLater(delay: Long) {
        val runnable = this
        object : BukkitRunnable() {
            override fun run() {
                AsyncThreadManager.randomTickThread.runTask(runnable)
            }
        }.runTaskLater(plugin, delay)
    }

    fun runTaskTimer(
        delay: Long,
        period: Long,
    ) {
        val runnable = this
        val thread = AsyncThreadManager.randomTickThread
        object : BukkitRunnable() {
            override fun run() {
                if (runnable.isCanceled) {
                    cancel()
                    return
                }
                thread.runTask(runnable)
            }
        }.runTaskTimer(plugin, delay, period)
    }
}
