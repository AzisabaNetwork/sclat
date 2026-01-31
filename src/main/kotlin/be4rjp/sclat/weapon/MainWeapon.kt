package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.DeathMgr
import be4rjp.sclat.manager.MainWeaponMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.SubWeaponMgr
import be4rjp.sclat.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class MainWeapon : Listener {
    @EventHandler
    fun onClickWeapon(e: PlayerInteractEvent) {
        val player = e.getPlayer()

        if (e.getItem() == null) return
        if (!getPlayerData(player)!!.isInMatch) return

        val action = e.getAction()
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            MainWeaponMgr.useMainWeapon(player)
        }
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (getPlayerData(player)!!.isInMatch) {
                SubWeaponMgr.useSubWeapon(
                    player,
                    getPlayerData(player)!!.weaponClass!!.subWeaponName!!,
                )
            }
        }
    }

    @EventHandler
    fun onSneek(event: PlayerToggleSneakEvent) {
        val player = event.getPlayer()
        val data = getPlayerData(player)
        data!!.isSneaking = true
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    data.isSneaking = false
                }
            }
        task.runTaskLater(plugin, 5)
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

        MainWeaponMgr.useMainWeapon(player)

        if (getPlayerData(player)!!.isInMatch) {
            SPWeaponMgr.useSPWeapon(
                player,
                player.inventory.itemInMainHand.itemMeta!!
                    .displayName,
            )
        }
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

        MainWeaponMgr.useMainWeapon(player)

        if (getPlayerData(player)!!.isInMatch) {
            SPWeaponMgr.useSPWeapon(
                player,
                player.inventory.itemInMainHand.itemMeta!!
                    .displayName,
            )
        }
    }

    @EventHandler
    fun onArmorStand(event: PlayerArmorStandManipulateEvent) {
        val player = event.getPlayer()

        if (player.inventory.itemInMainHand
                .itemMeta == null
        ) {
            return
        }
        if (!getPlayerData(player)!!.isInMatch) return

        MainWeaponMgr.useMainWeapon(player)

        if (getPlayerData(player)!!.isInMatch) {
            SPWeaponMgr.useSPWeapon(
                player,
                player.inventory.itemInMainHand.itemMeta!!
                    .displayName,
            )
        }
    }

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.getEntity() is Player) {
            val player = e.getEntity() as Player
            if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                e.isCancelled = true
                if (Sclat.conf!!.config!!.getString("WorkMode") == "Trial") {
                    player.teleport(Sclat.lobby!!)
                    return
                }
                if (getPlayerData(player)!!.isInMatch) {
                    DeathMgr.playerDeathRunnable(player, player, "fall")
                } else {
                    player.teleport(Sclat.lobby!!)
                }
            } else if (e.cause == EntityDamageEvent.DamageCause.DROWNING) {
                e.isCancelled = true
            }
        }
    }
}
