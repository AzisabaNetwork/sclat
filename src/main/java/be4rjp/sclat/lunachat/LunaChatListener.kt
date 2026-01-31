package be4rjp.sclat.lunachat

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitPreChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 *
 * @author Be4rJP
 */
class LunaChatListener : Listener {
    // @EventHandler
    fun onChat(event: LunaChatBukkitPreChatEvent) {
        var sender: Player? = null
        for (player in plugin.server.onlinePlayers) {
            if (player.name == event.member.name) {
                sender = player
            }
        }
        if (sender != null) {
            val data = getPlayerData(sender)
            if (data!!.isJoined) event.message = data.team.teamColor!!.colorCode + event.message
        }
    }
}
