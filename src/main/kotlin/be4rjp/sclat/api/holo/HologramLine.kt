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
        @Suppress("DEPRECATION")
        val spawn = Packets.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING)
        spawn.integers.write(0, entityId)
        spawn.uuiDs.write(0, uuid)
        spawn.integers.write(1, 1)
        spawn
            .doubles
            .write(0, location.x)
            .write(1, location.y)
            .write(2, location.z)

        // Metadata Packet
        val meta = Packets.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        meta.integers.write(0, entityId)

        val watcher = WrappedDataWatcher()
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte::class.java), 0x20.toByte()) // Invisible
        watcher.setObject(
            2,
            WrappedDataWatcher.Registry.getChatComponentSerializer(true),
            Optional.of(WrappedChatComponent.fromText(text).getHandle()),
        )
        watcher.setObject(3, WrappedDataWatcher.Registry.get(Boolean::class.java), visible as Any)
        watcher.setObject(14, WrappedDataWatcher.Registry.get(Byte::class.java), 0x01.toByte()) // Small

        meta.watchableCollectionModifier.write(0, watcher.watchableObjects)

        Packets.sendServerPacket(player, spawn)
        Packets.sendServerPacket(player, meta)
    }

    fun sendDestroy(player: Player?) {
        val destroy =
            ProtocolLibrary
                .getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        destroy.integerArrays.write(0, intArrayOf(entityId))
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
