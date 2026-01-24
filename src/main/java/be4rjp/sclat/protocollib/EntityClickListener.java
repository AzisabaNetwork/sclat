package be4rjp.sclat.protocollib;

import be4rjp.sclat.Main;
import be4rjp.sclat.api.holo.RankingHolograms;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_14_R1.EntityArmorStand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EntityClickListener extends PacketAdapter {

    public EntityClickListener(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {// プレイヤーがエンティティをクリックしたときのパケットの監視
        final Player player = event.getPlayer();
        if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
            final PacketContainer packet = event.getPacket();

            final int EntityID = packet.getIntegers().readSafely(0);

            try {
                RankingHolograms rankingHolograms = Main.playerHolograms.get(event.getPlayer());
                if (rankingHolograms == null)
                    return;
                for (EntityArmorStand armorStand : rankingHolograms.getArmorStandList()) {
                    if (armorStand.getBukkitEntity().getEntityId() == EntityID) {
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 1F,
                                1.2F);
                        rankingHolograms.switchNextRankingType();
                        rankingHolograms.refreshRankingAsync();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
