package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.wiremesh.WiremeshListTask
import be4rjp.sclat.data.Area
import be4rjp.sclat.data.DataMgr.addMapList
import be4rjp.sclat.data.MapData
import be4rjp.sclat.data.Path
import be4rjp.sclat.extension.getLocation
import be4rjp.sclat.extension.getLocationWithPitch
import be4rjp.sclat.extension.getLocationWithYaw
import be4rjp.sclat.extension.getSection
import be4rjp.sclat.sclatLogger
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

object MapDataMgr {
    var allmapcount: Int = 0

    @Synchronized
    fun setupMap() {
        val mapConfig = Sclat.conf?.mapConfig ?: return
        val mapsSection = mapConfig.getConfigurationSection("Maps") ?: return

        for (mapName in mapsSection.getKeys(false)) {
            val mapSection = mapsSection.getConfigurationSection(mapName) ?: continue
            val worldName = mapSection.getString("WorldName") ?: continue
            val world = Bukkit.getServer().getWorld(worldName) ?: continue

            val map =
                MapData(mapName).apply {
                    this.worldName = worldName
                    intro = loadIntroLocation(mapSection, world)
                    team0Loc = loadTeamLocation(mapSection, world, "Team0")
                    team1Loc = loadTeamLocation(mapSection, world, "Team1")
                    team0Intro = mapSection.getLocationWithYaw("Team0IntroLoc", world)
                    team1Intro = mapSection.getLocationWithYaw("Team1IntroLoc", world)
                    resultLoc = mapSection.getLocationWithPitch("ResultLoc", world).apply { pitch = 90f }
                    setTaikibasyo(mapSection.getLocation("WaitLoc", world))
                    noBlockLocation = mapSection.getLocation("NoBlockLoc", world)
                    introMoveX = mapSection.getDouble("Intro.MoveX", 0.0)
                    introMoveY = mapSection.getDouble("Intro.MoveY", 0.0)
                    introMoveZ = mapSection.getDouble("Intro.MoveZ", 0.0)
                    setCanPaintBBlock(mapSection.getBoolean("CanPaintBarrierBlock", false))
                    voidY = mapSection.getInt("VoidY", 0)
                    canAreaBattle = false
                }

            loadPaths(mapSection, world, map)
            loadAreas(mapSection, world, map)
            loadWiremesh(mapSection, world, map)

            sclatLogger.info(mapName)
            allmapcount++
            addMapList(map)
        }
    }

    private fun loadIntroLocation(
        section: ConfigurationSection,
        world: World,
    ): Location {
        val x = section.getInt("Intro.X").toDouble()
        val y = section.getInt("Intro.Y").toDouble()
        val z = section.getInt("Intro.Z").toDouble()
        val yaw = section.getInt("Intro.Yaw").toFloat()
        return Location(world, x, y, z).apply { this.yaw = yaw }
    }

    private fun loadTeamLocation(
        section: ConfigurationSection,
        world: World,
        teamPath: String,
    ): Location {
        val x = section.getInt("$teamPath.X").toDouble() + 0.5
        val y = section.getInt("$teamPath.Y").toDouble()
        val z = section.getInt("$teamPath.Z").toDouble() + 0.5
        val yaw = section.getInt("$teamPath.Yaw").toFloat()
        return Location(world, x, y, z).apply { this.yaw = yaw }
    }

    private fun loadPaths(
        section: ConfigurationSection,
        world: World,
        map: MapData,
    ) {
        val pathSection = section.getSection("Path") ?: return
        for (pathName in pathSection.getKeys(false)) {
            val pSection = pathSection.getConfigurationSection(pathName) ?: continue
            val from = pSection.getLocation("From", world)
            val to = pSection.getLocation("To", world)
            map.addPath(Path(from, to))
        }
    }

    private fun loadAreas(
        section: ConfigurationSection,
        world: World,
        map: MapData,
    ) {
        val areaSection = section.getSection("Area") ?: return
        map.canAreaBattle = true
        for (areaName in areaSection.getKeys(false)) {
            val aSection = areaSection.getConfigurationSection(areaName) ?: continue
            val from = aSection.getLocation("From", world)
            val to = aSection.getLocation("To", world)
            map.addArea(Area(from, to))
        }
    }

    private fun loadWiremesh(
        section: ConfigurationSection,
        world: World,
        map: MapData,
    ) {
        val wmSection = section.getSection("Wiremesh") ?: return
        val trapDoor = wmSection.getBoolean("TrapDoor", false)
        val ironBars = wmSection.getBoolean("IronBars", false)
        val fence = wmSection.getBoolean("Fence", false)
        val from = wmSection.getLocation("From", world)
        val to = wmSection.getLocation("To", world)
        map.wiremeshListTask = WiremeshListTask(from, to, trapDoor, ironBars, fence)
    }
}
