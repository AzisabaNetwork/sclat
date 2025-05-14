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
                ConfigurationSection pathSection = conf.getMapConfig().getConfigurationSection("Maps." + mapName + ".Path");
                assert pathSection != null;
                for (String pathname : pathSection.getKeys(false)){
                    Location from = ConfigUtil.getLocation(pathSection, world, pathname + ".From");
                    Location to = ConfigUtil.getLocation(pathSection, world, pathname + ".To");

                    Path path = new Path(from, to);
                    map.addPath(path);
                }
            }
            
            if(conf.getMapConfig().contains("Maps." + mapName + ".Area")){
                ConfigurationSection areaSection = conf.getMapConfig().getConfigurationSection("Maps." + mapName + ".Area");
                assert areaSection != null;
                for (String Areaname : areaSection.getKeys(false)){
                    map.setCanAreaBattle(true);

                    Location from = ConfigUtil.getLocation(areaSection, world, Areaname + ".From");
                    from.add(0.5, 0.5, 0.5);

                    Location to = ConfigUtil.getLocation(areaSection, world, Areaname + ".To");
                    to.add(0.5,0.5,0.5);

                    Area area = new Area(from, to);
                    map.addArea(area);
                }
            }
            
            boolean canpaintbblock = conf.getMapConfig().getBoolean("Maps." + mapName + ".CanPaintBarrierBlock", false);
            
            
            if(conf.getMapConfig().contains("Maps." + mapName + ".Wiremesh")){
                ConfigurationSection wiremeshSection = conf.getMapConfig().getConfigurationSection("Maps." + mapName + ".Wiremesh");
                assert wiremeshSection != null;

                boolean trapDoor = wiremeshSection.getBoolean("TrapDoor", false);
                boolean ironBars = wiremeshSection.getBoolean("IronBars", false);
                boolean fence = wiremeshSection.getBoolean("Fence", false);

                Location from = ConfigUtil.getLocation(wiremeshSection, world, "From");
                from.add(0.5, 0.0, 0.5);

                Location to = ConfigUtil.getLocation(wiremeshSection, world, "To");
                to.add(0.5,0.0,0.5);

                
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
