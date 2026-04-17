package be4rjp.sclat.protocollib

import be4rjp.sclat.data.DataMgr.getPlayerData
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

class VehiclePacketListener(
    plugin: Plugin,
    vararg types: PacketType,
) : PacketAdapter(plugin, *types) {
    override fun onPacketReceiving(event: PacketEvent) { // プレイヤーがエンティティに乗っているときのパケットを監視
        val player = event.player
        if (event.packetType === PacketType.Play.Client.STEER_VEHICLE && player.vehicle != null) {
            event.packet

            val z = event.packet.float.readSafely(0)
            val x = event.packet.float.readSafely(1)

            var y = 0f

            try {
                if (event.packet.booleans.readSafely(1)) {
                    y = -1f
                    if (getPlayerData(player)!!.isInMatch) event.isCancelled = true
                }
                if (event.packet.booleans.readSafely(0)) {
                    y = 1f
                }
            } catch (e45: Error) {
            } catch (e45: Exception) {
            }

            val vec = Vector(x, y, z)
            getPlayerData(player)!!.vehicleVector = vec
        }
    }
}
