package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerSettings
import be4rjp.sclat.data.DataMgr.getPlayerData
import org.bukkit.entity.Player

object SettingMgr {
    fun setSettings(settings: PlayerSettings, player: Player) {
        val uuid: String? = player.getUniqueId().toString()
        val def = "011111111"
        if (Sclat.Companion.conf!!.playerSettings.contains("Settings." + uuid)) {
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(1) == '0'
            ) {
                settings.S_ShowEffect_MainWeaponInk()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(2) == '0'
            ) {
                settings.S_ShowEffect_ChargerLine()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(3) == '0'
            ) {
                settings.S_ShowEffect_SPWeapon()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(4) == '0'
            ) {
                settings.S_ShowEffect_SPWeaponRegion()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(5) == '0'
            ) {
                settings.S_ShowSnowBall()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(0) == '0'
            ) {
                settings.S_PlayBGM()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(6) == '0'
            ) {
                settings.S_ShowEffect_Bomb()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(7) == '0'
            ) {
                settings.S_ShowEffect_BombEx()
            }
            if (Sclat.Companion.conf!!.playerSettings.getString("Settings." + uuid)!!
                    .get(8) == '0'
            ) {
                settings.S_doChargeKeep()
            }
        } else {
            Sclat.Companion.conf!!.playerSettings.set("Settings." + uuid, def)
            settings.S_PlayBGM()
        }
        getPlayerData(player)!!.settings = settings
    }
}
