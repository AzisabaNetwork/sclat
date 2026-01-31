package be4rjp.sclat.data

import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.plugin
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class MatchServerRunnable(
    private val serverStatus: ServerStatus,
) : BukkitRunnable() {
    private val waitTime = 0

    init {
        sendMessage(
            serverStatus.displayName + "§rの試合待機カウントダウンが開始されました",
            MessageType.ALL_PLAYER,
        )
        sendMessage("§a30秒後にマッチングを開始します", MessageType.ALL_PLAYER)
        plugin
            .getServer()
            .getOnlinePlayers()
            .forEach { player: Player -> playGameSound(player, SoundType.SUCCESS) }
    }

    override fun run() {
        if (waitTime == 30) {
            for (player in plugin.getServer().getOnlinePlayers()) {
                BungeeCordMgr.PlayerSendServer(player, serverStatus.serverName)
                DataMgr.getPlayerData(player)?.setServerName(serverStatus.displayName)
            }
        }
    }
}
