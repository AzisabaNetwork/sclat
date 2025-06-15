package be4rjp.sclat.manager;

import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerSettings;
import org.bukkit.entity.Player;

import static be4rjp.sclat.Main.conf;

public class SettingMgr {

    public static void setSettings(PlayerSettings settings, Player player) {
        String uuid = player.getUniqueId().toString();
        String def = "011111111";
        if (conf.getPlayerSettings().contains("Settings." + uuid)) {
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(1) == '0')
                settings.S_ShowEffect_MainWeaponInk();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(2) == '0')
                settings.S_ShowEffect_ChargerLine();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(3) == '0')
                settings.S_ShowEffect_SPWeapon();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(4) == '0')
                settings.S_ShowEffect_SPWeaponRegion();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(5) == '0')
                settings.S_ShowSnowBall();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(0) == '0')
                settings.S_PlayBGM();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(6) == '0')
                settings.S_ShowEffect_Bomb();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(7) == '0')
                settings.S_ShowEffect_BombEx();
            if (conf.getPlayerSettings().getString("Settings." + uuid).charAt(8) == '0')
                settings.S_doChargeKeep();
        } else {
            conf.getPlayerSettings().set("Settings." + uuid, def);
            settings.S_PlayBGM();
        }
        DataMgr.getPlayerData(player).setSettings(settings);
    }
}
