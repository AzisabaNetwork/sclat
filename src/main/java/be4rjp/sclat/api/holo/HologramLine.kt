package be4rjp.sclat.api.holo

import be4rjp.sclat.api.packet.Packets
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class HologramLine(
    private val location: Location,
    private var text: String,
) {
    val entityId: Int
    private val uuid: UUID
    private var visible = true

    init {
        this.entityId = ThreadLocalRandom.current().nextInt(100000, 999999)
        this.uuid = UUID.randomUUID()
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    fun sendSpawn(player: Player) {
        // Spawn Packet (1.14.4 ArmorStand ID is 1)
        val spawn = Packets.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING)
        spawn.getIntegers().write(0, entityId)
        spawn.getUUIDs().write(0, uuid)
        spawn.getIntegers().write(1, 1)
        spawn
            .getDoubles()
            .write(0, location.getX())
            .write(1, location.getY())
            .write(2, location.getZ())

        // Metadata Packet
        val meta = Packets.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        meta.getIntegers().write(0, entityId)

        val watcher = WrappedDataWatcher()
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte::class.java), 0x20.toByte()) // Invisible
        watcher.setObject(
            2,
            WrappedDataWatcher.Registry.getChatComponentSerializer(true),
            Optional.of<Any?>(WrappedChatComponent.fromText(text).getHandle()),
        )
        watcher.setObject(3, WrappedDataWatcher.Registry.get(Boolean::class.java), visible as Any)
        watcher.setObject(14, WrappedDataWatcher.Registry.get(Byte::class.java), 0x01.toByte()) // Small

        meta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects())

        Packets.sendServerPacket(player, spawn)
        Packets.sendServerPacket(player, meta)
    }

    fun sendDestroy(player: Player?) {
        val destroy =
            ProtocolLibrary
                .getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        destroy.getIntegerArrays().write(0, intArrayOf(entityId))
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
