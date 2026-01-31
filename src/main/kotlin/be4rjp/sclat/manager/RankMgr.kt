package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.holo.RankingHolograms
import be4rjp.sclat.plugin
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.TreeMap
import java.util.function.Consumer

/**
 *
 * @author Be4rJP
 */
object RankMgr {
    private val ranks =
        arrayOf<String>(
            "E",
            "D-",
            "D",
            "D+",
            "C-",
            "C",
            "C+",
            "B-",
            "B",
            "B+",
            "A-",
            "A",
            "A+",
            "S-",
            "S",
            "S+",
            "MASTER",
        )
    private val MAX_RATE = (ranks.size - 1) * 500

    var ranking: MutableList<String?> = ArrayList<String?>()
    var killRanking: MutableList<String?> = ArrayList<String?>()
    var paintRanking: MutableList<String?> = ArrayList<String?>()

    // レートを500単位で区切ってランク付けする
    @JvmStatic
    fun toABCRank(ir: Int): String = if (ir >= 0) ranks[if (ir <= MAX_RATE) ir / 500 else ranks.size - 1] else "UnRanked"

    fun IndicateRankPointmove(
        p: Player,
        rankPoint: Int,
    ): Int {
        if (rankPoint == 0) return 0

        val rank = PlayerStatusMgr.getRank(p)

        var rank_Rate = 1.0

        if (rank < 500) {
            rank_Rate = 3.0
        } else if (rank < 2000) {
            rank_Rate = 2.0
        } else if (rank < 3500) {
            rank_Rate = 1.5
        } else if (rank < 6500) {
            rank_Rate = 1.0
        } else if (rank < 8000) {
            rank_Rate = 0.75
        } else if (rank < 20000) {
            rank_Rate = 0.5
        } else {
            rank_Rate = 0.2
        }
        val plus = (rankPoint.toDouble() * rank_Rate).toInt()
        return plus
    }

    fun addPlayerRankPoint(
        uuid: String?,
        rankPoint: Int,
    ) {
        if (rankPoint == 0) return

        val rank = PlayerStatusMgr.getRank(uuid)

        // int MAX_RATE = ranks.length * 500;
        var rank_Rate = 1.0

        if (rank < 500) {
            rank_Rate = 3.0
        } else if (rank < 2000) {
            rank_Rate = 2.0
        } else if (rank < 3500) {
            rank_Rate = 1.5
        } else if (rank < 6500) {
            rank_Rate = 1.0
        } else if (rank < 8000) {
            rank_Rate = 0.75
        } else if (rank < 20000) {
            rank_Rate = 0.5
        } else {
            rank_Rate = 0.2
        }

        // if(rank >= MAX_RATE) {
        // if(rankPoint < 0){
        // double minusRate = (double)MAX_RATE / ((double)MAX_RATE - (double)rank);
        // int minus = (int)((double)rankPoint * minusRate);
        // PlayerStatusMgr.addRank(uuid, -minus);
        // }
        // return;
        // }
        val plus = (rankPoint.toDouble() * rank_Rate).toInt()
        if (plus > 0) {
            // double plusRate = ((double)MAX_RATE - (double)rank) / (double)MAX_RATE;
            // int plus = (int)((double)rankPoint * plusRate);
            PlayerStatusMgr.addRank(uuid, plus)
        }

        // }else{
        // double minusRate = (double)MAX_RATE / ((double)MAX_RATE - (double)rank);
        // int minus = (int)((double)rankPoint * minusRate);
        // PlayerStatusMgr.addRank(uuid, minus);
        // }
    }

    fun makeRankingAsync() {
        val async: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        // かぶらないようにマッピング
                        val playerMap: MutableMap<Int, String> = HashMap()
                        for (uuid in Sclat.Companion.conf!!
                            .playerStatus
                            .getConfigurationSection("Status")!!
                            .getKeys(false)) {
                            var rate =
                                Sclat.Companion.conf!!
                                    .playerStatus
                                    .getInt("Status." + uuid + ".Rank")
                            if (rate == 0) continue

                            while (playerMap.containsKey(rate)) {
                                rate++
                            }
                            playerMap.put(rate, uuid)
                        }

                        val treeMap: MutableMap<Int, String> = TreeMap(Comparator.reverseOrder<Int>())
                        treeMap.putAll(playerMap)
                        ranking = ArrayList<String?>()
                        for (key in treeMap.keys) ranking.add(treeMap.get(key))
                    } catch (e: Exception) {
                    }
                }
            }
        async.runTaskAsynchronously(plugin)
    }

    fun makeKillRankingAsync() {
        val async: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        // かぶらないようにマッピング
                        val playerMap: MutableMap<Int, String> = HashMap()
                        for (uuid in Sclat.conf!!
                            .playerStatus
                            .getConfigurationSection("Status")!!
                            .getKeys(false)) {
                            var rate =
                                Sclat.Companion.conf!!
                                    .playerStatus
                                    .getInt("Status." + uuid + ".Kill")
                            if (rate == 0) continue

                            while (playerMap.containsKey(rate)) {
                                rate++
                            }
                            playerMap.put(rate, uuid)
                        }

                        val treeMap: MutableMap<Int, String> = TreeMap(Comparator.reverseOrder<Int>())
                        treeMap.putAll(playerMap)
                        killRanking = ArrayList()
                        for (key in treeMap.keys) killRanking.add(treeMap.get(key))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        async.runTaskAsynchronously(plugin)
    }

    fun makePaintRankingAsync() {
        val async: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        // かぶらないようにマッピング
                        val playerMap: MutableMap<Int, String> = HashMap()
                        for (uuid in Sclat.Companion.conf!!
                            .playerStatus
                            .getConfigurationSection("Status")!!
                            .getKeys(false)) {
                            var rate =
                                Sclat.Companion.conf!!
                                    .playerStatus
                                    .getInt("Status." + uuid + ".Paint")
                            if (rate == 0) continue

                            while (playerMap.containsKey(rate)) {
                                rate++
                            }
                            playerMap.put(rate, uuid)
                        }

                        val treeMap: MutableMap<Int, String> = TreeMap(Comparator.reverseOrder<Int>())
                        treeMap.putAll(playerMap)
                        paintRanking = ArrayList<String?>()
                        for (key in treeMap.keys) paintRanking.add(treeMap.get(key))
                    } catch (e: Exception) {
                    }
                }
            }
        async.runTaskAsynchronously(plugin)
    }

    fun makeRankingTask() {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    makeRankingAsync()
                    makeKillRankingAsync()
                    makePaintRankingAsync()
                    for (player in plugin.getServer().getOnlinePlayers()) {
                        try {
                            Sclat.playerHolograms.ifPresent(
                                player,
                                Consumer { obj: RankingHolograms? -> obj!!.refreshRankingAsync() },
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        task.runTaskTimer(
            plugin,
            0,
            Sclat.Companion.conf!!
                .config!!
                .getInt("MakeRankingPeriod")
                .toLong(),
        )
    }
}
