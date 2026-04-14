package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getArmorStandPlayer
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.setArmorStandPlayer
import be4rjp.sclat.data.DataMgr.setBeaconFromPlayer
import be4rjp.sclat.data.DataMgr.setSprinklerFromPlayer
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.round

/**
 *
 * @author Be4rJP
 */
object ArmorStandMgr {
    var isSpawned: Boolean = false

    fun armorStandEquipPacketSender(world: World) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val c: Int = 0

                override fun run() {
                    for (`as` in world.entities) {
                        if (`as` is ArmorStand) {
                            if (`as`.customName == null) continue
                            if (!`as`.isVisible) continue
                            if ((`as`.customName != "Path") &&
                                (`as`.customName != "21") &&
                                (`as`.customName != "100") &&
                                (`as`.customName != "SplashShield") &&
                                (`as`.customName != "Kasa")
                            ) {
                                for (o_player in plugin.server.onlinePlayers) {
                                    (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_HELMET)),
                                        ),
                                    )
                                    o_player.handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.CHEST,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_CHESTPLATE)),
                                        ),
                                    )
                                    o_player.handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.LEGS,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_LEGGINGS)),
                                        ),
                                    )
                                    o_player.handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.FEET,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_BOOTS)),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }

    fun armorStandSetup(player: Player) {
        for (e in player.world.entities) {
            if (e is ArmorStand || e is Snowball) {
                if (e.customName == null) continue
                if (e.customName == "Path") continue
                e.remove()
            }
        }

        for (name in Sclat.conf!!
            .armorStandSettings!!
            .getConfigurationSection("ArmorStand")!!
            .getKeys(false)) {
            val w =
                Bukkit
                    .getServer()
                    .getWorld(
                        Sclat.conf!!
                            .armorStandSettings!!
                            .getString("ArmorStand.$name.WorldName")!!,
                    )
            val ix =
                Sclat.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand.$name.X")
            val iy =
                Sclat.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand.$name.Y")
            val iz =
                Sclat.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand.$name.Z")
            val iyaw =
                Sclat.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand.$name.Yaw")
            val il = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
            il.yaw = iyaw.toFloat()
            val `as` = w!!.spawnEntity(il, EntityType.ARMOR_STAND) as ArmorStand
            // Equipments are wear with packet
            `as`.isInvulnerable = true
            `as`.customName = "20.0"
            `as`.isCustomNameVisible = true
            `as`.isVisible = true
            setArmorStandPlayer(`as`, player)
        }
    }

    @JvmStatic
    fun beaconArmorStandSetup(player: Player) {
        val al: Location? =
            if (Sclat.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
                Sclat.lobby
            } else {
                getPlayerData(player)!!.matchLocation
            }
        val `as` = player.world.spawnEntity(al!!, EntityType.ARMOR_STAND) as ArmorStand
        `as`.isVisible = false
        `as`.isSmall = true
        `as`.setGravity(false)
        `as`.customName = "100"
        `as`.setBasePlate(false)
        `as`.isCustomNameVisible = false
        setArmorStandPlayer(`as`, player)
        setBeaconFromPlayer(player, `as`)
        val effect: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0

                override fun run() {
                    if (`as`.customName == "21") {
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        p.world.spawnParticle<Particle.DustOptions?>(
                            Particle.REDSTONE,
                            `as`.location.add(0.0, 0.7, 0.0),
                            3,
                            0.3,
                            0.3,
                            0.3,
                            1.0,
                            dustOptions,
                        )
                        if (c % 10 == 0) {
                            for (player in plugin.server.onlinePlayers) {
                                if (`as`.world === player.world) {
                                    (player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.IRON_TRAPDOOR)),
                                        ),
                                    )
                                }
                            }

                            // 索敵機能
                            val distance = 8.0

                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distance(`as`.location) <= distance) {
                                    if (getPlayerData(player)!!.team!!.iD !=
                                        getPlayerData(target)!!
                                            .team!!
                                            .iD
                                    ) {
                                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 40, 1))
                                    }
                                }
                            }

                            for (as1 in player.world.entities) {
                                if (as1.location.distance(`as`.location) <= distance) {
                                    if (as1.customName != null) {
                                        if (as1.customName == null) continue
                                        if (as1 is ArmorStand &&
                                            (as1.customName != "Path") &&
                                            (as1.customName != "21") &&
                                            (as1.customName != "100") &&
                                            (as1.customName != "SplashShield") &&
                                            (as1.customName != "Kasa")
                                        ) {
                                            as1
                                                .addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 40, 1))
                                        }
                                    }
                                }
                            }
                        }
                        c++
                    } else {
                        if (c % 10 == 0) {
                            for (player in plugin.server.onlinePlayers) {
                                if (`as`.world === player.world) {
                                    (player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) cancel()
                }
            }
        effect.runTaskTimer(plugin, 0, 4)

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val loc = `as`.location
                    var yaw = `as`.location.yaw
                    if (yaw >= 175) yaw = -180f
                    yaw += 3f
                    loc.yaw = yaw
                    `as`.teleport(loc)
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) cancel()
                }
            }
        task2.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun sprinklerArmorStandSetup(player: Player) {
        val al: Location?
        al =
            if (Sclat.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
                Sclat.lobby
            } else {
                getPlayerData(player)!!.matchLocation
            }
        val `as` = player.world.spawnEntity(al!!, EntityType.ARMOR_STAND) as ArmorStand
        `as`.isVisible = false
        `as`.isSmall = true
        `as`.setGravity(false)
        `as`.customName = "100"
        `as`.setBasePlate(false)
        `as`.isCustomNameVisible = false
        setArmorStandPlayer(`as`, player)
        setSprinklerFromPlayer(player, `as`)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0

                override fun run() {
                    if (`as`.customName == "21") {
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        p.world.spawnParticle<Particle.DustOptions?>(
                            Particle.REDSTONE,
                            `as`.location.add(0.0, 0.7, 0.0),
                            3,
                            0.3,
                            0.3,
                            0.3,
                            1.0,
                            dustOptions,
                        )
                        if (c % 10 == 0) {
                            for (player in plugin.server.onlinePlayers) {
                                if (`as`.world === player.world) {
                                    (player as CraftPlayer)
                                        .handle
                                        .playerConnection
                                        .sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        getPlayerData(p)!!.team!!.teamColor!!.glass!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                }
                            }
                        }
                        c++
                    } else {
                        if (c % 10 == 0) {
                            for (player in plugin.server.onlinePlayers) {
                                if (`as`.world === player.world) {
                                    (player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) cancel()
                }
            }
        task.runTaskTimer(plugin, 0, 4)

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val loc = `as`.location
                    var yaw = `as`.location.yaw
                    if (yaw >= 175) yaw = -180f
                    yaw += 3f
                    loc.yaw = yaw
                    `as`.teleport(loc)
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) cancel()
                }
            }
        task2.runTaskTimer(plugin, 0, 2)

        val shoot: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    if (`as`.customName == "21") {
                        val b = `as`.location.add(0.0, 0.5, 0.0).block
                        val u = b.getRelative(BlockFace.UP)
                        val n = b.getRelative(BlockFace.NORTH)
                        val s = b.getRelative(BlockFace.SOUTH)
                        val w = b.getRelative(BlockFace.WEST)
                        val e = b.getRelative(BlockFace.EAST)
                        val d = b.getRelative(BlockFace.DOWN)

                        var vec = Vector(0, 1, 0)

                        if (n.type != Material.AIR) vec = Vector(0.0, 0.0, 0.5)
                        if (s.type != Material.AIR) vec = Vector(0.0, 0.0, -0.5)
                        if (w.type != Material.AIR) vec = Vector(0.5, 0.0, 0.0)
                        if (e.type != Material.AIR) vec = Vector(-0.5, 0.0, 0.0)
                        if (u.type != Material.AIR) vec = Vector(0.0, -0.5, 0.0)
                        if (d.type != Material.AIR) vec = Vector(0.0, 0.5, 0.0)
                        SprinklerMgr.sprinklerShoot(p, `as`, vec)
                        if (!getPlayerData(p)!!.isInMatch || !p.isOnline) cancel()
                    }
                }
            }

        shoot.runTaskTimer(plugin, 0, 4)
    }

    fun giveDamageArmorStand(
        `as`: ArmorStand,
        damage: Double,
        shooter: Player,
    ) {
        if (`as`.customName == null) return

        if (`as`.customName!!.contains("§")) return

        if (`as`.customName == "SplashShield") {
            val ssdata = getSplashShieldDataFromArmorStand(`as`)
            if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(shooter)!!.team) {
                ssdata.damage = (ssdata.damage + damage)
                // ssdata.setDamage(ssdata.damage +
                // DataMgr.getPlayerData(shooter).weaponClass.mainWeapon.damage);
                `as`.world.playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
            }
            return
        }

        if (`as`.customName == "Kasa") {
            val ssdata = getKasaDataFromArmorStand(`as`)
            if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(shooter)!!.team) {
                ssdata.damage = (ssdata.damage + damage)
                if (ssdata.damage > 200) {
                    `as`
                        .world
                        .playSound(`as`.location, Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                }
                `as`.world.playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
            }
            return
        }

        if (`as`.customName == "Path") {
            for (path in getPlayerData(shooter)!!.match!!.mapData!!.pathList) {
                if (path!!.armorStand == `as`) path.setTeam(getPlayerData(shooter)!!.team)
            }
            return
        }

        val health = `as`.customName!!.toDouble()
        if (health <= 20.0) {
            if (`as`.isVisible) {
                if (health > damage) {
                    val h = health - damage
                    val rh = (round(h * 10)) / 10
                    `as`.customName = rh.toString()
                    `as`.location.world!!.playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
                } else {
                    createInkExplosionEffect(`as`.eyeLocation.add(0.0, -1.0, 0.0), 3.0, 30, shooter)
                    shooter.playSound(shooter.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
                    val drop1 = `as`.world.dropItem(`as`.eyeLocation, ItemStack(Material.LEATHER_HELMET))
                    val drop2 =
                        `as`.world.dropItem(
                            `as`.eyeLocation,
                            ItemStack(Material.LEATHER_CHESTPLATE),
                        )
                    val drop3 = `as`.world.dropItem(`as`.eyeLocation, ItemStack(Material.LEATHER_LEGGINGS))
                    val drop4 = `as`.world.dropItem(`as`.eyeLocation, ItemStack(Material.LEATHER_BOOTS))
                    val random = 0.4
                    drop1.velocity =
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        )
                    drop2.velocity =
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        )
                    drop3.velocity =
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        )
                    drop4.velocity =
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        )

                    val bd =
                        getPlayerData(shooter)!!
                            .team!!
                            .teamColor!!
                            .wool!!
                            .createBlockData()
                    `as`.world.spawnParticle<BlockData?>(
                        Particle.BLOCK_DUST,
                        `as`.eyeLocation,
                        15,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        bd,
                    )

                    `as`.isCustomNameVisible = false
                    `as`.isVisible = false
                    `as`.isMarker = true

                    // as.setHelmet(new ItemStack(Material.AIR));
                    // as.setChestplate(new ItemStack(Material.AIR));
                    // as.setLeggings(new ItemStack(Material.AIR));
                    // as.setBoots(new ItemStack(Material.AIR));

                    // 半径
                    val maxDist = 3.0

                    // 塗る
                    var i = 0
                    while (i <= maxDist) {
                        val pLocs: MutableList<Location> = getSphere(`as`.location, i.toDouble(), 20)
                        for (loc in pLocs) {
                            PaintMgr.paint(loc, shooter, false)
                        }
                        i++
                    }

                    for (o_player in plugin.server.onlinePlayers) {
                        (o_player as CraftPlayer)
                            .handle
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.entityId,
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .handle
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.entityId,
                                    EnumItemSlot.CHEST,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .handle
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.entityId,
                                    EnumItemSlot.LEGS,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .handle
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.entityId,
                                    EnumItemSlot.FEET,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .handle
                            .playerConnection
                            .sendPacket(PacketPlayOutEntityDestroy(`as`.entityId))
                    }

                    val delay: BukkitRunnable =
                        object : BukkitRunnable() {
                            override fun run() {
                                drop1.remove()
                                drop2.remove()
                                drop3.remove()
                                drop4.remove()
                                for (o_player in plugin
                                    .server
                                    .onlinePlayers) {
                                    (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutSpawnEntityLiving((`as` as CraftArmorStand).handle),
                                    )
                                }
                                `as`.isCustomNameVisible = true
                                `as`.isVisible = true
                                `as`.isMarker = false
                                `as`.world.playSound(`as`.location, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f)
                                // as.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                                // as.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                                // as.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                                // as.setBoots(new ItemStack(Material.LEATHER_BOOTS));
                                `as`.customName = "20.0"
                            }
                        }
                    delay.runTaskLater(plugin, 60)
                }
            }
        } else if (health == 21.0) {
            val player = getArmorStandPlayer(`as`)
            if (getPlayerData(shooter)!!.team != getPlayerData(player)!!.team) {
                `as`.customName = "100"
                `as`.isVisible = false
                for (op in plugin.server.onlinePlayers) {
                    if (`as`.world === op.world) {
                        (op as CraftPlayer)
                            .handle
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.entityId,
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                    }
                }
                `as`.location.world!!.playSound(`as`.location, Sound.ENTITY_ARROW_HIT, 1f, 2f)
                `as`.teleport(`as`.location.add(0.0, -1.0, 0.0))
            }
        }
    }
}
