package be4rjp.sclat.api.rank;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.holo.RankingHolograms;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static be4rjp.sclat.Sclat.conf;

public class RankingUpdater {
    public static List<String> ranking = new ArrayList<>();
    public static List<String> killRanking = new ArrayList<>();
    public static List<String> paintRanking = new ArrayList<>();

    public static void makeRankingAsync() {
        BukkitRunnable async = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // かぶらないようにマッピング
                    Map<Integer, String> playerMap = new HashMap<>();
                    for (String uuid : conf.getPlayerStatus().getConfigurationSection("Status").getKeys(false)) {
                        int rate = conf.getPlayerStatus().getInt("Status." + uuid + ".Rank");
                        if (rate == 0)
                            continue;

                        while (playerMap.containsKey(rate)) {
                            rate++;
                        }
                        playerMap.put(rate, uuid);
                    }

                    Map<Integer, String> treeMap = new TreeMap<>(Comparator.reverseOrder());
                    treeMap.putAll(playerMap);
                    ranking = new ArrayList<>();
                    for (Integer key : treeMap.keySet())
                        ranking.add(treeMap.get(key));
                } catch (Exception e) {
                }
            }
        };
        async.runTaskAsynchronously(Sclat.getPlugin());
    }

    public static void makeKillRankingAsync() {
        BukkitRunnable async = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // かぶらないようにマッピング
                    Map<Integer, String> playerMap = new HashMap<>();
                    for (String uuid : conf.getPlayerStatus().getConfigurationSection("Status").getKeys(false)) {
                        int rate = conf.getPlayerStatus().getInt("Status." + uuid + ".Kill");
                        if (rate == 0)
                            continue;

                        while (playerMap.containsKey(rate)) {
                            rate++;
                        }
                        playerMap.put(rate, uuid);
                    }

                    Map<Integer, String> treeMap = new TreeMap<>(Comparator.reverseOrder());
                    treeMap.putAll(playerMap);
                    killRanking = new ArrayList<>();
                    for (Integer key : treeMap.keySet())
                        killRanking.add(treeMap.get(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        async.runTaskAsynchronously(Sclat.getPlugin());
    }

    public static void makePaintRankingAsync() {
        BukkitRunnable async = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // かぶらないようにマッピング
                    Map<Integer, String> playerMap = new HashMap<>();
                    for (String uuid : conf.getPlayerStatus().getConfigurationSection("Status").getKeys(false)) {
                        int rate = conf.getPlayerStatus().getInt("Status." + uuid + ".Paint");
                        if (rate == 0)
                            continue;

                        while (playerMap.containsKey(rate)) {
                            rate++;
                        }
                        playerMap.put(rate, uuid);
                    }

                    Map<Integer, String> treeMap = new TreeMap<>(Comparator.reverseOrder());
                    treeMap.putAll(playerMap);
                    paintRanking = new ArrayList<>();
                    for (Integer key : treeMap.keySet())
                        paintRanking.add(treeMap.get(key));
                } catch (Exception e) {
                }
            }
        };
        async.runTaskAsynchronously(Sclat.getPlugin());
    }

    public static void makeRankingTask() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                makeRankingAsync();
                makeKillRankingAsync();
                makePaintRankingAsync();
                for (Player player : Sclat.getPlugin().getServer().getOnlinePlayers()) {
                    try {
                        Sclat.playerHolograms.ifPresent(player, RankingHolograms::refreshRankingAsync);
                    } catch (Exception e) {
                    }
                }
            }
        };
        task.runTaskTimer(Sclat.getPlugin(), 0, conf.getConfig().getInt("MakeRankingPeriod"));
    }
}
