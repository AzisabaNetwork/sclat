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
        plugin.server.getPlayer(event.member.name)?.let {
            val data = getPlayerData(it)
            if (data!!.isJoined) event.message = data.team?.teamColor!!.colorCode + event.message
        }
    }
}
