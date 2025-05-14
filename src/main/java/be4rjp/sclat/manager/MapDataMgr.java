package be4rjp.sclat.manager;

import be4rjp.sclat.Main;
import static be4rjp.sclat.Main.conf;
import be4rjp.sclat.data.Area;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.MapData;
import be4rjp.sclat.data.Path;
import be4rjp.sclat.data.WiremeshListTask;
import static org.bukkit.Bukkit.getServer;

import be4rjp.sclat.utils.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Be4rJP
 */
public class MapDataMgr {
    
    public static int allMapCount = 0;
    
    public synchronized static void SetupMap(){
        ConfigurationSection mapsSection = conf.getMapConfig().getConfigurationSection("Maps");
        assert mapsSection != null;

        for (String mapName : mapsSection.getKeys(false)){
            MapData map = new MapData(mapName);
            String WorldName = conf.getMapConfig().getString("Maps." + mapName + ".WorldName");
            assert WorldName != null;

            World world = getServer().getWorld(WorldName);

            Location introLocation = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".Intro");
            
            double intromovex = conf.getMapConfig().getDouble("Maps." + mapName + ".Intro.MoveX");
            double intromovey = conf.getMapConfig().getDouble("Maps." + mapName + ".Intro.MoveY");
            double intromovez = conf.getMapConfig().getDouble("Maps." + mapName + ".Intro.MoveZ");

            Location team0Location = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".Team0");
            team0Location.setX(team0Location.getX() + 0.5);
            team0Location.setZ(team0Location.getZ() + 0.5);

            Location team1Location = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".Team1");
            team1Location.setX(team1Location.getX() + 0.5);
            team1Location.setZ(team1Location.getZ() + 0.5);

            Location team0IntroLocation = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".Team0IntroLoc");
            Location team1IntroLocation = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".Team1IntroLoc");

            Location resultLocation = ConfigUtil.getLocationWithYaw(mapsSection, world, mapName + ".ResultLoc");
            resultLocation.setPitch(90);

            Location waitLocation = ConfigUtil.getLocation(mapsSection, world, mapName + ".WaitLoc");

            Location noBlockLocation = ConfigUtil.getLocation(mapsSection, world, mapName + ".NoBlockLoc");
            
            if(conf.getMapConfig().contains("Maps." + mapName + ".Path")){
                for (String pathname : conf.getMapConfig().getConfigurationSection("Maps." + mapName + ".Path").getKeys(false)){
                    String pathPrefix = mapName + ".Path.";
                    double flocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".From.X") + 0.5;
                    double flocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".From.Y") + 0.5;
                    double flocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".From.Z") + 0.5;
                    Location from = new Location(world, flocx, flocy, flocz);
                    ConfigUtil.getLocation(mapsSection, world, pathPrefix + pathname);

                    double tolocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".To.X") + 0.5;
                    double tolocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".To.Y") + 0.5;
                    double tolocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Path." + pathname + ".To.Z") + 0.5;
                    Location to = new Location(world, tolocx, tolocy, tolocz);
                    Path path = new Path(from, to);
                    map.addPath(path);
                }
            }
            
            if(conf.getMapConfig().contains("Maps." + mapName + ".Area")){
                for (String Areaname : conf.getMapConfig().getConfigurationSection("Maps." + mapName + ".Area").getKeys(false)){
                    map.setCanAreaBattle(true);
                    double flocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".From.X") + 0.5;
                    double flocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".From.Y") + 0.5;
                    double flocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".From.Z") + 0.5;
                    Location from = new Location(world, flocx, flocy, flocz);

                    double tolocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".To.X") + 0.5;
                    double tolocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".To.Y") + 0.5;
                    double tolocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Area." + Areaname + ".To.Z") + 0.5;
                    Location to = new Location(world, tolocx, tolocy, tolocz);
                    Area area = new Area(from, to);
                    map.addArea(area);
                }
            }
            
            boolean canpaintbblock = false;
            if(conf.getMapConfig().contains("Maps." + mapName + ".CanPaintBarrierBlock"))
                canpaintbblock = conf.getMapConfig().getBoolean("Maps." + mapName + ".CanPaintBarrierBlock");
            
            
            if(conf.getMapConfig().contains("Maps." + mapName + ".Wiremesh")){
                boolean trapDoor = false;
                if(conf.getMapConfig().contains("Maps." + mapName + ".Wiremesh.TrapDoor"))
                    trapDoor = conf.getMapConfig().getBoolean("Maps." + mapName + ".Wiremesh.TrapDoor");
                boolean ironBars = false;
                if(conf.getMapConfig().contains("Maps." + mapName + ".Wiremesh.IronBars"))
                    ironBars = conf.getMapConfig().getBoolean("Maps." + mapName + ".Wiremesh.IronBars");
                boolean fence = false;
                if(conf.getMapConfig().contains("Maps." + mapName + ".Wiremesh.Fence"))
                    fence = conf.getMapConfig().getBoolean("Maps." + mapName + ".Wiremesh.Fence");
                
                double flocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.From.X") + 0.5;
                double flocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.From.Y");
                double flocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.From.Z") + 0.5;
                Location from = new Location(world, flocx, flocy, flocz);
                
                double tolocx = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.To.X") + 0.5;
                double tolocy = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.To.Y");
                double tolocz = conf.getMapConfig().getDouble("Maps." + mapName + ".Wiremesh.To.Z") + 0.5;
                Location to = new Location(world, tolocx, tolocy, tolocz);
                
                WiremeshListTask wmListTask = new WiremeshListTask(from, to, trapDoor, ironBars, fence);
                map.setWiremeshListTask(wmListTask);
            }
    
            if(conf.getMapConfig().contains("Maps." + mapName + ".VoidY")){
                map.setVoidY(conf.getMapConfig().getInt("Maps." + mapName + ".VoidY"));
            }
            
            
            map.setIntro(introLocation);
            map.setTeam0Loc(team0Location);
            map.setTeam1Loc(team1Location);
            map.setTeam0Intro(team0IntroLocation);
            map.setTeam1Intro(team1IntroLocation);
            map.setResultLoc(resultLocation);
            map.setTaikibasyo(waitLocation);
            map.setNoBlockLocation(noBlockLocation);
            
            map.setIntroMoveX(intromovex);
            map.setIntroMoveY(intromovey);
            map.setIntroMoveZ(intromovez);
            
            
            map.setCanPaintBBlock(canpaintbblock);
            
            //Main.getPlugin().getServer().createWorld(new WorldCreator(WorldName));
            
            Main.getPlugin().getLogger().info(mapName);
            
            map.setWorldName(WorldName);
            
            allMapCount++;
            
            //DataMgr.setMap(mapname, map);
            DataMgr.addMapList(map);
        }
    }
}
