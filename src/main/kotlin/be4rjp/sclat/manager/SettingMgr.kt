package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerSettings
import be4rjp.sclat.data.DataMgr.getPlayerData
import org.bukkit.entity.Player

object SettingMgr {
    const val DEFAULT_SETTING_STRING = "011111111"

    fun setSettings(
        settings: PlayerSettings,
        player: Player,
    ) {
        val uuid: String = player.uniqueId.toString()
        val settingString =
            Sclat.conf!!
                .playerSettings
                .getString("Settings.$uuid") ?: run {
                DEFAULT_SETTING_STRING.also { it ->
                    Sclat.conf!!
                        .playerSettings
                        .set("Settings.$uuid", it)
                }
            }

        if (settingString[1] == '0') settings.sShowEffectMainWeaponInk()
        if (settingString[2] == '0') settings.sShowEffectChargerLine()
        if (settingString[3] == '0') settings.sShowEffectSPWeapon()
        if (settingString[4] == '0') settings.sShowEffectSPWeaponRegion()
        if (settingString[5] == '0') settings.sShowSnowBall()
        if (settingString[0] == '0') settings.sPlayBGM()
        if (settingString[6] == '0') settings.sShowEffectBomb()
        if (settingString[7] == '0') settings.sShowEffectBombEx()
        if (settingString[8] == '0') settings.sDoChargeKeep()

        getPlayerData(player)!!.settings = settings
    }
}
