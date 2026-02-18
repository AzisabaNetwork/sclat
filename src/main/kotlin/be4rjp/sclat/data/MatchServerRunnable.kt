package be4rjp.sclat.data

import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.extension.broadcastMessage
import be4rjp.sclat.extension.component
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.server
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class MatchServerRunnable(
    private val serverStatus: ServerStatus,
) : BukkitRunnable() {
    private val waitTime = 0

    init {
        server.broadcastMessage(serverStatus.displayName + "§rの試合待機カウントダウンが開始されました")
        server.broadcastMessage(component("30秒後にマッチングを開始します", NamedTextColor.GREEN))
        server
            .onlinePlayers
            .forEach { player: Player ->
                playGameSound(player, SoundType.SUCCESS)
            }
    }

    override fun run() {
        if (waitTime == 30) {
            for (player in plugin.server.onlinePlayers) {
                BungeeCordMgr.playerSendServer(player, serverStatus.serverName!!)
                DataMgr.getPlayerData(player)?.setServerName(serverStatus.displayName)
            }
        }
    }
}
