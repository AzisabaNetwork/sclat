package be4rjp.sclat.schedular

import be4rjp.sclat.plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.BiConsumer

class TickSchedular {
    private val timingMap: MutableMap<Int, BiConsumer<Int, Runnable>> = mutableMapOf()
    private val ticker =
        object : BukkitRunnable() {
            private var currentTick = 0

            override fun run() {
                timingMap[0]?.accept(currentTick, ::cancel)
                currentTick++
            }
        }

    /**
     * Add action at specific tick
     *
     * @param tick when to run this action
     * @param action consume tick and cancel function
     */
    fun addAction(
        tick: Int,
        action: BiConsumer<Int, Runnable>,
    ): TickSchedular {
        timingMap[tick] = action
        return this
    }

    fun runSync(
        delay: Long = 0L,
        period: Long? = null,
    ) {
        if (period != null) {
            ticker.runTaskTimer(plugin, delay, period)
        } else {
            ticker.runTaskLater(plugin, delay)
        }
    }

    fun runAsync(
        delay: Long = 0L,
        period: Long? = null,
    ) {
        if (period != null) {
            ticker.runTaskTimerAsynchronously(plugin, delay, period)
        } else {
            ticker.runTaskLaterAsynchronously(plugin, delay)
        }
    }
}
