package be4rjp.sclat.api.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EntityPackets {
	public static boolean sendDestroyEntities(Player player, int... entityIds) {
		PacketContainer destroyPacket = Packets.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		destroyPacket.getIntegerArrays().write(0, entityIds);
		return Packets.sendServerPacket(player, destroyPacket);
	}
}
