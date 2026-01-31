package be4rjp.sclat.lunachat

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
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
        for (player in Sclat.getPlugin().getServer().getOnlinePlayers()) {
            if (player.getName() == event.getMember().getName()) {
                sender = player
            }
        }
        if (sender != null) {
            val data = getPlayerData(sender)
            if (data!!.getIsJoined()) event.setMessage(data.team.teamColor!!.colorCode + event.getMessage())
        }
    }
}
