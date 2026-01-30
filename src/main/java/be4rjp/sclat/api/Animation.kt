package be4rjp.sclat.api

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.data.DataMgr
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object Animation {
    @JvmStatic
    fun resultAnimation(
        p: Player,
        team0point: Int,
        team1point: Int,
        team0color: String?,
        team1color: String?,
        winteam: Team?,
        hikiwake: Boolean,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var player: Player = p
                var i: Int = 0
                var g: Int = 0

                override fun run() {
                    if (i <= 15) {
                        player.sendTitle(
                            "",
                            (
                                g.toString() + "% [" + GaugeAPI.toGauge(g, 50, team0color, "§7") +
                                    GaugeAPI.toGauge(50 - g, 50, "§7", team1color) + "] " + g.toString() + "%"
                                ),
                            0,
                            40,
                            0,
                        )
                        g = g + 2
                    }
                /*
                 * if(i >= 6 && i <= 10){ player.sendTitle("", String.valueOf(g) + "% [" +
                 * GaugeAPI.toGauge(g,50,team0color,"§7") + GaugeAPI.toGauge(50 -
                 * g,50,"§7",team1color) + "] " + String.valueOf(g) + "%", 0, 40, 0); g++; }
                 */
                    if (i == 35) {
                        if (hikiwake) {
                            player.sendTitle(
                                "引き分け！",
                                "[" + GaugeAPI.toGauge(50, 100, team0color, team1color) + "]",
                                0,
                                40,
                                10,
                            )
                        } else {
                            if (winteam === DataMgr.getPlayerData(player).team) {
                                player.sendTitle(
                                    ChatColor.GREEN.toString() + "You Win!",
                                    (
                                        team0point.toString() + "% [" +
                                            GaugeAPI.toGauge(team0point, 100, team0color, team1color) + "] " +
                                            (100 - team0point).toString() + "%"
                                        ),
                                    0,
                                    40,
                                    10,
                                )
                            } else {
                                player.sendTitle(
                                    ChatColor.RED.toString() + "You Lose...",
                                    (
                                        team0point.toString() + "% [" +
                                            GaugeAPI.toGauge(team0point, 100, team0color, team1color) + "] " +
                                            (100 - team0point).toString() + "%"
                                        ),
                                    0,
                                    40,
                                    10,
                                )
                            }
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 13.0f, 1.5f)
                    }
                    if (i == 40) {
                        if (winteam === DataMgr.getPlayerData(player).team) {
                            SclatUtil.playGameSound(
                                player,
                                SoundType.CONGRATULATIONS,
                            )
                        }
                        cancel()
                    }
                    i++
                }
            }
        task.runTaskTimer(Sclat.getPlugin(), 0, 2)
    }

    @JvmStatic
    fun areaResultAnimation(
        p: Player,
        team0point: Int,
        team1point: Int,
        team0color: String?,
        team1color: String?,
        winteam: Team?,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var player: Player = p
                var i: Int = 0
                var g: Int = 0

                override fun run() {
                    if (i <= 15) {
                        player.sendTitle(
                            "",
                            (
                                g.toString() + "% [" + GaugeAPI.toGauge(g, 50, team0color, "§7") +
                                    GaugeAPI.toGauge(50 - g, 50, "§7", team1color) + "] " + g.toString() + "%"
                                ),
                            0,
                            40,
                            0,
                        )
                        g = g + 2
                    }
                    if (i == 35) {
                        if (winteam === DataMgr.getPlayerData(player).team) {
                            player.sendTitle(
                                ChatColor.GREEN.toString() + "Knock Out !!",
                                (
                                    team0point.toString() + "% [" +
                                        GaugeAPI.toGauge(team0point, 100, team0color, team1color) + "] " +
                                        (100 - team0point).toString() + "%"
                                    ),
                                0,
                                40,
                                10,
                            )
                        } else {
                            player.sendTitle(
                                ChatColor.RED.toString() + "You Lose...",
                                (
                                    team0point.toString() + "% [" +
                                        GaugeAPI.toGauge(team0point, 100, team0color, team1color) + "] " +
                                        (100 - team0point).toString() + "%"
                                    ),
                                0,
                                40,
                                10,
                            )
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 13.0f, 1.5f)
                    }
                    if (i == 40) {
                        if (winteam === DataMgr.getPlayerData(player).team) {
                            SclatUtil.playGameSound(
                                player,
                                SoundType.CONGRATULATIONS,
                            )
                        }
                        cancel()
                    }
                    i++
                }
            }
        task.runTaskTimer(Sclat.getPlugin(), 0, 2)
    }

    @JvmStatic
    fun tdmResultAnimation(
        p: Player,
        team0point: Int,
        team1point: Int,
        team0color: String?,
        team1color: String?,
        winteam: Team?,
        hikiwake: Boolean,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var player: Player = p
                var i: Int = 0

                override fun run() {
                    if (i <= 15) {
                        player.sendTitle(
                            "",
                            (
                                team0color +
                                    DataMgr
                                        .getPlayerData(player)
                                        .match
                                        .getTeam0()
                                        ?.teamColor
                                        ?.colorName!! + "Team" +
                                    ChatColor.RESET + " : " + Random().nextInt(10).toString() + " Kill       " +
                                    team1color +
                                    DataMgr
                                        .getPlayerData(player)
                                        .match
                                        .getTeam1()
                                        ?.teamColor
                                        ?.colorName!! + "Team" +
                                    ChatColor.RESET + " : " + Random().nextInt(10).toString() + " Kill"
                                ),
                            0,
                            40,
                            0,
                        )
                    }
                /*
                 * if(i >= 6 && i <= 10){ player.sendTitle("", String.valueOf(g) + "% [" +
                 * GaugeAPI.toGauge(g,50,team0color,"§7") + GaugeAPI.toGauge(50 -
                 * g,50,"§7",team1color) + "] " + String.valueOf(g) + "%", 0, 40, 0); g++; }
                 */
                    if (i == 35) {
                        if (hikiwake) {
                            player.sendTitle(
                                "引き分け！",
                                (
                                    team0color +
                                        DataMgr
                                            .getPlayerData(player)
                                            .match
                                            .getTeam0()
                                            ?.teamColor
                                            ?.colorName!! +
                                        "Team" + ChatColor.RESET + " : " + team0point.toString() + " Kill       " +
                                        team1color +
                                        DataMgr
                                            .getPlayerData(player)
                                            .match
                                            .getTeam1()
                                            ?.teamColor
                                            ?.colorName!! +
                                        "Team" + ChatColor.RESET + " : " + team1point.toString() + " Kill"
                                    ),
                                0,
                                40,
                                10,
                            )
                        } else {
                            if (winteam === DataMgr.getPlayerData(player).team) {
                                player.sendTitle(
                                    ChatColor.GREEN.toString() + "You  Win!",
                                    (
                                        team0color +
                                            DataMgr
                                                .getPlayerData(player)
                                                .match
                                                .getTeam0()
                                                ?.teamColor
                                                ?.colorName!! +
                                            "Team" + ChatColor.RESET + " : " + team0point.toString() + " Kill       " + team1color +
                                            DataMgr
                                                .getPlayerData(player)
                                                .match
                                                .getTeam1()
                                                ?.teamColor
                                                ?.colorName!! +
                                            "Team" + ChatColor.RESET + " : " + team1point.toString() + " Kill"
                                        ),
                                    0,
                                    40,
                                    10,
                                )
                            } else {
                                player.sendTitle(
                                    ChatColor.RED.toString() + "You  Lose...",
                                    (
                                        team0color +
                                            DataMgr
                                                .getPlayerData(player)
                                                .match
                                                .getTeam0()
                                                ?.teamColor
                                                ?.colorName!! +
                                            "Team" + ChatColor.RESET + " : " + team0point.toString() + " Kill       " + team1color +
                                            DataMgr
                                                .getPlayerData(player)
                                                .match
                                                .getTeam1()
                                                ?.teamColor
                                                ?.colorName!! +
                                            "Team" + ChatColor.RESET + " : " + team1point.toString() + " Kill"
                                        ),
                                    0,
                                    40,
                                    10,
                                )
                            }
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 13.0f, 1.5f)
                    }
                    if (i == 40) {
                        if (winteam === DataMgr.getPlayerData(player).team) {
                            SclatUtil.playGameSound(
                                player,
                                SoundType.CONGRATULATIONS,
                            )
                        }
                        cancel()
                    }
                    i++
                }
            }
        task.runTaskTimer(Sclat.getPlugin(), 0, 2)
    }
}
