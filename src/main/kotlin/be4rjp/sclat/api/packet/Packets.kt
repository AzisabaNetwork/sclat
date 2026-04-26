package be4rjp.sclat.api.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.FieldAccessException
import net.azisaba.sclat.core.DelegatedLogger
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked
import java.lang.reflect.InvocationTargetException

@NullMarked
object Packets {
    private val logger by DelegatedLogger()

    val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

    fun createPacket(packetType: PacketType): PacketContainer = protocolManager.createPacket(packetType)

    fun createPacket(
        packetType: PacketType,
        b: Boolean,
    ): PacketContainer = protocolManager.createPacket(packetType, b)

    fun sendServerPacket(
        player: Player,
        packet: PacketContainer,
    ): Boolean {
        try {
            protocolManager.sendServerPacket(player, packet)
            return true
        } catch (e: InvocationTargetException) {
            logger.error("Failed to send packet", e)
            return false
        }
    }

    fun broadcastServerPacket(packet: PacketContainer): Boolean =
        try {
            protocolManager.broadcastServerPacket(packet)
            true
        } catch (e: FieldAccessException) {
            logger.error("Failed to broadcast packet because of reflection", e)
            false
        }
}
