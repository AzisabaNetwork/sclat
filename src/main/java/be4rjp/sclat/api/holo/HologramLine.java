package be4rjp.sclat.api.holo;

import be4rjp.sclat.api.packet.Packets;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HologramLine {
	private final int entityId;
	private final UUID uuid;
	private final Location location;
	private String text;
	private boolean visible = true;

	public HologramLine(Location loc, String text) {
		this.entityId = ThreadLocalRandom.current().nextInt(100000, 999999);
		this.uuid = UUID.randomUUID();
		this.location = loc;
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public int getEntityId() {
		return entityId;
	}

	public void sendSpawn(Player player) {
		// Spawn Packet (1.14.4 ArmorStand ID is 1)
		PacketContainer spawn = Packets.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		spawn.getIntegers().write(0, entityId);
		spawn.getUUIDs().write(0, uuid);
		spawn.getIntegers().write(1, 1);
		spawn.getDoubles().write(0, location.getX()).write(1, location.getY()).write(2, location.getZ());

		// Metadata Packet
		PacketContainer meta = Packets.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		meta.getIntegers().write(0, entityId);

		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20); // Invisible
		watcher.setObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true),
				Optional.of(WrappedChatComponent.fromText(text).getHandle()));
		watcher.setObject(3, WrappedDataWatcher.Registry.get(Boolean.class), visible);
		watcher.setObject(14, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01); // Small

		meta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

		Packets.sendServerPacket(player, spawn);
		Packets.sendServerPacket(player, meta);
	}

	public void sendDestroy(Player player) {
		PacketContainer destroy = ProtocolLibrary.getProtocolManager()
				.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		destroy.getIntegerArrays().write(0, new int[]{entityId});
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
