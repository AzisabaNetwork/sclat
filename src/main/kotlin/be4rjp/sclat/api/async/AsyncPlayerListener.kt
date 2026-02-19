package be4rjp.sclat.api.async

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object AsyncPlayerListener : Listener {
    @EventHandler
    fun online(event: PlayerJoinEvent) {
        val player = event.getPlayer()
        AsyncThreadManager.onlinePlayers.add(player)
    }

    @EventHandler
    fun offline(event: PlayerQuitEvent) {
        val player = event.getPlayer()
        AsyncThreadManager.onlinePlayers.remove(player)
    }
}
