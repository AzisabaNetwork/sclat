
package be4rjp.sclat.api;

import be4rjp.sclat.Sclat;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

/**
 *
 * @author Be4rJP
 */
public class BungeeCordAPI {
	public static void PlayerSendServer(Player player, String servername) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(servername);
		player.sendPluginMessage(Sclat.getPlugin(), "BungeeCord", out.toByteArray());
	}
}
