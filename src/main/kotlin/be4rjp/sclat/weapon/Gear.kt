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
    fun getGearMaterial(gearN: Int): Material =
        when (gearN) {
            1 -> Material.INK_SAC
            2 -> Material.GOLDEN_BOOTS
            3 -> Material.IRON_HOE
            4 -> Material.WHITE_STAINED_GLASS
            5 -> Material.WATER_BUCKET
            6 -> Material.LAVA_BUCKET
            7 -> Material.PRISMARINE_SHARD
            8 -> Material.GOLDEN_APPLE
            9 -> Material.APPLE
            else -> Material.IRON_BARS
        }

    @JvmStatic
    fun getGearName(gearN: Int): String =
        when (gearN) {
            1 -> "イカダッシュ速度アップ"
            2 -> "ヒト移動速度アップ"
            3 -> "メイン性能アップ"
            4 -> "サブ性能アップ"
            5 -> "インク回復量アップ"
            6 -> "メインインク効率アップ"
            7 -> "スペシャル増加量アップ"
            8 -> "最大体力アップ"
            9 -> "ペナルティ軽減"
            else -> "ギアなし"
        }

    @JvmStatic
    fun getGearPrice(gearN: Int): Int =
        when (gearN) {
            1 -> 65000
            2 -> 60000
            3 -> 120000
            4 -> 55000
            5 -> 50000
            6 -> 100000
            7 -> 200000
            8 -> 110000
            9 -> 10
            else -> 0
        }

    @JvmStatic
    fun getGearInfluence(
        player: Player?,
        gearN: Int,
    ): Double {
        if (getPlayerData(player)!!.gearNumber == gearN) {
            return when (getPlayerData(player)!!.gearNumber) {
                1 -> 1.1
                2 -> 1.3
                3 -> 1.1
                4 -> 1.2
                5 -> 1.15
                6 -> 1.1
                7 -> 1.3
                8 -> 1.2
                9 -> 1.2
                else -> 1.0
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
