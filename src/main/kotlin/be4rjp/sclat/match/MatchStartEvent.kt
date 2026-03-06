package be4rjp.sclat.match

import be4rjp.sclat.data.Match
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event called when the match is started.
 *
 * @property match the match that has started
 */
class MatchStartEvent(
    private val match: Match,
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}
