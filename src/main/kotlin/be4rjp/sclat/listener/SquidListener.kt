package be4rjp.sclat.listener

import be4rjp.sclat.data.DataMgr.getPlayerData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent

/**
 *
 * @author Be4rJP
 */
class SquidListener : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        SquidListenerMgr.checkOnInk(event.player)
    }

    @EventHandler
    fun onPlayerSwitchSlot(event: PlayerItemHeldEvent) {
        val player = event.player
        val data = getPlayerData(player)
        SquidListenerMgr.checkOnInk(player)

        // enable squid mode if player held no item
        data?.isSquid = player.inventory.getItem(event.newSlot) == null
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }
}
