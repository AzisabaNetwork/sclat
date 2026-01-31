package be4rjp.sclat.api.packet

import be4rjp.sclat.data.DataMgr
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedBlockData
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

object WorldPackets {
    @JvmStatic
    fun broadcastBlockChange(
        location: Location,
        material: Material?,
    ): Boolean {
        val packet = Packets.createPacket(PacketType.Play.Server.BLOCK_CHANGE)
        packet.blockPositionModifier.write(0, BlockPosition(location.toVector()))
        packet.blockData.write(0, WrappedBlockData.createData(material))
        return Packets.broadcastServerPacket(packet)
    }

    @Suppress("DEPRECATION")
    @JvmStatic
    fun sendWorldBorderWarningPacket(player: Player): Boolean {
        val packet = Packets.createPacket(PacketType.Play.Server.WORLD_BORDER)

        // Set the action to INITIALIZE (Enum index 3 in 1.14.4)
        packet.worldBorderActions.write(0, EnumWrappers.WorldBorderAction.INITIALIZE)

        // Set Center (far away from the player to trigger the warning)
        packet.doubles.write(0, player.location.x + 10000.0) // Center X
        packet.doubles.write(1, player.location.z + 10000.0) // Center Z

        // Set Sizes
        packet.doubles.write(2, 0.0) // Old radius
        packet.doubles.write(3, 1.0) // New radius (Size 1)

        // Set Lerp Speed (Time to reach new radius)
        packet.longs.write(0, 0L)

        // Set Other Required Data for INITIALIZE
        packet.integers.write(0, 29999984) // Portal Teleport Boundary
        packet.integers.write(1, 5) // Warning Time
        packet.integers.write(2, 5) // Warning Distance

        return Packets.sendServerPacket(player, packet)
    }

    @Suppress("DEPRECATION")
    @JvmStatic
    fun sendWorldBorderWarningClearPacket(player: Player): Boolean {
        val packet = Packets.createPacket(PacketType.Play.Server.WORLD_BORDER)

        // Set the action to INITIALIZE
        packet.worldBorderActions.write(0, EnumWrappers.WorldBorderAction.INITIALIZE)

        // Set Center to player's current position so they are in the middle
        packet.doubles.write(0, player.location.x) // Center X
        packet.doubles.write(1, player.location.z) // Center Z

        // Set Sizes to maximum (30 million is the Minecraft default limit)
        packet.doubles.write(2, 30000000.0) // Old radius
        packet.doubles.write(3, 30000000.0) // New radius

        // Set Lerp Speed
        packet.longs.write(0, 0L)

        // Set Required Constants
        packet.integers.write(0, 29999984) // Portal boundary
        packet.integers.write(1, 5) // Warning time
        packet.integers.write(2, 5) // Warning distance

        return Packets.sendServerPacket(player, packet)
    }

    @JvmStatic
    fun setPlayerFOV(
        player: Player,
        fov: Float,
    ): Boolean {
        val packet = Packets.createPacket(PacketType.Play.Server.ABILITIES)

        // 1. Set the walk speed (which controls the FOV zoom/widening)
        // In 1.14.4, walkSpeed is usually the second float (index 1)
        packet.float.write(1, fov)

        // 2. Set the fly speed (index 0) to the player's current fly speed to avoid
        // bugs
        packet.float.write(0, player.flySpeed)

        // 3. Set the flags (invulnerable, flying, canFly, instabuild)
        // We use a bitmask or just mirror the player's current state
        var flags: Byte = 0
        if (player.allowFlight) flags = (flags.toInt() or 0x04).toByte()
        if (player.isFlying) flags = (flags.toInt() or 0x02).toByte()

        // (Add more flags if you need to maintain invulnerability or creative mode
        // status)
        packet.booleans.write(0, player.isInvulnerable)
        packet.booleans.write(1, player.isFlying)
        packet.booleans.write(2, player.allowFlight)
        packet.booleans.write(3, player.gameMode == GameMode.CREATIVE)

        // Update your local data manager
        DataMgr.getPlayerData(player)?.fov = fov

        return Packets.sendServerPacket(player, packet)
    }
}
