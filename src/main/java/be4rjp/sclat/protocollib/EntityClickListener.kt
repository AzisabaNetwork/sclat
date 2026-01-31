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
        val player = event.getPlayer()
        if (event.getPacketType() === PacketType.Play.Client.USE_ENTITY) {
            val packet = event.getPacket()

            val entityID = packet.getIntegers().readSafely(0)

            try {
                val rankingHolograms = Sclat.playerHolograms.get(player)
                if (rankingHolograms == null) return
                for (armorStand in rankingHolograms.armorStandList) {
                    if (armorStand!!.getBukkitEntity().getEntityId() == entityID) {
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 1f, 1.2f)
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
