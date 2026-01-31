package be4rjp.sclat.listener

import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.DeathMgr
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
object SquidListenerMgr {
    fun checkOnInk(player: Player) {
        val data = getPlayerData(player)
        if (!data!!.isInMatch()) return
        val loc = player.getLocation()
        val playerblock = player.getLocation().getBlock()
        val b1 = loc.add(0.0, -0.5, 0.0).getBlock()
        val b2 = player.getLocation().getBlock().getRelative(BlockFace.NORTH)
        val b3 = player.getLocation().getBlock().getRelative(BlockFace.EAST)
        val b4 = player.getLocation().getBlock().getRelative(BlockFace.SOUTH)
        val b5 = player.getLocation().getBlock().getRelative(BlockFace.WEST)

        val list: MutableList<Block> = ArrayList<Block>()
        list.add(b1)
        list.add(b2)
        list.add(b3)
        list.add(b4)
        list.add(b5)

        if (playerblock.getType() == Material.WATER && player.getGameMode() == GameMode.ADVENTURE) {
            DeathMgr.PlayerDeathRunnable(
                player,
                player,
                "water",
            )
        }

        try {
            if (data.match.mapData!!.voidY != 0) {
                if (player.getLocation().getY() <= data.match.mapData!!.voidY) {
                    DeathMgr.PlayerDeathRunnable(player, player, "fall")
                }
            }
        } catch (e: Exception) {
        }

        // if(!DataMgr.getBlockDataMap().containsKey(b2) ||
        // !DataMgr.getBlockDataMap().containsKey(b3) ||
        // !DataMgr.getBlockDataMap().containsKey(b4) ||
        // !DataMgr.getBlockDataMap().containsKey(b5) ||
        // !DataMgr.getBlockDataMap().containsKey(b1))
        // return;
        val team = getPlayerData(player)!!.team
        val p =
            Material.getMaterial(
                data.team.teamColor!!
                    .concrete
                    .toString() + "_POWDER",
            )

        for (block in list) {
            if (block != b1) {
                if (blockDataMap.containsKey(block)) {
                    if (blockDataMap.get(block)!!.team == data.team) {
                        if (!data.getIsSquid() || block.getType() == Material.AIR) continue
                        data.setIsOnInk(true)
                        player.setAllowFlight(true)
                        player.setFlying(true)
                        return
                    }
                }
            } else {
                if (blockDataMap.containsKey(block)) {
                    if (blockDataMap.get(block)!!.team == data.team) {
                        if (!data.getIsSquid() || block.getType() == Material.AIR) continue
                        data.setIsOnInk(true)
                        if (!data.getIsUsingJetPack()) {
                            player.setAllowFlight(false)
                            player.setFlying(false)
                        }
                        return
                    }
                }
            }
        }

        data.setIsOnInk(false)
        if (!data.getIsUsingJetPack()) {
            player.setAllowFlight(false)
            player.setFlying(false)
        }
    }
}
