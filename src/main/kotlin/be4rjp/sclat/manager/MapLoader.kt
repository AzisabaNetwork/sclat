package be4rjp.sclat.manager

import be4rjp.sclat.api.wiremesh.WiremeshListTask
import be4rjp.sclat.data.Area
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.LocMeta
import be4rjp.sclat.data.MapData
import be4rjp.sclat.data.Path
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.math.roundToLong

/**
 * MapLoader: usage-counted synchronous map loader that runs heavy Bukkit
 * operations on the main thread. Callers off the main thread will block
 * until the load completes.
 */
object MapLoader {
    private val usage: MutableMap<String, Int> = ConcurrentHashMap()
    private val unloadRetries: MutableMap<String, Int> = ConcurrentHashMap()

    // Simple in-memory metrics
    private val loadTimes: MutableMap<String, MutableList<Long>> = ConcurrentHashMap()
    private val wiremeshCandidates: MutableMap<String, Int> = ConcurrentHashMap()

    private const val RETRY_DELAY_TICKS = 100L // ~5s
    private const val MAX_RETRIES = 12

    fun incrementUsage(map: MapData) {
        val name = map.mapName ?: return
        val newCount = usage.merge(name, 1) { old, one -> old + one } ?: 1
        sclatLogger.info("MapLoader: incrementUsage($name) -> $newCount")

        if (newCount == 1) {
            // first user — load synchronously on main thread
            if (Bukkit.isPrimaryThread()) {
                loadMapInternal(map)
            } else {
                val latch = CountDownLatch(1)
                Bukkit.getScheduler().runTask(
                    plugin,
                    Runnable {
                        try {
                            loadMapInternal(map)
                        } finally {
                            latch.countDown()
                        }
                    },
                )
                try {
                    latch.await()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    sclatLogger.warn("MapLoader: interrupted while waiting for map load: $name")
                }
            }
        }
    }

    fun releaseMap(map: MapData) {
        val name = map.mapName ?: return
        usage.computeIfPresent(name) { _, old -> (old - 1).coerceAtLeast(0) }
        val current = usage[name] ?: 0
        sclatLogger.info("MapLoader: releaseMap($name) -> $current")
        if (current <= 0) {
            attemptUnload(map)
        }
    }

    private fun toLocation(meta: LocMeta?): Location? {
        if (meta == null) return null
        val worldName = meta.worldName ?: return null
        var w = Bukkit.getWorld(worldName)
        if (w == null) {
            // create world if missing
            w = Bukkit.createWorld(WorldCreator(worldName))
        }
        val loc = Location(w, meta.x, meta.y, meta.z)
        loc.yaw = meta.yaw
        loc.pitch = meta.pitch
        return loc
    }

    private fun loadMapInternal(map: MapData) {
        val name = map.mapName ?: return

        // idempotent
        if (map.team0Loc != null || map.pathList.isNotEmpty() || map.wiremeshListTask != null) {
            sclatLogger.info("MapLoader: map $name already loaded; skipping")
            return
        }

        val startMs = System.currentTimeMillis()

        // Ensure world exists
        val worldName = map.worldName ?: map.introMeta?.worldName ?: map.team0LocMeta?.worldName
        if (worldName != null) {
            var w = Bukkit.getWorld(worldName)
            if (w == null) {
                sclatLogger.info("MapLoader: creating world $worldName for map $name")
                w = Bukkit.createWorld(WorldCreator(worldName))
            }
            map.worldName = w?.name
        }

        // Locations
        map.intro = toLocation(map.introMeta)
        map.team0Loc = toLocation(map.team0LocMeta)
        map.team1Loc = toLocation(map.team1LocMeta)
        map.team0Intro = toLocation(map.team0IntroMeta)
        map.team1Intro = toLocation(map.team1IntroMeta)
        map.resultLoc = toLocation(map.resultLocMeta)
        map.noBlockLocation = toLocation(map.noBlockLocMeta)
        // waiting/taiki location for lobby/waiting players
        val waitLoc = toLocation(map.waitLocMeta)
        // fallback order: waitLoc -> team0Loc -> intro
        val taiki = waitLoc ?: map.team0Loc ?: map.intro
        map.setTaikibasyo(taiki)

        // Paths
        for (pm in map.pathMetaList) {
            val from = toLocation(pm.from)
            val to = toLocation(pm.to)
            val path = Path(from, to)
            map.addPath(path)
        }

        // Areas
        for (am in map.areaMetaList) {
            val from = toLocation(am.from)
            val to = toLocation(am.to)
            if (from != null && to != null) {
                val area = Area(from, to)
                map.addArea(area)
            }
        }

        // Wiremesh (may be expensive)
        map.wiremeshMeta?.let { wm ->
            val from = toLocation(wm.from)
            val to = toLocation(wm.to)
            if (from != null && to != null) {
                val tstart = System.currentTimeMillis()
                val wmTask = WiremeshListTask(from, to, wm.trapDoor, wm.ironBars, wm.fence)
                map.wiremeshListTask = wmTask
                // Start incremental build with small batches to avoid tick stalls
                wmTask.startBuilding(100)
                // record candidate count for metrics
                wiremeshCandidates[name] = wmTask.totalBlocks
                val tend = System.currentTimeMillis()
                sclatLogger.info("MapLoader: wiremesh builder started for $name in ${tend - tstart}ms (candidates=${wmTask.totalBlocks})")
            }
        }

        val ms = System.currentTimeMillis() - startMs
        // record load time metric
        try {
            loadTimes.computeIfAbsent(name) { ArrayList() }.add(ms)
        } catch (_: Exception) {
        }
        sclatLogger.info("MapLoader: loaded map $name in ${ms}ms")
    }

