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
 * @author Be4rJP, sysnote8
 */
public class MapDataMgr {
    
    public static int allMapCount = 0;
    
    public synchronized static void SetupMap(){
        ConfigurationSection mapsSection = conf.getMapConfig().getConfigurationSection("Maps");
        assert mapsSection != null;

        for (String mapName : mapsSection.getKeys(false)){
            ConfigurationSection eachMapSection = mapsSection.getConfigurationSection(mapName);
            assert eachMapSection != null;

            MapData map = new MapData(mapName);
            String WorldName = eachMapSection.getString("WorldName");
            assert WorldName != null;

            World world = getServer().getWorld(WorldName);

            Location introLocation = ConfigUtil.getLocationWithYaw(eachMapSection, world, "Intro");

            double intromovex = eachMapSection.getDouble("Intro.MoveX");
            double intromovey = eachMapSection.getDouble("Intro.MoveY");
            double intromovez = eachMapSection.getDouble("Intro.MoveZ");

            Location team0Location = ConfigUtil.getLocationWithYaw(eachMapSection, world, "Team0");
            team0Location.setX(team0Location.getX() + 0.5);
            team0Location.setZ(team0Location.getZ() + 0.5);

            Location team1Location = ConfigUtil.getLocationWithYaw(eachMapSection, world, "Team1");
            team1Location.setX(team1Location.getX() + 0.5);
            team1Location.setZ(team1Location.getZ() + 0.5);

            Location team0IntroLocation = ConfigUtil.getLocationWithYaw(eachMapSection, world, "Team0IntroLoc");
            Location team1IntroLocation = ConfigUtil.getLocationWithYaw(eachMapSection, world, "Team1IntroLoc");

            Location resultLocation = ConfigUtil.getLocationWithYaw(eachMapSection, world, "ResultLoc");
            resultLocation.setPitch(90);

            Location waitLocation = ConfigUtil.getLocation(eachMapSection, world, "WaitLoc");

            Location noBlockLocation = ConfigUtil.getLocation(eachMapSection, world, "NoBlockLoc");
            
            if(eachMapSection.contains("Path")){
                ConfigurationSection pathSection = eachMapSection.getConfigurationSection("Path");
                assert pathSection != null;
                for (String pathname : pathSection.getKeys(false)){
                    Location from = ConfigUtil.getLocation(pathSection, world, pathname + ".From");
                    Location to = ConfigUtil.getLocation(pathSection, world, pathname + ".To");

                    Path path = new Path(from, to);
                    map.addPath(path);
                }
            }
            
            if(eachMapSection.contains("Area")){
                ConfigurationSection areaSection = eachMapSection.getConfigurationSection("Area");
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
            
            boolean canPaintBBlock = eachMapSection.getBoolean("CanPaintBarrierBlock", false);
            
            
            if(eachMapSection.contains("Wiremesh")){
                ConfigurationSection wiremeshSection = eachMapSection.getConfigurationSection("Wiremesh");
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
    
            if(eachMapSection.contains("VoidY")){
                map.setVoidY(eachMapSection.getInt("VoidY"));
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
            
            
            map.setCanPaintBBlock(canPaintBBlock);
            
            Main.getPlugin().getLogger().info(mapName);
            
            map.setWorldName(WorldName);
            
            allMapCount++;

            DataMgr.addMapList(map);
        }
    }
}
