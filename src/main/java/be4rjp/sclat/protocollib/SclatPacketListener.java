package be4rjp.sclat.protocollib;

import be4rjp.sclat.Main;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;

public class SclatPacketListener {
	public static void init() {
		Main.protocolManager.addPacketListener(new VehiclePacketListener(Main.getPlugin(), ListenerPriority.NORMAL,
				PacketType.Play.Client.STEER_VEHICLE));

		Main.protocolManager
				.addPacketListener(new EntityClickListener(Main.getPlugin(), PacketType.Play.Client.USE_ENTITY));

		/*
		 * Main.protocolManager.addPacketListener( new PacketAdapter(Main.getPlugin(),
		 * PacketType.Play.Server.SPAWN_ENTITY){
		 * 
		 * @Override public void onPacketReceiving(PacketEvent event) {//雪玉のスポーンパケットを遮断
		 * final Player player = event.getPlayer(); if (event.getPacketType() ==
		 * PacketType.Play.Server.SPAWN_ENTITY) { final PacketContainer packet =
		 * event.getPacket();
		 * 
		 * packet.g
		 * 
		 * if(packet.getEntityTypeModifier().readSafely(0) == EntityType.SNOWBALL){
		 * if(!DataMgr.getPlayerData(player).getSettings().ShowSnowBall())
		 * event.setCancelled(true); } } } });
		 * 
		 */
	}
}
