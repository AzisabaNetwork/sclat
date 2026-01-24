package be4rjp.sclat.api.packet;

import be4rjp.sclat.Sclat;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

@NullMarked
public class Packets {
	private static final Logger logger = LoggerFactory.getLogger(EntityPackets.class);
	public static ProtocolManager manager() {
		return Sclat.protocolManager;
	}

	public static PacketContainer createPacket(PacketType packetType) {
		return manager().createPacket(packetType);
	}

	public static PacketContainer createPacket(PacketType packetType, boolean b) {
		return manager().createPacket(packetType, b);
	}

	public static boolean sendServerPacket(Player player, PacketContainer packet) {
		try {
			manager().sendServerPacket(player, packet);
			return true;
		} catch (InvocationTargetException e) {
			logger.error("Failed to send packet", e);
			return false;
		}
	}

	public static boolean broadcastServerPacket(PacketContainer packet) {
		try {
			manager().broadcastServerPacket(packet);
			return true;
		} catch (Exception e) {
			logger.error("Failed to broadcast packet", e);
			return false;
		}
	}
}
