package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
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
    fun DecoyRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var dc_recharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }

                    if (data.getIsSneaking() && dc_recharge && player.getGameMode() != GameMode.SPECTATOR) {
                        dc_recharge = false
                        // createDecoy(p, p.getName(), p.getLocation());
                        DecoyShot(p)
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    dc_recharge = true
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
                        location.setYaw(location1.getYaw())

                        val nmsServer: MinecraftServer = (Bukkit.getServer() as CraftServer).getServer()
                        val nmsWorld = (location.getWorld() as CraftWorld).getHandle()
                        val gameProfile = GameProfile(player.getUniqueId(), npcName)

                        npc = EntityPlayer(nmsServer, nmsWorld, gameProfile, PlayerInteractManager(nmsWorld))

                        // 見えないところにスポーンさせて、クライアントにスキンを先に読み込ませる
                        yaw = player1.getEyeLocation().getYaw()
                        npc!!.setLocation(location.getX(), location.getY() - 20, location.getZ(), yaw, 0f)
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
                        es = EntitySquid(EntityTypes.SQUID, nmsWorld)
                        es!!.setNoAI(true)
                        es!!.setNoGravity(true)
                        // es.setCustomName(CraftChatMessage.fromStringOrNull(player.getName()));
                        // es.setCustomNameVisible(false);
                        (es!!.getBukkitEntity() as LivingEntity).setCollidable(false)
                    }
                    if (s == 0) {
                        block = location.getBlock().getRelative(BlockFace.DOWN)
                        if (blockDataMap.containsKey(block)) {
                            if (block!!.getType().toString().contains("WOOL")) {
                                if (block!!.getType() != data!!.team.teamColor!!.wool) {
                                    ika = true
                                }
                            }
                        }
                        if (ika) {
                            es!!.setLocation(location.getX(), location.getY(), location.getZ(), yaw, 0f)
                            npc!!.setLocation(
                                location.getX(),
                                location.getY() - 20,
                                location.getZ(),
                                player1.getEyeLocation().getYaw(),
                                0f,
                            )
                        } else {
                            npc!!.setLocation(
                                location.getX(),
                                location.getY(),
                                location.getZ(),
                                player1.getEyeLocation().getYaw(),
                                0f,
                            )
                            es!!.setLocation(location.getX(), location.getY() - 20, location.getZ(), yaw, 0f)
                        }
                        val packet = PacketPlayOutSpawnEntityLiving(es)
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (player.getWorld() === target.getWorld()) {
                                (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                            }
                        }
                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                            connection.sendPacket(
                                PacketPlayOutEntityHeadRotation(
                                    npc,
                                    ((player1.getEyeLocation().getYaw() * 256.0f) / 360.0f).toInt().toByte(),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.getBukkitEntity().getEntityId(),
                                    EnumItemSlot.MAINHAND,
                                    CraftItemStack.asNMSCopy(
                                        getPlayerData(player)!!
                                            .getWeaponClass()
                                            .mainWeapon!!
                                            .weaponIteamStack,
                                    ),
                                ),
                            )
                            connection.sendPacket(
                                PacketPlayOutEntityEquipment(
                                    npc!!.getBukkitEntity().getEntityId(),
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(getPlayerData(player)!!.team.teamColor!!.bougu),
                                ),
                            )
                            connection.sendPacket(PacketPlayOutAnimation(npc, 0))
                        }
                    }
                    if (s != 0 || s != 15) {
                        block = location.getBlock().getRelative(BlockFace.DOWN)
                        if (blockDataMap.containsKey(block)) {
                            if (block!!.getType().toString().contains("WOOL")) {
                                ika = block!!.getType() != data!!.team.teamColor!!.wool
                            } else {
                                ika = false
                            }
                        } else {
                            ika = false
                        }
                        if (ika) {
                            es!!.setLocation(location.getX(), location.getY(), location.getZ(), yaw, 0f)
                            npc!!.setLocation(location.getX(), location.getY() - 20, location.getZ(), yaw, 0f)
                            if (s % 2 == 0) {
                                player.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
                            }
                        } else {
                            es!!.setLocation(location.getX(), location.getY() - 20, location.getZ(), yaw, 0f)
                            npc!!.setLocation(location.getX(), location.getY(), location.getZ(), yaw, 0f)
                        }
                        val packet = PacketPlayOutEntityTeleport(es)
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (player.getWorld() === target.getWorld()) {
                                (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                            }
                        }
                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityTeleport(npc))
                        }
                    }
                    if (s == 15) {
                        for (p in plugin.getServer().getOnlinePlayers()) {
                            val connection = (p as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(npc!!.getBukkitEntity().getEntityId()))
                        }
                        val packet =
                            PacketPlayOutEntityDestroy(
                                es!!.getBukkitEntity().getEntityId(),
                            )
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (player.getWorld() === target.getWorld()) {
                                (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                            }
                        }
                        cancel()
                    }
                    s++
                }
            }
        task.runTaskTimer(plugin, 0, 7)
    }

    fun DecoyShot(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                var as1: ArmorStand? = null
                var c: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            as1 =
                                player.getWorld().spawn<ArmorStand>(
                                    player.getLocation().add(0.0, 1.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setVisible(false)
                                        armorStand.setSmall(true)
                                    },
                                )
                            as1!!.setVelocity(
                                p
                                    .getEyeLocation()
                                    .getDirection()
                                    .normalize()
                                    .multiply(2.0),
                            )
                        }

                        // デコイショットの視認用エフェクト
                        if (getPlayerData(player)!!.settings.ShowEffect_Bomb()) {
                            if (player.getWorld() === as1!!.getLocation().getWorld()) {
                                if (player
                                        .getLocation()
                                        .distanceSquared(as1!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                            1f,
                                        )
                                    player.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        as1!!.getLocation(),
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

                        if (as1!!.isOnGround()) {
                            createDecoy(p, p.getName(), as1!!.getLocation())
                            as1!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        as1!!.remove()
                        cancel()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
