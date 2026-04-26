package net.azisaba.sclat.core.shape

import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * @author Be4rJP
 */
object Sphere {
    @JvmStatic
    fun getSphere(
        baseLoc: Location,
        r: Double,
        accuracy: Int,
    ): MutableList<Location> {
        val tempList: MutableList<Location> = mutableListOf()
        var count = 0
        var i = 0
        while (i < 180) {
            var t = 0
            while (t < 360) {
                var s = 1
                if (count % 2 == 0) s = -1
                val x = r * cos(Math.toRadians(i.toDouble())) * cos(Math.toRadians(t.toDouble())) * s
                val y = r * cos(Math.toRadians(i.toDouble())) * sin(Math.toRadians(t.toDouble())) * s
                val z = r * sin(Math.toRadians(i.toDouble())) * s
                val sphereLoc =
                    Location(
                        baseLoc.world,
                        baseLoc.x + x,
                        baseLoc.y + y,
                        baseLoc.z + z,
                    )
                tempList.add(sphereLoc)
                count++
                t += accuracy
            }
            i += accuracy
        }
        return tempList
    }

    @JvmStatic
    fun getXZCircle(
        baseLoc: Location,
        r: Double,
        rAccuracy: Double,
        accuracy: Int,
    ): MutableList<Location> {
        val tempList: MutableList<Location> = mutableListOf()
        var tr = 1
        while (tr <= r) {
            var t = 0
            while (t < 360) {
                val x = tr * sin(Math.toRadians(t.toDouble()))
                val z = tr * cos(Math.toRadians(t.toDouble()))
                val loc = Location(baseLoc.world, baseLoc.x + x, baseLoc.y, baseLoc.z + z)
                tempList.add(loc)
                t += accuracy / tr
            }
            tr = (tr + rAccuracy).toInt()
        }
        return tempList
    }
}
