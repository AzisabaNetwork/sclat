package be4rjp.sclat.api

import be4rjp.sclat.Sclat
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException

object GlowingAPI {
    @JvmStatic
    fun setGlowing(
        entity: Entity,
        player: Player?,
        flag: Boolean,
    ) {
        val packet = Sclat.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        packet.getIntegers().write(0, entity.getEntityId())
        val watcher = WrappedDataWatcher()
        val serializer = WrappedDataWatcher.Registry.get(Byte::class.java)
        watcher.setEntity(entity)
        watcher.setObject(0, serializer, (if (flag) 0x40 else 0).toByte())
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects())
        try {
            Sclat.protocolManager.sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
