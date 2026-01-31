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
        val player = event.getPlayer()
        SquidListenerMgr.checkOnInk(player)
    }

    @EventHandler
    fun onPlayerSwitchSlot(event: PlayerItemHeldEvent) {
        val player = event.getPlayer()
        val data = getPlayerData(player)
        SquidListenerMgr.checkOnInk(player)
        if (player.inventory.getItem(event.newSlot) == null) {
            data!!.isSquid = true
            return
        }
        data!!.isSquid = false
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }
}
