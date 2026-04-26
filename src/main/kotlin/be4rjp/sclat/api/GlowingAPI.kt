@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package be4rjp.sclat.api

import be4rjp.sclat.api.packet.Packets
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.lang.Byte
import java.lang.reflect.InvocationTargetException
import kotlin.Boolean
import kotlin.Suppress

object GlowingAPI {
    @JvmStatic
    fun setGlowing(
        entity: Entity,
        player: Player,
        flag: Boolean,
    ) {
        val packet = Packets.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        packet.integers.write(0, entity.entityId)
        val watcher = WrappedDataWatcher()
        val serializer = WrappedDataWatcher.Registry.get(Byte::class.java)
        watcher.entity = entity
        watcher.setObject(0, serializer, (if (flag) 0x40 else 0).toByte())
        packet.watchableCollectionModifier.write(0, watcher.watchableObjects)
        try {
            Packets.sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
