package be4rjp.sclat;

import java.io.File;
import java.io.IOException;

import static org.bukkit.Bukkit.getLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Be4rJP
 */
public class Config {
    private FileConfiguration ps;
    private FileConfiguration conf;
    private FileConfiguration weapon;
    private FileConfiguration map;
    private FileConfiguration playersettings;
    private FileConfiguration as;
    private FileConfiguration s;
    private FileConfiguration servers;
    private FileConfiguration idCash;
    private FileConfiguration emblemItems;
    private FileConfiguration emblemUserdata;
    private File parent = new File("plugins/Sclat");
    private File psf = new File(parent, "class.yml");
    private File weaponf = new File(parent, "mainnweapon.yml");
    private File mapf = new File(parent, "maps.yml");
    private File conff = new File(parent, "config.yml");
    private File playersettings_f = new File(parent, "settings.yml");
    private File asf = new File(parent, "armorstand.yml");
    private File sf = new File(parent, "status.yml");
    private File serverFile = new File(parent, "servers.yml");
    private File idCashFile = new File(parent, "UUIDCash.yml");
    public File emblemsFile = new File(parent, "emblems.yml");
    private File emblemItemsFile = new File(parent, "emblem_items.yml");
    private File emblemUserDataFile = new File(parent, "emblem_userdata.yml");
    
    public synchronized void LoadConfig(){
        ps = YamlConfiguration.loadConfiguration(psf);
        conf = YamlConfiguration.loadConfiguration(conff);
        weapon = YamlConfiguration.loadConfiguration(weaponf);
        map = YamlConfiguration.loadConfiguration(mapf);
        playersettings = YamlConfiguration.loadConfiguration(playersettings_f);
        as = YamlConfiguration.loadConfiguration(asf);
        s = YamlConfiguration.loadConfiguration(sf);
        servers = YamlConfiguration.loadConfiguration(serverFile);
        idCash = YamlConfiguration.loadConfiguration(idCashFile);
        tryCreateFile(emblemItemsFile);
        tryCreateFile(emblemUserDataFile);
        loadEmblemUserData();
        loadEmblemLoreData();
    }

    public synchronized void loadEmblemUserData() {
        emblemUserdata = YamlConfiguration.loadConfiguration(emblemUserDataFile);
    }

    public synchronized void loadEmblemLoreData() {
        emblemItems = YamlConfiguration.loadConfiguration(emblemItemsFile);
    }

    private void tryCreateFile(File targetFile) {
        try {
            if(!targetFile.exists()) targetFile.createNewFile();
        } catch (IOException e) {
            getLogger().warning("Failed to create file: " + e);
        }
    }

    public synchronized void SaveConfig(){
        try{
            playersettings.save(playersettings_f);
            s.save(sf);
            idCash.save(idCashFile);
            saveEmblemUserdata();
        }catch(Exception e){
            getLogger().warning("Failed to save config files!");
        }
    }

    public synchronized void saveEmblemUserdata() throws IOException {
        emblemUserdata.save(emblemUserDataFile);
    }
    
    public FileConfiguration getConfig(){
        return conf;
    }
    
    public FileConfiguration getClassConfig(){
        return ps;
    }
    
    public FileConfiguration getWeaponConfig(){
        return weapon;
    }
    
    public FileConfiguration getMapConfig(){
        return map;
    }
    
    public FileConfiguration getPlayerSettings(){
        return playersettings;
    }
    
    public FileConfiguration getArmorStandSettings(){
        return as;
    }
    
    public FileConfiguration getPlayerStatus(){
        return s;
    }
    
    public FileConfiguration getServers(){
        return servers;
    }
    
    public FileConfiguration getUUIDCash(){
        return idCash;
    }

    public FileConfiguration getEmblemUserdata() {
        return emblemUserdata;
    }

    public FileConfiguration getEmblemItems() {
        return emblemItems;
    }
}
