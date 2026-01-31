package be4rjp.sclat.manager

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_14_R1.DataWatcherRegistry
import net.minecraft.server.v1_14_R1.EntityPlayer
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.MinecraftServer
import net.minecraft.server.v1_14_R1.PacketPlayOutAnimation
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityTeleport
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_14_R1.PlayerInteractManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_14_R1.CraftServer
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object NPCMgr {
    fun createNPC(
        player1: Player,
        npcName1: String?,
        location1: Location,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var npc: EntityPlayer? = null

                var s: Int = 0

                val player: Player = player1
                val npcName: String? = npcName1
                val location: Location = location1

                override fun run() {
                    if (s == 0) {
                        location.setYaw(location1.getYaw())

                        val nmsServer: MinecraftServer = (Bukkit.getServer() as CraftServer).getServer()
                        val nmsWorld = (location.getWorld() as CraftWorld).getHandle()
                        val gameProfile = GameProfile(player.getUniqueId(), npcName)

                        npc = EntityPlayer(nmsServer, nmsWorld, gameProfile, PlayerInteractManager(nmsWorld))

                        // 見えないところにスポーンさせて、クライアントにスキンを先に読み込ませる
                        npc!!.setLocation(location.getX(), location.getY() - 20, location.getZ(), location.getYaw(), 0f)
                        npc!!.getDataWatcher().set<Byte?>(DataWatcherRegistry.a.a(15), 127.toByte())

                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(
                                PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                                    npc,
                                ),
                            )
                            connection.sendPacket(PacketPlayOutNamedEntitySpawn(npc))
                            connection.sendPacket(PacketPlayOutEntityMetadata(npc!!.getId(), npc!!.getDataWatcher(), true))
                        }
                    }
                    if (s == 1) {
                        npc!!.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0f)
                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                            connection.sendPacket(
                                PacketPlayOutEntityHeadRotation(
                                    npc,
                                    ((location.getYaw() * 256.0f) / 360.0f).toInt().toByte(),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.getBukkitEntity().getEntityId(),
                                    EnumItemSlot.MAINHAND,
                                    CraftItemStack.asNMSCopy(
                                        getPlayerData(player)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                    ),
                                ),
                            )
                            if (getPlayerData(player)!!.weaponClass!!.mainWeapon!!.isManeuver) {
                                connection.sendPacket(
                                    PacketPlayOutEntityEquipment(
                                        npc!!.getBukkitEntity().getEntityId(),
                                        EnumItemSlot.OFFHAND,
                                        CraftItemStack.asNMSCopy(
                                            getPlayerData(player)!!
                                                .weaponClass!!
                                                .mainWeapon!!
                                                .weaponIteamStack,
                                        ),
                                    ),
                                )
                            }
                            connection.sendPacket(PacketPlayOutAnimation(npc, 0))
                        }
                    }
                    if (s == 3) {
                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(npc!!.getBukkitEntity().getEntityId()))
                        }
                        cancel()
                    }
                    s++
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }
}
