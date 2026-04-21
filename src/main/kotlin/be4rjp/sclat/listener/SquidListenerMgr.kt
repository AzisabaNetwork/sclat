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
        if (!data?.isInMatch!!) return

        val loc = player.location
        val playerBlock = player.location.block
        val b1 = loc.add(0.0, -0.5, 0.0).block

        val list: MutableList<Block> =
            mutableListOf(
                b1,
                *listOf(
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                ).map { face -> playerBlock.getRelative(face) }.toTypedArray(),
            )

        if (playerBlock.type == Material.WATER && player.gameMode == GameMode.ADVENTURE) {
            DeathMgr.playerDeathRunnable(
                player,
                player,
                "water",
            )
        }

        try {
            if (data.match?.mapData!!.voidY != 0) {
                if (player.location.y <= data.match?.mapData!!.voidY) {
                    DeathMgr.playerDeathRunnable(player, player, "fall")
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
            data.team
                ?.teamColor!!
                .concrete
                .toString() + "_POWDER",
        )

        if (data.isSquid) {
            list
                .firstOrNull { b ->
                    blockDataMap.containsKey(b) &&
                        blockDataMap[b]?.team == data.team &&
                        b.type != Material.AIR
                }.let { b ->
                    data.isOnInk = b != null
                    if (b == b1 || b == null) {
                        if (!data.isUsingJetPack) {
                            player.allowFlight = false
                            player.isFlying = false
                        }
                    } else {
                        player.allowFlight = true
                        player.isFlying = true
                        return
                    }
                }
        }
    }
}
