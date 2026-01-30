package be4rjp.sclat.api.packet

import com.comphenix.protocol.PacketType
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked

@NullMarked
object EntityPackets {
    @JvmStatic
    fun sendDestroyEntities(
        player: Player,
        vararg entityIds: Int,
    ): Boolean {
        val destroyPacket = Packets.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        destroyPacket.getIntegerArrays().write(0, entityIds)
        return Packets.sendServerPacket(player, destroyPacket)
    }
}
