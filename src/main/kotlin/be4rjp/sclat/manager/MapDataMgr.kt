package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.wiremesh.WiremeshListTask
import be4rjp.sclat.data.Area
import be4rjp.sclat.data.DataMgr.addMapList
import be4rjp.sclat.data.MapData
import be4rjp.sclat.data.Path
import be4rjp.sclat.plugin
import org.bukkit.Bukkit
import org.bukkit.Location

/**
 *
 * @author Be4rJP
 */
object MapDataMgr {
    var allmapcount: Int = 0

    @Synchronized
    fun SetupMap() {
        for (mapname in Sclat.Companion.conf!!
            .mapConfig!!
            .getConfigurationSection("Maps")!!
            .getKeys(false)) {
            val map = MapData(mapname)
            val WorldName =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getString("Maps." + mapname + ".WorldName")
            val w = Bukkit.getServer().getWorld(WorldName!!)

            val ix =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Intro.X")
            val iy =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Intro.Y")
            val iz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Intro.Z")
            val iyaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Intro.Yaw")
            val il = Location(w, ix.toDouble(), iy.toDouble(), iz.toDouble())
            il.setYaw(iyaw.toFloat())

            val intromovex =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getDouble("Maps." + mapname + ".Intro.MoveX")
            val intromovey =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getDouble("Maps." + mapname + ".Intro.MoveY")
            val intromovez =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getDouble("Maps." + mapname + ".Intro.MoveZ")

            val t0x =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0.X")
            val t0y =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0.Y")
            val t0z =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0.Z")
            val t0yaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0.Yaw")
            val t0l = Location(w, t0x.toDouble(), t0y.toDouble(), t0z.toDouble())
            t0l.setX(t0l.getX() + 0.5)
            t0l.setZ(t0l.getZ() + 0.5)
            t0l.setYaw(t0yaw.toFloat())

            val t1x =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1.X")
            val t1y =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1.Y")
            val t1z =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1.Z")
            val t1yaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1.Yaw")
            val t1l = Location(w, t1x.toDouble(), t1y.toDouble(), t1z.toDouble())
            t1l.setX(t1l.getX() + 0.5)
            t1l.setZ(t1l.getZ() + 0.5)
            t1l.setYaw(t1yaw.toFloat())

            val t0intx =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0IntroLoc.X")
            val t0inty =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0IntroLoc.Y")
            val t0intz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0IntroLoc.Z")
            val t0intyaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team0IntroLoc.Yaw")
            val t0intl = Location(w, t0intx.toDouble(), t0inty.toDouble(), t0intz.toDouble())
            t0intl.setYaw(t0intyaw.toFloat())

            val t1intx =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1IntroLoc.X")
            val t1inty =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1IntroLoc.Y")
            val t1intz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1IntroLoc.Z")
            val t1intyaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".Team1IntroLoc.Yaw")
            val t1intl = Location(w, t1intx.toDouble(), t1inty.toDouble(), t1intz.toDouble())
            t1intl.setYaw(t1intyaw.toFloat())

            val rlocx =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".ResultLoc.X")
            val rlocy =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".ResultLoc.Y")
            val rlocz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".ResultLoc.Z")
            val rlocyaw =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".ResultLoc.Yaw")
            val rloc = Location(w, rlocx.toDouble(), rlocy.toDouble(), rlocz.toDouble())
            rloc.setYaw(rlocyaw.toFloat())
            rloc.setPitch(90f)

            val tlocx =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".WaitLoc.X")
            val tlocy =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".WaitLoc.Y")
            val tlocz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".WaitLoc.Z")
            val tloc = Location(w, tlocx.toDouble(), tlocy.toDouble(), tlocz.toDouble())

            val nlocx =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".NoBlockLoc.X")
            val nlocy =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".NoBlockLoc.Y")
            val nlocz =
                Sclat.Companion.conf!!
                    .mapConfig!!
                    .getInt("Maps." + mapname + ".NoBlockLoc.Z")
            val nloc = Location(w, nlocx.toDouble(), nlocy.toDouble(), nlocz.toDouble())

            if (Sclat.Companion.conf!!
                    .mapConfig!!
                    .contains("Maps." + mapname + ".Path")
            ) {
                for (pathname in Sclat.Companion.conf!!
                    .mapConfig!!
                    .getConfigurationSection("Maps." + mapname + ".Path")!!
                    .getKeys(false)) {
                    val flocx =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".From.X") +
                                0.5
                        )
                    val flocy =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".From.Y") +
                                0.5
                        )
                    val flocz =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".From.Z") +
                                0.5
                        )
                    val from = Location(w, flocx, flocy, flocz)

                    val tolocx =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".To.X") +
                                0.5
                        )
                    val tolocy =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".To.Y") +
                                0.5
                        )
                    val tolocz =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Path." + pathname + ".To.Z") +
                                0.5
                        )
                    val to = Location(w, tolocx, tolocy, tolocz)
                    val path = Path(from, to)
                    map.addPath(path)
                }
            }

            if (Sclat.Companion.conf!!
                    .mapConfig!!
                    .contains("Maps." + mapname + ".Area")
            ) {
                for (Areaname in Sclat.Companion.conf!!
                    .mapConfig!!
                    .getConfigurationSection("Maps." + mapname + ".Area")!!
                    .getKeys(false)) {
                    map.canAreaBattle = true
                    val flocx =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".From.X") +
                                0.5
                        )
                    val flocy =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".From.Y") +
                                0.5
                        )
                    val flocz =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".From.Z") +
                                0.5
                        )
                    val from = Location(w, flocx, flocy, flocz)

                    val tolocx =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".To.X") +
                                0.5
                        )
                    val tolocy =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".To.Y") +
                                0.5
                        )
                    val tolocz =
                        (
                            Sclat.Companion.conf!!
                                .mapConfig!!
                                .getDouble("Maps." + mapname + ".Area." + Areaname + ".To.Z") +
                                0.5
                        )
                    val to = Location(w, tolocx, tolocy, tolocz)
                    val area = Area(from, to)
                    map.addArea(area)
                }
            }

            var canpaintbblock = false
            if (Sclat.Companion.conf!!
                    .mapConfig!!
                    .contains("Maps." + mapname + ".CanPaintBarrierBlock")
            ) {
                canpaintbblock =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getBoolean("Maps." + mapname + ".CanPaintBarrierBlock")
            }

            if (Sclat.Companion.conf!!
                    .mapConfig!!
                    .contains("Maps." + mapname + ".Wiremesh")
            ) {
                var trapDoor = false
                if (Sclat.Companion.conf!!
                        .mapConfig!!
                        .contains("Maps." + mapname + ".Wiremesh.TrapDoor")
                ) {
                    trapDoor =
                        Sclat.Companion.conf!!
                            .mapConfig!!
                            .getBoolean("Maps." + mapname + ".Wiremesh.TrapDoor")
                }
                var ironBars = false
                if (Sclat.Companion.conf!!
                        .mapConfig!!
                        .contains("Maps." + mapname + ".Wiremesh.IronBars")
                ) {
                    ironBars =
                        Sclat.Companion.conf!!
                            .mapConfig!!
                            .getBoolean("Maps." + mapname + ".Wiremesh.IronBars")
                }
                var fence = false
                if (Sclat.Companion.conf!!
                        .mapConfig!!
                        .contains("Maps." + mapname + ".Wiremesh.Fence")
                ) {
                    fence =
                        Sclat.Companion.conf!!
                            .mapConfig!!
                            .getBoolean("Maps." + mapname + ".Wiremesh.Fence")
                }

                val flocx =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.From.X") + 0.5
                val flocy =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.From.Y")
                val flocz =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.From.Z") + 0.5
                val from = Location(w, flocx, flocy, flocz)

                val tolocx =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.To.X") + 0.5
                val tolocy =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.To.Y")
                val tolocz =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getDouble("Maps." + mapname + ".Wiremesh.To.Z") + 0.5
                val to = Location(w, tolocx, tolocy, tolocz)

                val wmListTask = WiremeshListTask(from, to, trapDoor, ironBars, fence)
                map.wiremeshListTask = wmListTask
            }

            if (Sclat.Companion.conf!!
                    .mapConfig!!
                    .contains("Maps." + mapname + ".VoidY")
            ) {
                map.voidY =
                    Sclat.Companion.conf!!
                        .mapConfig!!
                        .getInt("Maps." + mapname + ".VoidY")
            }

            map.intro = il
            map.team0Loc = t0l
            map.team1Loc = t1l
            map.team0Intro = t0intl
            map.team1Intro = t1intl
            map.resultLoc = rloc
            map.setTaikibasyo(tloc)
            map.noBlockLocation = nloc

            map.introMoveX = intromovex
            map.introMoveY = intromovey
            map.introMoveZ = intromovez

            map.setCanPaintBBlock(canpaintbblock)

            // Main.getPlugin().getServer().createWorld(new WorldCreator(WorldName));
            plugin.getLogger().info(mapname)

            map.worldName = WorldName

            allmapcount++

            // DataMgr.setMap(mapname, map);
            addMapList(map)
        }
    }
}
