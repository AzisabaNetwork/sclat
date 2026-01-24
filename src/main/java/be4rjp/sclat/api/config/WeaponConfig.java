package be4rjp.sclat.api.config;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.WeaponClass;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class WeaponConfig {
    // Todo: needs more refactorization
    public static WeaponClass parseSection(String className, ConfigurationSection section) {
        List<String> missingProperties = new ArrayList<>();
        String weaponName = getStringOrRecord(section, "MainWeaponName", missingProperties);
        String subWeaponName = getStringOrRecord(section, "SubWeaponName", missingProperties);
        String spWeaponName = getStringOrRecord(section,"SPWeaponName", missingProperties);
        if(!missingProperties.isEmpty()) {
            Sclat.logger.warn("Missing weapon properties in {}: [{}]", className, String.join(", ", missingProperties));
        }

        WeaponClass wc = new WeaponClass(className);
        wc.setMainWeapon(DataMgr.getWeapon(weaponName));
        wc.setSubWeaponName(subWeaponName);
        wc.setSPWeaponName(spWeaponName);
        return wc;
    }

    @Nullable
    private static String getStringOrRecord(ConfigurationSection section, String path, List<String> missingList) {
        String result = section.getString(path);
        if(result == null) {
            missingList.add(path);
        }
        return result;
    }
}
