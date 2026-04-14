package be4rjp.sclat.protocollib

import be4rjp.sclat.Sclat
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.Sound
import org.bukkit.plugin.Plugin

class EntityClickListener(
    plugin: Plugin?,
    vararg types: PacketType?,
) : PacketAdapter(plugin, *types) {
    override fun onPacketReceiving(event: PacketEvent) { // プレイヤーがエンティティをクリックしたときのパケットの監視
        val player = event.player
        if (event.packetType === PacketType.Play.Client.USE_ENTITY) {
            val packet = event.packet

            val entityID = packet.integers.readSafely(0)

            try {
                val rankingHolograms = Sclat.playerHolograms.get(player) ?: return
                for (armorStand in rankingHolograms.armorStandList) {
                    if (armorStand!!.bukkitEntity.entityId == entityID) {
                        player.playSound(player.location, Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 1f, 1.2f)
                        rankingHolograms.switchNextRankingType()
                        rankingHolograms.refreshRankingAsync()
                        break
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
