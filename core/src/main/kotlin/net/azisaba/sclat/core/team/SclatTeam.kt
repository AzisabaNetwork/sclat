package net.azisaba.sclat.core.team

import net.azisaba.sclat.core.data.Color
import org.bukkit.scoreboard.Team

/**
 *
 * @author Be4rJP
 */
class SclatTeam(
    val iD: Int,
) {
    var teamColor: Color? = null
    private val mapname: String? = null
    var point: Int = 0
        private set

    @JvmField
    var killCount: Int = 0
    var team: Team? = null
    var gatiCount: Int = 0
        private set
    var rateTotal: Int = 0
        private set

    fun addPaintCount() {
        this.point++
    }

    fun subtractPaintCount() {
        this.point--
    }

    fun subtractRateTotal(rate: Int) {
        this.rateTotal -= rate
    }

    fun addKillCount() {
        this.killCount++
    }

    fun addGatiCount() {
        this.gatiCount++
    }

    fun addRateTotal(rate: Int) {
        this.rateTotal += rate
    }
}
