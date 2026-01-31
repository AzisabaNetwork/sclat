package be4rjp.sclat.api.packet

import be4rjp.sclat.Sclat
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException

@NullMarked
object Packets {
    private val logger: Logger = LoggerFactory.getLogger(EntityPackets::class.java)

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
            logger.error("Failed to send packet", e)
            return false
        }
    }

    fun broadcastServerPacket(packet: PacketContainer): Boolean {
        try {
            manager().broadcastServerPacket(packet)
            return true
        } catch (e: Exception) {
            logger.error("Failed to broadcast packet", e)
            return false
        }
    }
}
