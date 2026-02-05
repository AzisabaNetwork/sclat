package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_14_R1.DataWatcherRegistry
import net.minecraft.server.v1_14_R1.EntityPlayer
import net.minecraft.server.v1_14_R1.EntitySquid
import net.minecraft.server.v1_14_R1.EntityTypes
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
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import net.minecraft.server.v1_14_R1.PlayerInteractManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_14_R1.CraftServer
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Consumer

object Decoy {
    @JvmStatic
    fun decoyRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var dcRecharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    if (data.isSneaking && dcRecharge && player.gameMode != GameMode.SPECTATOR) {
                        dcRecharge = false
                        // createDecoy(p, p.getName(), p.getLocation());
                        decoyShot(p)
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    dcRecharge = true
                                }
                            }
                        // Decoyset.runTaskLater(Main.getPlugin(), 5);
                        task.runTaskLater(plugin, 95)
                    }
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun createDecoy(
        player1: Player,
        npcName1: String?,
        location1: Location,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var npc: EntityPlayer? = null
                var es: EntitySquid? = null

                var s: Int = 0

                var player: Player = player1
                var npcName: String? = npcName1
                var location: Location = location1
                var block: Block? = null
                var data: PlayerData? = getPlayerData(player)
                var yaw: Float = 0f
                var ika: Boolean = false // falseがヒト、trueがイカ

                override fun run() {
                    if (s == 0) {
                        ika = false
                        location.yaw = location1.yaw

                        val nmsServer: MinecraftServer = (Bukkit.getServer() as CraftServer).server
                        val nmsWorld = (location.world as CraftWorld).handle
                        val gameProfile = GameProfile(player.uniqueId, npcName)

                        npc = EntityPlayer(nmsServer, nmsWorld, gameProfile, PlayerInteractManager(nmsWorld))

                        // 見えないところにスポーンさせて、クライアントにスキンを先に読み込ませる
                        yaw = player1.eyeLocation.yaw
                        npc!!.setLocation(location.x, location.y - 20, location.z, yaw, 0f)
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
                        es = EntitySquid(EntityTypes.SQUID, nmsWorld)
                        es!!.isNoAI = true
                        es!!.isNoGravity = true
                        // es.setCustomName(CraftChatMessage.fromStringOrNull(player.getName()));
                        // es.setCustomNameVisible(false);
                        (es!!.bukkitEntity as LivingEntity).isCollidable = false
                    }
                    if (s == 0) {
                        block = location.block.getRelative(BlockFace.DOWN)
                        if (blockDataMap.containsKey(block)) {
                            if (block!!.type.toString().contains("WOOL")) {
                                if (block!!.type != data!!.team?.teamColor!!.wool) {
                                    ika = true
                                }
                            }
                        }
                        if (ika) {
                            es!!.setLocation(location.x, location.y, location.z, yaw, 0f)
                            npc!!.setLocation(
                                location.x,
                                location.y - 20,
                                location.z,
                                player1.eyeLocation.yaw,
                                0f,
                            )
                        } else {
                            npc!!.setLocation(
                                location.x,
                                location.y,
                                location.z,
                                player1.eyeLocation.yaw,
                                0f,
                            )
                            es!!.setLocation(location.x, location.y - 20, location.z, yaw, 0f)
                        }
                        val packet = PacketPlayOutSpawnEntityLiving(es)
                        for (target in plugin.server.onlinePlayers) {
                            if (player.world === target.world) {
                                (target as CraftPlayer).handle.playerConnection.sendPacket(packet)
                            }
                        }
                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                            connection.sendPacket(
                                PacketPlayOutEntityHeadRotation(
                                    npc,
                                    ((player1.eyeLocation.yaw * 256.0f) / 360.0f).toInt().toByte(),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.bukkitEntity.entityId,
                                    EnumItemSlot.MAINHAND,
                                    CraftItemStack.asNMSCopy(
                                        getPlayerData(player)!!
                                            .weaponClass
                                            ?.mainWeapon!!
                                            .weaponIteamStack,
                                    ),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.bukkitEntity.entityId,
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(getPlayerData(player)!!.team?.teamColor!!.bougu),
                                ),
                            )
                            connection.sendPacket(PacketPlayOutAnimation(npc, 0))
                        }
                    }
                    if (s != 0 || s != 15) {
                        block = location.block.getRelative(BlockFace.DOWN)
                        if (blockDataMap.containsKey(block)) {
                            if (block!!.type.toString().contains("WOOL")) {
                                ika = block!!.type != data!!.team?.teamColor!!.wool
                            } else {
                                ika = false
                            }
                        } else {
                            ika = false
                        }
                        if (ika) {
                            es!!.setLocation(location.x, location.y, location.z, yaw, 0f)
                            npc!!.setLocation(location.x, location.y - 20, location.z, yaw, 0f)
                            if (s % 2 == 0) {
                                player.world.playSound(location, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
                            }
                        } else {
                            es!!.setLocation(location.x, location.y - 20, location.z, yaw, 0f)
                            npc!!.setLocation(location.x, location.y, location.z, yaw, 0f)
                        }
                        val packet = PacketPlayOutEntityTeleport(es)
                        for (target in plugin.server.onlinePlayers) {
                            if (player.world === target.world) {
                                (target as CraftPlayer).handle.playerConnection.sendPacket(packet)
                            }
                        }
                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                        }
                    }
                    if (s == 15) {
                        for (p in plugin.server.onlinePlayers) {
                            val connection = (p as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(npc!!.bukkitEntity.entityId))
                        }
                        val packet =
                            PacketPlayOutEntityDestroy(
                                es!!.bukkitEntity.entityId,
                            )
                        for (target in plugin.server.onlinePlayers) {
                            if (player.world === target.world) {
                                (target as CraftPlayer).handle.playerConnection.sendPacket(packet)
                            }
                        }
                        cancel()
                    }
                    s++
                }
            }
        task.runTaskTimer(plugin, 0, 7)
    }

    fun decoyShot(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                var as1: ArmorStand? = null
                var c: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            as1 =
                                player.world.spawn<ArmorStand>(
                                    player.location.add(0.0, 1.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.isVisible = false
                                        armorStand.isSmall = true
                                    },
                                )
                            as1!!.velocity =
                                p
                                    .eyeLocation
                                    .direction
                                    .normalize()
                                    .multiply(2.0)
                        }

                        // デコイショットの視認用エフェクト
                        if (getPlayerData(player)!!.settings?.showEffectBomb()!!) {
                            if (player.world === as1!!.location.world) {
                                if (player
                                        .location
                                        .distanceSquared(as1!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            getPlayerData(p)!!.team?.teamColor!!.bukkitColor!!,
                                            1f,
                                        )
                                    player.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        as1!!.location,
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        50.0,
                                        dustOptions,
                                    )
                                }
                            }
                        }

                        c++

                        if (c > 500) {
                            as1!!.remove()
                            cancel()
                            return
                        }

                        if (as1!!.isOnGround) {
                            createDecoy(p, p.name, as1!!.location)
                            as1!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        as1!!.remove()
                        cancel()
                        sclatLogger.warn(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
