package be4rjp.sclat.api

import org.bukkit.ChatColor

/**
 *
 * @author Be4rJP
 */
object GaugeAPI {
    @JvmStatic
    fun toGauge(
        value: Int,
        max: Int,
        color1: String?,
        color2: String?,
    ): String {
        val m = "|"
        val ms = StringBuilder()
        ms.append(ChatColor.RESET).append(color1)
        for (i in 1..value) {
            ms.append(m)
        }
        ms.append(color2)
        val rem = max - value
        for (i1 in 1..rem) {
            ms.append(m)
        }
        ms.append(ChatColor.RESET)
        return ms.toString()
    }
}
