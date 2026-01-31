package be4rjp.sclat.data

import be4rjp.sclat.api.MineStat
import be4rjp.sclat.plugin
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.scheduler.BukkitRunnable

class ServerStatus(
    @JvmField val serverName: String?,
    displayName: String,
    host: String?,
    port: Int,
    maxPlayer: Int,
    period: Int,
    sign: Block,
    info: String?,
) {
    @JvmField
    val displayName: String?
    private val host: String?
    private val port: Int
    private val period: Int
    private val task: BukkitRunnable

    @JvmField
    val maxPlayer: Int
    private val task2: BukkitRunnable

    @JvmField
    val sign: Block?

    @JvmField
    val info: String?

    var playerCount: Int = 0
        private set
    var isOnline: Boolean = false
        private set
    var runningMatch: Boolean = false
    var restartingServer: Boolean = false

    @JvmField
    var mapName: String? = ""
    var isMaintenance: Boolean = false
    val uUIDList: MutableList<String?>?

    @JvmField
    var waitingEndTime: Long = 0

    @JvmField
    var matchStartTime: Long = 0

    private val matchServerRunnable: MatchServerRunnable?

    init {
        this.displayName = displayName
        this.host = host
        this.port = port
        this.period = period
        this.maxPlayer = maxPlayer
        this.sign = sign
        this.info = info
        this.uUIDList = ArrayList<String?>()

        this.matchServerRunnable = MatchServerRunnable(this)

        this.task =
            object : BukkitRunnable() {
                override fun run() {
                    if (isMaintenance) {
                        isOnline = false
                    } else {
                        try {
                            // Todo: migrate to PluginMessaging
                            val ms = MineStat(host, port)
                            playerCount = ms.currentPlayers!!.toInt()
                            isOnline = ms.isServerUp
                            if (!isOnline) {
                                runningMatch = false
                            }
                        } catch (e: Exception) {
                            isOnline = false
                            runningMatch = false
                        }
                    }
                }
            }
        task.runTaskTimerAsynchronously(plugin, 0, this.period.toLong())

        this.task2 =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        if (sign.type.toString().contains("SIGN")) {
                            val signState = sign.state as Sign
                            signState.setLine(0, displayName)
                            if (isOnline) {
                                signState.setLine(1, "§a" + playerCount + " / " + maxPlayer)
                                if (runningMatch) {
                                    signState.setLine(2, "§cIN MATCH")
                                } else {
                                    signState.setLine(2, "§aINACTIVE")
                                }
                                signState.setLine(3, "§b" + mapName)
                            } else {
                                signState.setLine(1, if (isMaintenance) "§cMAINTENANCE" else "§cOFFLINE")
                                signState.setLine(2, "")
                                signState.setLine(3, "")
                            }
                            signState.update()
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        task2.runTaskTimer(plugin, 5, this.period.toLong())
    }

    fun stopTask() {
        this.task.cancel()
    }
}
