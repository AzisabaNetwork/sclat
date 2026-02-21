package be4rjp.sclat.extension

import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

fun bukkitTask(func: Consumer<BukkitRunnable>) =
    object : BukkitRunnable() {
        override fun run() {
            func.accept(this)
        }
    }
