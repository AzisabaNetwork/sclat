package be4rjp.sclat.manager;

import be4rjp.sclat.api.player.PlayerSettings;
import be4rjp.sclat.data.DataMgr;
import org.bukkit.entity.Player;

import static be4rjp.sclat.Sclat.conf;

public class SettingMgr {

	public static void setSettings(PlayerSettings settings, Player player) {
		String uuid = player.getUniqueId().toString();
		String def = "011111111";
		String settingStr = conf.getPlayerSettings().getString("Settings." + uuid);
		if (settingStr != null) {
			char[] options = settingStr.toCharArray();
			if (options[0] == '0')
				settings.S_PlayBGM();
			if (options[1] == '0')
				settings.S_ShowEffect_MainWeaponInk();
			if (options[2] == '0')
				settings.S_ShowEffect_ChargerLine();
			if (options[3] == '0')
				settings.S_ShowEffect_SPWeapon();
			if (options[4] == '0')
				settings.S_ShowEffect_SPWeaponRegion();
			if (options[5] == '0')
				settings.S_ShowSnowBall();
			if (options[6] == '0')
				settings.S_ShowEffect_Bomb();
			if (options[7] == '0')
				settings.S_ShowEffect_BombEx();
			if (options[8] == '0')
				settings.S_doChargeKeep();
		} else {
			conf.getPlayerSettings().set("Settings." + uuid, def);
			settings.S_PlayBGM();
		}
		DataMgr.getPlayerData(player).setSettings(settings);
	}
}
