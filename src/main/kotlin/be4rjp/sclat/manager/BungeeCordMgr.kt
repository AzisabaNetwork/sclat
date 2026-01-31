package be4rjp.sclat.manager

import be4rjp.sclat.plugin
import com.google.common.io.ByteStreams
import org.bukkit.entity.Player

/**
 *
 * @author Be4rJP
 */
object BungeeCordMgr {
    @JvmStatic
    fun PlayerSendServer(player: Player, servername: String) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(servername)
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }
}