    fun getMetricsString(mapName: String?): String? {
        if (mapName == null) return null
        val loads = loadTimes[mapName]
        val runs = loads?.size ?: 0
        val avg = if (runs > 0) (loads!!.average()).roundToLong() else 0L
        val candidates = wiremeshCandidates.getOrDefault(mapName, 0)
        val deferred = unloadRetries.getOrDefault(mapName, 0)
        return "avgLoad=${avg}ms runs=$runs wiremeshCandidates=$candidates unloadRetries=$deferred"
    }

    fun attemptUnload(
        map: MapData,
        force: Boolean = false,
    ) {
        val name = map.mapName ?: return
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(
                plugin,
                Runnable {
                    attemptUnload(map, force)
                },
            )
            return
        }

        val worldName = map.worldName ?: return
        val world = Bukkit.getWorld(worldName)

        if (!force) {
            if (world != null && world.players.isNotEmpty()) {
                val tries = unloadRetries.getOrDefault(name, 0)
                if (tries >= MAX_RETRIES) {
                    sclatLogger.warn("MapLoader: max unload retries reached for $name; leaving loaded")
                    return
                }
                unloadRetries[name] = tries + 1
                sclatLogger.info("MapLoader: deferred unload for $name - players present (attempt ${tries + 1})")
                Bukkit.getScheduler().runTaskLater(
                    plugin,
                    Runnable { attemptUnload(map, false) },
                    RETRY_DELAY_TICKS,
                )
                return
            }
        }

        // Stop wiremesh tasks
        try {
            map.wiremeshListTask?.stopTask()
            // wait briefly (bounded) for the builder to finish producing
            // remaining wiremeshes. If the builder is still working, wait up
            // to 5 seconds (100 ticks) in 50ms increments on the main thread.
            val wmTask = map.wiremeshListTask
            if (wmTask != null) {
                var waitedTicks = 0
                while (wmTask.isWorking() && waitedTicks < 100) {
                    // run a one-tick wait using scheduler; because we're on main
                    // thread, simply yield by scheduling a sync delayed task and
                    // returning — but this is an unload path invoked on main
                    // thread so instead we'll spin a short busy-sleep to avoid
                    // blocking other tasks for too long. Use Thread.sleep for
                    // short blocks; it's acceptable here because we're already
                    // on the main thread and the wait is tiny. Keep it short to
                    // avoid large stalls.
                    try {
                        Thread.sleep(50)
                    } catch (_: InterruptedException) {
                    }
                    waitedTicks++
                }
                if (wmTask.isWorking()) {
                    sclatLogger.warn("MapLoader: wiremesh builder did not finish promptly for $name; proceeding with unload")
                }
            }
        } catch (e: Exception) {
            sclatLogger.warn("MapLoader: error stopping wiremesh for $name: ${e.message}")
        }

        // Stop paths and areas
        try {
            for (p in map.pathList) {
                p?.stop()
            }
        } catch (e: Exception) {
            sclatLogger.warn("MapLoader: error stopping paths for $name: ${e.message}")
        }
        try {
            for (a in map.areaList) {
                a?.stop()
            }
        } catch (e: Exception) {
            sclatLogger.warn("MapLoader: error stopping areas for $name: ${e.message}")
        }

        // Remove armor stands and clear DataMgr entries that belong to this world's world
        if (world != null) {
            try {
                DataMgr.clearWorldData(world)
            } catch (e: Exception) {
                sclatLogger.warn("MapLoader: error clearing DataMgr for $name: ${e.message}")
            }
        }

        // Unload world
        try {
            if (world != null) {
                Bukkit.unloadWorld(world, false)
            }
        } catch (e: Exception) {
            sclatLogger.warn("MapLoader: error unloading world for $name: ${e.message}")
        }

        // Clear runtime fields
        map.intro = null
        map.team0Loc = null
        map.team1Loc = null
        map.team0Intro = null
        map.team1Intro = null
        map.resultLoc = null
        map.noBlockLocation = null
        map.pathList.clear()
        map.areaList.clear()
        map.wiremeshListTask = null

        usage.remove(name)
        unloadRetries.remove(name)
        sclatLogger.info("MapLoader: unloaded map $name")
    }

    fun unloadAllLoadedMaps() {
        for (map in DataMgr.maplist) {
            attemptUnload(map, true)
        }
    }
}
