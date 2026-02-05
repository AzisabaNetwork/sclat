package be4rjp.sclat.api.packet

import be4rjp.sclat.Sclat
import be4rjp.sclat.sclatLogger
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked
import java.lang.reflect.InvocationTargetException

@NullMarked
object Packets {
    fun manager(): ProtocolManager = Sclat.protocolManager

    fun createPacket(packetType: PacketType): PacketContainer = manager().createPacket(packetType)

    fun createPacket(
        packetType: PacketType,
        b: Boolean,
    ): PacketContainer = manager().createPacket(packetType, b)

    fun sendServerPacket(
        player: Player,
        packet: PacketContainer,
    ): Boolean {
        try {
            manager().sendServerPacket(player, packet)
            return true
        } catch (e: InvocationTargetException) {
            sclatLogger.error("Failed to send packet", e)
            return false
        }
    }

    fun broadcastServerPacket(packet: PacketContainer): Boolean {
        try {
            manager().broadcastServerPacket(packet)
            return true
        } catch (e: Exception) {
            sclatLogger.error("Failed to broadcast packet", e)
            return false
        }
    }
}
