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

/**
 *
 * @author Be4rJP
 */
object ArmorStandMgr {
    var isSpawned: Boolean = false

    fun ArmorStandEquipPacketSender(world: World) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val c: Int = 0

                override fun run() {
                    for (`as` in world.getEntities()) {
                        if (`as` is ArmorStand) {
                            if (`as`.getCustomName() == null) continue
                            if (!`as`.isVisible()) continue
                            if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") && (`as`.getCustomName() != "100") &&
                                (`as`.getCustomName() != "SplashShield") &&
                                (`as`.getCustomName() != "Kasa")
                            ) {
                                for (o_player in plugin.getServer().getOnlinePlayers()) {
                                    (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_HELMET)),
                                        ),
                                    )
                                    o_player.getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.CHEST,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_CHESTPLATE)),
                                        ),
                                    )
                                    o_player.getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.LEGS,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_LEGGINGS)),
                                        ),
                                    )
                                    o_player.getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
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

    fun ArmorStandSetup(player: Player) {
        for (e in player.getWorld().getEntities()) {
            if (e is ArmorStand || e is Snowball) {
                if (e.getCustomName() == null) continue
                if (e.getCustomName() == "Path") continue
                e.remove()
            }
        }

        for (name in Sclat.Companion.conf!!
            .armorStandSettings!!
            .getConfigurationSection("ArmorStand")!!
            .getKeys(false)) {
            val w =
                Bukkit
                    .getServer()
                    .getWorld(
                        Sclat.Companion.conf!!
                            .armorStandSettings!!
                            .getString("ArmorStand." + name + ".WorldName")!!,
                    )
            val ix =
                Sclat.Companion.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand." + name + ".X")
            val iy =
                Sclat.Companion.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand." + name + ".Y")
            val iz =
                Sclat.Companion.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand." + name + ".Z")
            val iyaw =
                Sclat.Companion.conf!!
                    .armorStandSettings!!
                    .getInt("ArmorStand." + name + ".Yaw")
            val il = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
            il.setYaw(iyaw.toFloat())
            val `as` = w!!.spawnEntity(il, EntityType.ARMOR_STAND) as ArmorStand
            // as.setHelmet(new ItemStack(Material.LEATHER_HELMET));
            // as.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            // as.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            // as.setBoots(new ItemStack(Material.LEATHER_BOOTS));
            `as`.setInvulnerable(true)
            `as`.setCustomName("20.0")
            `as`.setCustomNameVisible(true)
            `as`.setVisible(true)
            setArmorStandPlayer(`as`, player)
        }
    }

    @JvmStatic
    fun BeaconArmorStandSetup(player: Player) {
        val al: Location?
        if (Sclat.Companion.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            al = Sclat.lobby
        } else {
            al = getPlayerData(player)!!.matchLocation
        }
        val `as` = player.getWorld().spawnEntity(al!!, EntityType.ARMOR_STAND) as ArmorStand
        `as`.setVisible(false)
        `as`.setSmall(true)
        `as`.setGravity(false)
        `as`.setCustomName("100")
        `as`.setBasePlate(false)
        `as`.setCustomNameVisible(false)
        setArmorStandPlayer(`as`, player)
        setBeaconFromPlayer(player, `as`)
        val effect: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0

                override fun run() {
                    if (`as`.getCustomName() == "21") {
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        p.getWorld().spawnParticle<Particle.DustOptions?>(
                            Particle.REDSTONE,
                            `as`.getLocation().add(0.0, 0.7, 0.0),
                            3,
                            0.3,
                            0.3,
                            0.3,
                            1.0,
                            dustOptions,
                        )
                        if (c % 10 == 0) {
                            for (player in plugin.getServer().getOnlinePlayers()) {
                                if (`as`.getWorld() === player.getWorld()) {
                                    (player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.IRON_TRAPDOOR)),
                                        ),
                                    )
                                }
                            }

                            // 索敵機能
                            val distance = 8.0

                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch || target.getWorld() !== p.getWorld()) continue
                                if (target.getLocation().distance(`as`.getLocation()) <= distance) {
                                    if (getPlayerData(player)!!.team!!.iD !=
                                        getPlayerData(target)!!
                                            .team!!
                                            .iD
                                    ) {
                                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 40, 1))
                                    }
                                }
                            }

                            for (as1 in player.getWorld().getEntities()) {
                                if (as1.getLocation().distance(`as`.getLocation()) <= distance) {
                                    if (as1.getCustomName() != null) {
                                        if (as1.getCustomName() == null) continue
                                        if (as1 is ArmorStand && (as1.getCustomName() != "Path") && (as1.getCustomName() != "21") &&
                                            (as1.getCustomName() != "100") &&
                                            (as1.getCustomName() != "SplashShield") &&
                                            (as1.getCustomName() != "Kasa")
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
                            for (player in plugin.getServer().getOnlinePlayers()) {
                                if (`as`.getWorld() === player.getWorld()) {
                                    (player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline()) cancel()
                }
            }
        effect.runTaskTimer(plugin, 0, 4)

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val loc = `as`.getLocation()
                    var yaw = `as`.getLocation().getYaw()
                    if (yaw >= 175) yaw = -180f
                    yaw += 3f
                    loc.setYaw(yaw)
                    `as`.teleport(loc)
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline()) cancel()
                }
            }
        task2.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun SprinklerArmorStandSetup(player: Player) {
        val al: Location?
        if (Sclat.Companion.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            al = Sclat.lobby
        } else {
            al = getPlayerData(player)!!.matchLocation
        }
        val `as` = player.getWorld().spawnEntity(al!!, EntityType.ARMOR_STAND) as ArmorStand
        `as`.setVisible(false)
        `as`.setSmall(true)
        `as`.setGravity(false)
        `as`.setCustomName("100")
        `as`.setBasePlate(false)
        `as`.setCustomNameVisible(false)
        setArmorStandPlayer(`as`, player)
        setSprinklerFromPlayer(player, `as`)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0

                override fun run() {
                    if (`as`.getCustomName() == "21") {
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        p.getWorld().spawnParticle<Particle.DustOptions?>(
                            Particle.REDSTONE,
                            `as`.getLocation().add(0.0, 0.7, 0.0),
                            3,
                            0.3,
                            0.3,
                            0.3,
                            1.0,
                            dustOptions,
                        )
                        if (c % 10 == 0) {
                            for (player in plugin.getServer().getOnlinePlayers()) {
                                if (`as`.getWorld() === player.getWorld()) {
                                    (player as CraftPlayer)
                                        .getHandle()
                                        .playerConnection
                                        .sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.getEntityId(),
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
                            for (player in plugin.getServer().getOnlinePlayers()) {
                                if (`as`.getWorld() === player.getWorld()) {
                                    (player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.getEntityId(),
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline()) cancel()
                }
            }
        task.runTaskTimer(plugin, 0, 4)

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val loc = `as`.getLocation()
                    var yaw = `as`.getLocation().getYaw()
                    if (yaw >= 175) yaw = -180f
                    yaw += 3f
                    loc.setYaw(yaw)
                    `as`.teleport(loc)
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline()) cancel()
                }
            }
        task2.runTaskTimer(plugin, 0, 2)

        val shoot: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    if (`as`.getCustomName() == "21") {
                        val b = `as`.getLocation().add(0.0, 0.5, 0.0).getBlock()
                        val u = b.getRelative(BlockFace.UP)
                        val n = b.getRelative(BlockFace.NORTH)
                        val s = b.getRelative(BlockFace.SOUTH)
                        val w = b.getRelative(BlockFace.WEST)
                        val e = b.getRelative(BlockFace.EAST)
                        val d = b.getRelative(BlockFace.DOWN)

                        var vec = Vector(0, 1, 0)

                        if (n.getType() != Material.AIR) vec = Vector(0.0, 0.0, 0.5)
                        if (s.getType() != Material.AIR) vec = Vector(0.0, 0.0, -0.5)
                        if (w.getType() != Material.AIR) vec = Vector(0.5, 0.0, 0.0)
                        if (e.getType() != Material.AIR) vec = Vector(-0.5, 0.0, 0.0)
                        if (u.getType() != Material.AIR) vec = Vector(0.0, -0.5, 0.0)
                        if (d.getType() != Material.AIR) vec = Vector(0.0, 0.5, 0.0)
                        SprinklerMgr.sprinklerShoot(p, `as`, vec)
                        if (!getPlayerData(p)!!.isInMatch || !p.isOnline()) cancel()
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
        if (`as`.getCustomName() == null) return

        if (`as`.getCustomName()!!.contains("§")) return

        if (`as`.getCustomName() == "SplashShield") {
            val ssdata = getSplashShieldDataFromArmorStand(`as`)
            if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(shooter)!!.team) {
                ssdata.damage = (ssdata.damage + damage)
                // ssdata.setDamage(ssdata.damage +
                // DataMgr.getPlayerData(shooter).weaponClass.mainWeapon.damage);
                `as`.getWorld().playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
            }
            return
        }

        if (`as`.getCustomName() == "Kasa") {
            val ssdata = getKasaDataFromArmorStand(`as`)
            if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(shooter)!!.team) {
                ssdata.damage = (ssdata.damage + damage)
                if (ssdata.damage > 200) {
                    `as`
                        .getWorld()
                        .playSound(`as`.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                }
                `as`.getWorld().playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
            }
            return
        }

        if (`as`.getCustomName() == "Path") {
            for (path in getPlayerData(shooter)!!.match!!.mapData!!.pathList) {
                if (path!!.armorStand == `as`) path.setTeam(getPlayerData(shooter)!!.team)
            }
            return
        }

        val health = `as`.getCustomName()!!.toDouble()
        if (health <= 20.0) {
            if (`as`.isVisible()) {
                if (health > damage) {
                    val h = health - damage
                    val rh = (Math.round(h * 10).toDouble()) / 10
                    `as`.setCustomName(rh.toString())
                    `as`.getLocation().getWorld()!!.playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f)
                } else {
                    createInkExplosionEffect(`as`.getEyeLocation().add(0.0, -1.0, 0.0), 3.0, 30, shooter)
                    shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
                    val drop1 = `as`.getWorld().dropItem(`as`.getEyeLocation(), ItemStack(Material.LEATHER_HELMET))
                    val drop2 =
                        `as`.getWorld().dropItem(
                            `as`.getEyeLocation(),
                            ItemStack(Material.LEATHER_CHESTPLATE),
                        )
                    val drop3 = `as`.getWorld().dropItem(`as`.getEyeLocation(), ItemStack(Material.LEATHER_LEGGINGS))
                    val drop4 = `as`.getWorld().dropItem(`as`.getEyeLocation(), ItemStack(Material.LEATHER_BOOTS))
                    val random = 0.4
                    drop1.setVelocity(
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        ),
                    )
                    drop2.setVelocity(
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        ),
                    )
                    drop3.setVelocity(
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        ),
                    )
                    drop4.setVelocity(
                        Vector(
                            Math.random() * random - random / 2,
                            random * 2 / 3,
                            Math.random() * random - random / 2,
                        ),
                    )

                    val bd =
                        getPlayerData(shooter)!!
                            .team!!
                            .teamColor!!
                            .wool!!
                            .createBlockData()
                    `as`.getWorld().spawnParticle<BlockData?>(
                        Particle.BLOCK_DUST,
                        `as`.getEyeLocation(),
                        15,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        bd,
                    )

                    `as`.setCustomNameVisible(false)
                    `as`.setVisible(false)
                    `as`.setMarker(true)

                    // as.setHelmet(new ItemStack(Material.AIR));
                    // as.setChestplate(new ItemStack(Material.AIR));
                    // as.setLeggings(new ItemStack(Material.AIR));
                    // as.setBoots(new ItemStack(Material.AIR));

                    // 半径
                    val maxDist = 3.0

                    // 塗る
                    var i = 0
                    while (i <= maxDist) {
                        val p_locs: MutableList<Location> = getSphere(`as`.getLocation(), i.toDouble(), 20)
                        for (loc in p_locs) {
                            PaintMgr.paint(loc, shooter, false)
                        }
                        i++
                    }

                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        (o_player as CraftPlayer)
                            .getHandle()
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.getEntityId(),
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .getHandle()
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.getEntityId(),
                                    EnumItemSlot.CHEST,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .getHandle()
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.getEntityId(),
                                    EnumItemSlot.LEGS,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .getHandle()
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.getEntityId(),
                                    EnumItemSlot.FEET,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                        o_player
                            .getHandle()
                            .playerConnection
                            .sendPacket(PacketPlayOutEntityDestroy(`as`.getEntityId()))
                    }

                    val delay: BukkitRunnable =
                        object : BukkitRunnable() {
                            override fun run() {
                                drop1.remove()
                                drop2.remove()
                                drop3.remove()
                                drop4.remove()
                                for (o_player in plugin
                                    .getServer()
                                    .getOnlinePlayers()) {
                                    (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutSpawnEntityLiving((`as` as CraftArmorStand).getHandle()),
                                    )
                                }
                                `as`.setCustomNameVisible(true)
                                `as`.setVisible(true)
                                `as`.setMarker(false)
                                `as`.getWorld().playSound(`as`.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f)
                                // as.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                                // as.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                                // as.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                                // as.setBoots(new ItemStack(Material.LEATHER_BOOTS));
                                `as`.setCustomName("20.0")
                            }
                        }
                    delay.runTaskLater(plugin, 60)
                }
            }
        } else if (health == 21.0) {
            val player = getArmorStandPlayer(`as`)
            if (getPlayerData(shooter)!!.team != getPlayerData(player)!!.team) {
                `as`.setCustomName("100")
                `as`.setVisible(false)
                for (op in plugin.getServer().getOnlinePlayers()) {
                    if (`as`.getWorld() === op.getWorld()) {
                        (op as CraftPlayer)
                            .getHandle()
                            .playerConnection
                            .sendPacket(
                                PacketPlayOutEntityEquipment(
                                    `as`.getEntityId(),
                                    EnumItemSlot.HEAD,
                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                ),
                            )
                    }
                }
                `as`.getLocation().getWorld()!!.playSound(`as`.getLocation(), Sound.ENTITY_ARROW_HIT, 1f, 2f)
                `as`.teleport(`as`.getLocation().add(0.0, -1.0, 0.0))
            }
        }
    }
}
