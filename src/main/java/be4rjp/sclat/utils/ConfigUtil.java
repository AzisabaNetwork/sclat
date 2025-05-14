package be4rjp.sclat.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigUtil {
    public static Location getLocation(ConfigurationSection section, World world, String path) {
        return new Location(
                world,
                section.getDouble(path + ".X"),
                section.getDouble(path + ".Y"),
                section.getDouble(path + ".Z")
        );
    }

    public static Location getLocationWithYaw(ConfigurationSection section, World world, String path) {
        Location loc = getLocation(section, world, path);
        loc.setYaw(section.getInt(path + ".Yaw"));
        return loc;
    }
}
