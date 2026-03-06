package be4rjp.sclat.api.async

import be4rjp.sclat.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object AsyncThreadManager {
    private val tickThreads: MutableList<AsyncTickThread> = CopyOnWriteArrayList<AsyncTickThread>()

    val randomTickThread: AsyncTickThread
        get() = tickThreads.random()

    @JvmStatic
    fun setup(numberOfThread: Int) {
        repeat(numberOfThread) {
            tickThreads.add(AsyncTickThread())
        }
    }

    @JvmStatic
    fun shutdownAll() {
        for (thread in tickThreads) {
            thread.shutdown()
        }
    }

    @JvmField
    var onlinePlayers: MutableSet<Player?> = ConcurrentHashMap.newKeySet()

    @Deprecated("this method isn't used anymore, and will be removed in the future.")
    fun toOnline(player: Player?) {
        onlinePlayers.add(player)
    }

    @Deprecated("this method isn't used anymore, and will be removed in the future.")
    fun toOffline(player: Player?) {
        onlinePlayers.add(player)
    }

    @JvmStatic
    fun sync(runnable: Runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable)
    }
}
