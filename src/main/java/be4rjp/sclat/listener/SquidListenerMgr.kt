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
        if (!data!!.isInMatch) return
        val loc = player.location
        val playerblock = player.location.block
        val b1 = loc.add(0.0, -0.5, 0.0).block
        val b2 = player.location.block.getRelative(BlockFace.NORTH)
        val b3 = player.location.block.getRelative(BlockFace.EAST)
        val b4 = player.location.block.getRelative(BlockFace.SOUTH)
        val b5 = player.location.block.getRelative(BlockFace.WEST)

        val list: MutableList<Block> = ArrayList<Block>()
        list.add(b1)
        list.add(b2)
        list.add(b3)
        list.add(b4)
        list.add(b5)

        if (playerblock.type == Material.WATER && player.gameMode == GameMode.ADVENTURE) {
            DeathMgr.PlayerDeathRunnable(
                player,
                player,
                "water",
            )
        }

        try {
            if (data.match.mapData!!.voidY != 0) {
                if (player.location.y <= data.match.mapData!!.voidY) {
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
        getPlayerData(player)!!.team
        Material.getMaterial(
            data.team.teamColor!!
                .concrete
                .toString() + "_POWDER",
        )

        for (block in list) {
            if (block != b1) {
                if (blockDataMap.containsKey(block)) {
                    if (blockDataMap.get(block)!!.team == data.team) {
                        if (!data.isSquid || block.type == Material.AIR) continue
                        data.isOnInk = true
                        player.allowFlight = true
                        player.isFlying = true
                        return
                    }
                }
            } else {
                if (blockDataMap.containsKey(block)) {
                    if (blockDataMap.get(block)!!.team == data.team) {
                        if (!data.isSquid || block.type == Material.AIR) continue
                        data.isOnInk = true
                        if (!data.getIsUsingJetPack()) {
                            player.allowFlight = false
                            player.isFlying = false
                        }
                        return
                    }
                }
            }
        }

        data.isOnInk = false
        if (!data.getIsUsingJetPack()) {
            player.allowFlight = false
            player.isFlying = false
        }
    }
}
