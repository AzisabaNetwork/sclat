package be4rjp.sclat.map

import be4rjp.sclat.match.MatchEndEvent
import be4rjp.sclat.match.MatchStartEvent
import be4rjp.sclat.world.BukkitWorldAPI
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

object MapEventListener : Listener {
    var currentMapName: String = ""
    var nextMapName: String = ""

    fun onServerStart(event: ServerLoadEvent) {
        // todo: load 2 maps
        nextMap()
    }

    fun onMatchStart(event: MatchStartEvent) {
        // map mark as using (for safety)
    }

    fun onMatchEnd(event: MatchEndEvent) {
        // unload map without saving and mark as not using
        // load different map for future match
        BukkitWorldAPI.unloadWorld(currentMapName, false) ?: false
        nextMap()
    }

    fun nextMap() {
        currentMapName = nextMapName
        nextMapName = MapSelector.selectRandomMap()
        BukkitWorldAPI.loadWorld(nextMapName)
        if (currentMapName == "") {
            currentMapName = MapSelector.selectRandomMap()
            BukkitWorldAPI.loadWorld(currentMapName)
        }
    }
}
