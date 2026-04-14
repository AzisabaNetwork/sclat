package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.AreaMeta
import be4rjp.sclat.data.DataMgr.addMapList
import be4rjp.sclat.data.LocMeta
import be4rjp.sclat.data.MapData
import be4rjp.sclat.data.PathMeta
import be4rjp.sclat.data.WiremeshMeta
import be4rjp.sclat.sclatLogger

/**
 * MapData manager — parse map configuration into metadata only.
 *
 * This avoids performing Bukkit world/Location/Block operations at plugin
 * startup. Runtime objects remain null until MapLoader loads the map.
 */
object MapDataMgr {
    var allmapcount: Int = 0

    @Synchronized
    fun setupMap() {
        val mapsSection = Sclat.conf!!.mapConfig!!.getConfigurationSection("Maps") ?: return
        for (mapname in mapsSection.getKeys(false)) {
            val map = MapData(mapname)

            val worldName = Sclat.conf!!.mapConfig!!.getString("Maps." + mapname + ".WorldName")

            // Intro metadata
            val ix = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Intro.X")
            val iy = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Intro.Y")
            val iz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Intro.Z")
            val iyaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Intro.Yaw")
            map.introMeta = LocMeta(worldName, ix.toDouble(), iy.toDouble(), iz.toDouble(), iyaw.toFloat())

            map.introMoveX = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Intro.MoveX")
            map.introMoveY = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Intro.MoveY")
            map.introMoveZ = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Intro.MoveZ")

            // Team spawns
            val t0x = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0.X")
            val t0y = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0.Y")
            val t0z = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0.Z")
            val t0yaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0.Yaw")
            map.team0LocMeta = LocMeta(worldName, t0x.toDouble() + 0.5, t0y.toDouble(), t0z.toDouble() + 0.5, t0yaw.toFloat())

            val t1x = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1.X")
            val t1y = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1.Y")
            val t1z = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1.Z")
            val t1yaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1.Yaw")
            map.team1LocMeta = LocMeta(worldName, t1x.toDouble() + 0.5, t1y.toDouble(), t1z.toDouble() + 0.5, t1yaw.toFloat())

            // Team intro locations
            val t0intx = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0IntroLoc.X")
            val t0inty = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0IntroLoc.Y")
            val t0intz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0IntroLoc.Z")
            val t0intyaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team0IntroLoc.Yaw")
            map.team0IntroMeta = LocMeta(worldName, t0intx.toDouble(), t0inty.toDouble(), t0intz.toDouble(), t0intyaw.toFloat())

            val t1intx = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1IntroLoc.X")
            val t1inty = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1IntroLoc.Y")
            val t1intz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1IntroLoc.Z")
            val t1intyaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".Team1IntroLoc.Yaw")
            map.team1IntroMeta = LocMeta(worldName, t1intx.toDouble(), t1inty.toDouble(), t1intz.toDouble(), t1intyaw.toFloat())

            // Result loc
            val rlocx = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".ResultLoc.X")
            val rlocy = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".ResultLoc.Y")
            val rlocz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".ResultLoc.Z")
            val rlocyaw = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".ResultLoc.Yaw")
            map.resultLocMeta = LocMeta(worldName, rlocx.toDouble(), rlocy.toDouble(), rlocz.toDouble(), rlocyaw.toFloat(), 90f)

            // Wait / no-block locations
            val tlocx = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".WaitLoc.X")
            val tlocy = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".WaitLoc.Y")
            val tlocz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".WaitLoc.Z")
            map.waitLocMeta = LocMeta(worldName, tlocx.toDouble(), tlocy.toDouble(), tlocz.toDouble())

            val nlocx = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".NoBlockLoc.X")
            val nlocy = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".NoBlockLoc.Y")
            val nlocz = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".NoBlockLoc.Z")
            map.noBlockLocMeta = LocMeta(worldName, nlocx.toDouble(), nlocy.toDouble(), nlocz.toDouble())

            // Paths (metadata)
            if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Path")) {
                val pathSection = Sclat.conf!!.mapConfig!!.getConfigurationSection("Maps." + mapname + ".Path")!!
                for (pathname in pathSection.getKeys(false)) {
                    val flocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".From.X") + 0.5
                    val flocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".From.Y") + 0.5
                    val flocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".From.Z") + 0.5
                    val tolocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".To.X") + 0.5
                    val tolocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".To.Y") + 0.5
                    val tolocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Path." + pathname + ".To.Z") + 0.5
                    val fromMeta = LocMeta(worldName, flocx, flocy, flocz)
                    val toMeta = LocMeta(worldName, tolocx, tolocy, tolocz)
                    map.addPathMeta(PathMeta(fromMeta, toMeta))
                }
            }

            // Areas (metadata)
            if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Area")) {
                val areaSection = Sclat.conf!!.mapConfig!!.getConfigurationSection("Maps." + mapname + ".Area")!!
                for (Areaname in areaSection.getKeys(false)) {
                    map.canAreaBattle = true
                    val flocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".From.X") + 0.5
                    val flocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".From.Y") + 0.5
                    val flocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".From.Z") + 0.5
                    val tolocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".To.X") + 0.5
                    val tolocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".To.Y") + 0.5
                    val tolocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Area." + Areaname + ".To.Z") + 0.5
                    val fromMeta = LocMeta(worldName, flocx, flocy, flocz)
                    val toMeta = LocMeta(worldName, tolocx, tolocy, tolocz)
                    map.addAreaMeta(AreaMeta(fromMeta, toMeta))
                }
            }

            // Can paint barrier block flag
            var canpaintbblock = false
            if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".CanPaintBarrierBlock")) {
                canpaintbblock = Sclat.conf!!.mapConfig!!.getBoolean("Maps." + mapname + ".CanPaintBarrierBlock")
            }
            map.setCanPaintBBlock(canpaintbblock)

            // Wiremesh metadata
            if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Wiremesh")) {
                var trapDoor = false
                if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Wiremesh.TrapDoor")) {
                    trapDoor = Sclat.conf!!.mapConfig!!.getBoolean("Maps." + mapname + ".Wiremesh.TrapDoor")
                }
                var ironBars = false
                if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Wiremesh.IronBars")) {
                    ironBars = Sclat.conf!!.mapConfig!!.getBoolean("Maps." + mapname + ".Wiremesh.IronBars")
                }
                var fence = false
                if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".Wiremesh.Fence")) {
                    fence = Sclat.conf!!.mapConfig!!.getBoolean("Maps." + mapname + ".Wiremesh.Fence")
                }
                val flocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.From.X") + 0.5
                val flocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.From.Y")
                val flocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.From.Z") + 0.5
                val tolocx = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.To.X") + 0.5
                val tolocy = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.To.Y")
                val tolocz = Sclat.conf!!.mapConfig!!.getDouble("Maps." + mapname + ".Wiremesh.To.Z") + 0.5
                val fromMeta = LocMeta(worldName, flocx, flocy, flocz)
                val toMeta = LocMeta(worldName, tolocx, tolocy, tolocz)
                map.wiremeshMeta = WiremeshMeta(fromMeta, toMeta, trapDoor, ironBars, fence)
            }

            if (Sclat.conf!!.mapConfig!!.contains("Maps." + mapname + ".VoidY")) {
                map.voidY = Sclat.conf!!.mapConfig!!.getInt("Maps." + mapname + ".VoidY")
            }

            // Perform final registration
            sclatLogger.info(mapname)
            map.worldName = worldName
            allmapcount++
            addMapList(map)
        }
    }
}
