package be4rjp.sclat.schedular

import org.bukkit.scheduler.BukkitRunnable
import java.util.function.BiConsumer

fun everyTick(tickFunc: BiConsumer<Int, Runnable>) =
    object : BukkitRunnable() {
        var currentTicks = 0

        override fun run() {
            tickFunc.accept(currentTicks, ::cancel)
            currentTicks++
        }
    }
