package be4rjp.sclat.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigUtil {
    public static Location getLocation(ConfigurationSection section, String path, World world) {
        return new Location(
                world,
                section.getInt(path + ".X"),
                section.getInt(path + ".Y"),
                section.getInt(path + ".Z")
        );
    }

    public static Location getLocationWithYaw(ConfigurationSection section, String path, World world) {
        Location loc = getLocation(section, path, world);
        loc.setYaw(section.getInt(path + ".Yaw"));
        return loc;
    }
}
