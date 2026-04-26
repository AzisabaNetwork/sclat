package be4rjp.sclat.api.async

import be4rjp.sclat.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Deprecated("Use mccoroutine")
object AsyncThreadManager {
    private val tickThreads: MutableList<AsyncTickThread> = CopyOnWriteArrayList()

    val randomTickThread: AsyncTickThread
        get() = tickThreads[Random().nextInt(tickThreads.size)]

    @JvmStatic
    fun setup(numberOfThread: Int) {
        for (i in 0..<numberOfThread) {
            val thread = AsyncTickThread()
            tickThreads.add(thread)
        }
    }

    @JvmStatic
    fun shutdownAll() {
        for (thread in tickThreads) {
            thread.shutdown()
        }
    }

    @JvmField
    var onlinePlayers: MutableSet<Player?> = ConcurrentHashMap.newKeySet<Player?>()

    @JvmStatic
    fun sync(runnable: Runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable)
    }
}
