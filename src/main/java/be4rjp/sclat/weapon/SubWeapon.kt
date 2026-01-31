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
        val data = getPlayerData(player)

        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand()
                .getItemMeta() == null || player.getInventory().getItemInMainHand().getItemMeta()!!
                .getDisplayName() == null
        ) {
            return
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            SubWeaponMgr.UseSubWeapon(
                player,
                player.getInventory().getItemInMainHand().getItemMeta()!!.getDisplayName(),
            )
        }
    }

    @EventHandler
    fun onPlayerClick(event: PlayerAnimationEvent) {
        val player = event.getPlayer()

        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace.traverse(4.0, 0.5)
        check@ for (vector in positions) {
            val position = vector.toLocation(player.getLocation().getWorld()!!)
            if (position.getBlock().getType().toString().contains("SIGN")) {
                return
            }
        }

        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            if (getPlayerData(player)!!.isInMatch()) {
                SubWeaponMgr.UseSubWeapon(
                    player,
                    getPlayerData(player)!!.getWeaponClass().subWeaponName,
                )
            }
        }
    }

    @EventHandler
    fun PlayerRightClick(event: PlayerInteractEntityEvent) {
        val player = event.getPlayer()
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand()
                .getItemMeta() == null || player.getInventory().getItemInMainHand().getItemMeta()!!
                .getDisplayName() == null
        ) {
            return
        }

        if (!getPlayerData(player)!!.isInMatch()) return

        SubWeaponMgr.UseSubWeapon(player, player.getInventory().getItemInMainHand().getItemMeta()!!.getDisplayName())
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.getPlayer()
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand()
                .getItemMeta() == null || player.getInventory().getItemInMainHand().getItemMeta()!!
                .getDisplayName() == null
        ) {
            return
        }
        if (!getPlayerData(player)!!.isInMatch()) return

        SubWeaponMgr.UseSubWeapon(player, player.getInventory().getItemInMainHand().getItemMeta()!!.getDisplayName())
    }
}
