package be4rjp.sclat.weapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SPWeaponMgr
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 *
 * @author Be4rJP
 */
class SPWeapon : Listener {
    // スペシャルウエポンのリスナー部分
    @EventHandler
    fun onClickSPWeapon(event: PlayerInteractEvent) {
        val player = event.getPlayer()
        val action = event.getAction()
        getPlayerData(player)

        if (player.inventory.itemInMainHand
                .itemMeta == null
        ) {
            return
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            SPWeaponMgr.useSPWeapon(
                player,
                player.inventory.itemInMainHand.itemMeta!!
                    .displayName,
            )
        }
    }
}
