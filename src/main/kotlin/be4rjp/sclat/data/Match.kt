package be4rjp.sclat.data

import net.azisaba.sclat.core.team.Team
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

/**
 *
 * @author Be4rJP
 */
class Match(
    private val id: Int,
) {
    val world: World? = null

    @JvmField
    var team0: Team? = null

    @JvmField
    var team1: Team? = null
    var playerCount: Int = 0
        private set
    var mapData: MapData? = null
    private var canjoin = true
    var leaderPlayer: Player? = null
    var nawabariTCount: Int = 0
        private set
    var tdmTCount: Int = 0
        private set
    var gatiareaTCount: Int = 0
        private set
    var isFinished: Boolean = false
    var blockUpdater: BlockUpdater? = null
    var winTeam: Team? = null

    @JvmField
    var isHikiwake: Boolean = false

    @JvmField
    var scoreboard: Scoreboard? = null

    @JvmField
    var isStarted: Boolean = false
    var joinedPlayerCount: Int = 0
        private set

    @JvmField
    var isStartedCount: Boolean = false

    fun canJoin(): Boolean = this.canjoin

    fun addPlayerCount() {
        this.playerCount++
    }

    fun setCanJoin(`is`: Boolean) {
        this.canjoin = `is`
    }

    fun addnawabariTCount() {
        this.nawabariTCount++
    }

    fun addtdmTCount() {
        this.tdmTCount++
    }

    fun addgatiareaTCount() {
        this.gatiareaTCount++
    }

    fun addJoinedPlayerCount() {
        this.joinedPlayerCount++
    }

    fun subPlayerCount() {
        this.playerCount--
    }

    fun subJoinedPlayerCount() {
        this.joinedPlayerCount--
    }
}
