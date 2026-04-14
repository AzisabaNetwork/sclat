package be4rjp.sclat.weapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
object Gear {
    @JvmStatic
    fun getGearMaterial(gearN: Int): Material {
        when (gearN) {
            1 -> return Material.INK_SAC
            2 -> return Material.GOLDEN_BOOTS
            3 -> return Material.IRON_HOE
            4 -> return Material.WHITE_STAINED_GLASS
            5 -> return Material.WATER_BUCKET
            6 -> return Material.LAVA_BUCKET
            7 -> return Material.PRISMARINE_SHARD
            8 -> return Material.GOLDEN_APPLE
            9 -> return Material.APPLE
            else -> return Material.IRON_BARS
        }
    }

    @JvmStatic
    fun getGearName(gearN: Int): String {
        when (gearN) {
            1 -> return "イカダッシュ速度アップ"
            2 -> return "ヒト移動速度アップ"
            3 -> return "メイン性能アップ"
            4 -> return "サブ性能アップ"
            5 -> return "インク回復量アップ"
            6 -> return "メインインク効率アップ"
            7 -> return "スペシャル増加量アップ"
            8 -> return "最大体力アップ"
            9 -> return "ペナルティ軽減"
            else -> return "ギアなし"
        }
    }

    @JvmStatic
    fun getGearPrice(gearN: Int): Int {
        when (gearN) {
            1 -> return 65000
            2 -> return 60000
            3 -> return 120000
            4 -> return 55000
            5 -> return 50000
            6 -> return 100000
            7 -> return 200000
            8 -> return 110000
            9 -> return 10
            else -> return 0
        }
    }

    @JvmStatic
    fun getGearInfluence(
        player: Player?,
        gearN: Int,
    ): Double {
        if (getPlayerData(player)!!.gearNumber == gearN) {
            when (getPlayerData(player)!!.gearNumber) {
                1 -> return 1.1
                2 -> return 1.3
                3 -> return 1.1
                4 -> return 1.2
                5 -> return 1.15
                6 -> return 1.1
                7 -> return 1.3
                8 -> return 1.2
                9 -> return 1.2
                else -> return 1.0
            }
        } else {
            return 1.0
        }
    }

    object Type {
        const val NO_GEAR: Int = 0
        const val IKA_SPEED_UP: Int = 1
        const val HITO_SPEED_UP: Int = 2
        const val MAIN_SPEC_UP: Int = 3
        const val SUB_SPEC_UP: Int = 4
        const val INK_RECOVERY_UP: Int = 5
        const val MAIN_INK_EFFICIENCY_UP: Int = 6
        const val SPECIAL_UP: Int = 7
        const val MAX_HEALTH_UP: Int = 8
        const val PENA_DOWN: Int = 9
    }
}
