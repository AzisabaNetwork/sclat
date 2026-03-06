package be4rjp.sclat.map

import be4rjp.sclat.match.MatchEndEvent
import be4rjp.sclat.match.MatchStartEvent
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

object MapEventListener : Listener {
    fun onServerStart(event: ServerLoadEvent) {
        // todo: load 2 maps
    }

    fun onMatchStart(event: MatchStartEvent) {
        // map mark as using (for safety)
    }

    fun onMatchEnd(event: MatchEndEvent) {
        // unload map without saving and mark as not using
        // load different map for future match
    }
}
