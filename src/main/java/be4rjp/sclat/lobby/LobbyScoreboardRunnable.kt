package be4rjp.sclat.lobby

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.utils.ObjectiveUtil
import be4rjp.sclat.api.utils.TextAnimation
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.manager.RankMgr
import be4rjp.sclat.manager.ServerStatusManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class LobbyScoreboardRunnable(
    private val player: Player,
) : BukkitRunnable() {
    private val playerData: PlayerData?
    private val scoreboard: Scoreboard
    private val textAnimation: TextAnimation
    private var objective: Objective

    init {
        this.playerData = getPlayerData(player)

        val scoreboardManager = Bukkit.getScoreboardManager()
        this.scoreboard = scoreboardManager!!.getNewScoreboard()
        this.objective = scoreboard.registerNewObjective("Lobby", player.getName(), "§6§lSclat §r" + Sclat.VERSION)
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR)

        player.setScoreboard(scoreboard)

        val text = ChatColor.translateAlternateColorCodes('&', Sclat.news.getConfig()!!.getString("news-message")!!)

        this.textAnimation = TextAnimation(text, Sclat.news.getConfig()!!.getInt("scoreboard-length"))
    }

    override fun run() {
        objective.unregister()
        if (!player.isOnline()) cancel()

        val lines: MutableList<String> = mutableListOf()
        lines.add("§7§m                                  ")
        lines.add("")
        lines.add("§6§lステータス »")
        lines.add("§e COIN: §r" + PlayerStatusMgr.getMoney(player))
        lines.add("§e TICKET: §r" + PlayerStatusMgr.getTicket(player))
        lines.add(
            (
                "§b RANK: §r" + RankMgr.toABCRank(PlayerStatusMgr.getRank(player)) + " [" +
                    PlayerStatusMgr.getRank(player) + "]"
                ),
        )
        lines.add(" ")
        lines.add("§9§lサーバー »")
        for (serverStatus in ServerStatusManager.serverList) {
            if (serverStatus.isMaintenance) continue
            if (!serverStatus.isOnline) continue

            var line = ""
            if (serverStatus.runningMatch) {
                val time = System.currentTimeMillis() / 1000 - serverStatus.matchStartTime
                val min = String.format("%02d", time % 60)
                line = (
                    serverStatus.playerCount.toString() + "§e人が試合中" +
                        (if (time < 10000) " §r(" + time / 60 + ":" + min + ")" else "")
                    )
            } else {
                if (serverStatus.waitingEndTime != 0L) {
                    line = (
                        serverStatus.playerCount.toString() + "§a人が待機中" + " §r(§b" +
                            ((serverStatus.waitingEndTime - (System.currentTimeMillis() / 1000)).toString() + "§r秒後に開始)")
                        )
                } else {
                    line = serverStatus.playerCount.toString() + "§a人が待機中"
                }
            }

            lines.add(" " + serverStatus.displayName + ": §r" + line)
        }
        lines.add("  ")
        lines.add("§a§lNews »")
        lines.add(textAnimation.next())
        lines.add("   ")
        lines.add("§7§m                                  §r")

        objective = scoreboard.registerNewObjective("Lobby", player.getName(), "§6§lSclat §r" + Sclat.VERSION)
        objective.setDisplaySlot(DisplaySlot.SIDEBAR)
        ObjectiveUtil.setLine(objective, lines)
    }
}
