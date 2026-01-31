package be4rjp.sclat.weapon

import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.SubWeaponMgr
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 *
 * @author Be4rJP
 */
class SubWeapon : Listener {
    // サブウエポンのリスナー部分
    @EventHandler
    fun onClickSubWeapon(event: PlayerInteractEvent) {
        val player = event.getPlayer()
        val action = event.getAction()
        getPlayerData(player)

        if (player.inventory.itemInMainHand
                .itemMeta == null
        ) {
            return
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            SubWeaponMgr.UseSubWeapon(
                player,
                player.inventory.itemInMainHand.itemMeta!!
                    .displayName,
            )
        }
    }

    @EventHandler
    fun onPlayerClick(event: PlayerAnimationEvent) {
        val player = event.getPlayer()

        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions = rayTrace.traverse(4.0, 0.5)
        check@ for (vector in positions) {
            val position = vector.toLocation(player.location.world!!)
            if (position.block.type
                    .toString()
                    .contains("SIGN")
            ) {
                return
            }
        }

        if (event.animationType == PlayerAnimationType.ARM_SWING) {
            if (getPlayerData(player)!!.isInMatch) {
                SubWeaponMgr.UseSubWeapon(
                    player,
                    getPlayerData(player)!!.weaponClass!!.subWeaponName,
                )
            }
        }
    }

    @EventHandler
    fun playerRightClick(event: PlayerInteractEntityEvent) {
        val player = event.getPlayer()
        if (player.inventory.itemInMainHand
                .itemMeta == null
        ) {
            return
        }

        if (!getPlayerData(player)!!.isInMatch) return

        SubWeaponMgr.UseSubWeapon(
            player,
            player.inventory.itemInMainHand.itemMeta!!
                .displayName,
        )
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.getPlayer()
        if (player.inventory.itemInMainHand
                .itemMeta == null
        ) {
            return
        }
        if (!getPlayerData(player)!!.isInMatch) return

        SubWeaponMgr.UseSubWeapon(
            player,
            player.inventory.itemInMainHand.itemMeta!!
                .displayName,
        )
    }
}
