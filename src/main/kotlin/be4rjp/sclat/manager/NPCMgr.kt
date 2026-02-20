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
                        location.yaw = location1.yaw

                        val nmsServer: MinecraftServer = (Bukkit.getServer() as CraftServer).server
                        val nmsWorld = (location.world as CraftWorld).handle
                        val gameProfile = GameProfile(player.uniqueId, npcName)

                        npc = EntityPlayer(nmsServer, nmsWorld, gameProfile, PlayerInteractManager(nmsWorld))

                        // 見えないところにスポーンさせて、クライアントにスキンを先に読み込ませる
                        npc!!.setLocation(location.x, location.y - 20, location.z, location.yaw, 0f)
                        npc!!.dataWatcher.set<Byte?>(DataWatcherRegistry.a.a(15), 127.toByte())

                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(
                                PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                                    npc,
                                ),
                            )
                            connection.sendPacket(PacketPlayOutNamedEntitySpawn(npc))
                            connection.sendPacket(PacketPlayOutEntityMetadata(npc!!.id, npc!!.dataWatcher, true))
                        }
                    }
                    if (s == 1) {
                        npc!!.setLocation(location.x, location.y, location.z, location.yaw, 0f)
                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                            connection.sendPacket(
                                PacketPlayOutEntityHeadRotation(
                                    npc,
                                    ((location.yaw * 256.0f) / 360.0f).toInt().toByte(),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.bukkitEntity.entityId,
                                    EnumItemSlot.MAINHAND,
                                    CraftItemStack.asNMSCopy(
                                        getPlayerData(player)!!.weaponClass!!.mainWeapon!!.weaponItemStack,
                                    ),
                                ),
                            )
                            if (getPlayerData(player)!!.weaponClass!!.mainWeapon!!.isManeuver) {
                                connection.sendPacket(
                                    PacketPlayOutEntityEquipment(
                                        npc!!.bukkitEntity.entityId,
                                        EnumItemSlot.OFFHAND,
                                        CraftItemStack.asNMSCopy(
                                            getPlayerData(player)!!
                                                .weaponClass!!
                                                .mainWeapon!!
                                                .weaponItemStack,
                                        ),
                                    ),
                                )
                            }
                            connection.sendPacket(PacketPlayOutAnimation(npc, 0))
                        }
                    }
                    if (s == 3) {
                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(npc!!.bukkitEntity.entityId))
                        }
                        cancel()
                    }
                    s++
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }
}
