package be4rjp.sclat.packet

import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.player.PlayerSettings
import be4rjp.sclat.data.DataMgr.getPlayerData
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.server.v1_14_R1.EntityTypes
import net.minecraft.server.v1_14_R1.PacketPlayOutAbilities
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntity
import org.bukkit.entity.Player

class PacketHandler(
    private val player: Player?,
) : ChannelDuplexHandler() {
    private val playerData: PlayerData?
    private val playerSettings: PlayerSettings

    init {
        this.playerData = getPlayerData(player)
        this.playerSettings = playerData?.settings!!
    }

    @Throws(Exception::class)
    override fun channelRead(
        channelHandlerContext: ChannelHandlerContext?,
        packet: Any?,
    ) {
        super.channelRead(channelHandlerContext, packet)
    }

    @Throws(Exception::class)
    override fun write(
        channelHandlerContext: ChannelHandlerContext?,
        packet: Any?,
        channelPromise: ChannelPromise?,
    ) {
        // Snowball shown handle

        if (packet is PacketPlayOutSpawnEntity) {
            val k = packet.javaClass.getDeclaredField("k")
            k.setAccessible(true)
            val entityTypes = k.get(packet) as EntityTypes<*>?

            if (entityTypes === EntityTypes.SNOWBALL) {
                if (!playerSettings.showSnowBall()) return
            }
        }

        // Charging fov handle
        if (packet is PacketPlayOutAbilities) {
            if (playerData!!.isCharging) {
                packet.b(playerData.fov)
            }
        }

        super.write(channelHandlerContext, packet, channelPromise)
    }
}
