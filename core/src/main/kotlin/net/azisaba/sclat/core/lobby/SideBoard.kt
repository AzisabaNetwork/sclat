package net.azisaba.sclat.core.lobby

import fr.mrmicky.fastboard.FastBoard
import org.bukkit.entity.Player
import java.util.UUID

// / Todo: Side status board for lobby
class SideBoard {
    private val boards: MutableMap<UUID, FastBoard> = mutableMapOf()

    fun addPlayer(player: Player) {
        if (boards.containsKey(player.uniqueId)) return
        boards[player.uniqueId] = FastBoard(player)
    }

    fun removePlayer(player: Player) {
        boards.remove(player.uniqueId)?.delete()
    }

    fun updateBoard(board: FastBoard) {
        val player = board.player
        board.updateLines(
//            "§7§m                                  ",
//            "",
//            "§6§lステータス »",
//            "§e COIN: §r" + PlayerStatusMgr.getMoney(player),
//            "§e TICKET: §r" + PlayerStatusMgr.getTicket(player),
//            "§b RANK: §r" + RankMgr.toABCRank(PlayerStatusMgr.getRank(player)) + " [" +
//                PlayerStatusMgr.getRank(player) + "]",
//            " ",
//            "§9§lサーバー »",
//            *ServerStatusManager.serverList
//                .filter { status -> !status.isMaintenance && status.isOnline }
//                .mapNotNull { serverStatus ->
//                    StatusLine.getLine(
//                        ServerStatus(
//                            serverStatus.isOnline,
//                            serverStatus.isMaintenance,
//                            serverStatus.displayName,
//                            serverStatus.matchStartTime,
//                            serverStatus.playerCount,
//                            serverStatus.waitingEndTime,
//                            serverStatus.runningMatch,
//                        ),
//                    )
//                }.toTypedArray(),
//            "  ",
//            "§a§lNews »",
//            textAnimation.next(),
//            "   ", // Prevent from same name
            "§7§m                                  §r",
        )
    }
}
