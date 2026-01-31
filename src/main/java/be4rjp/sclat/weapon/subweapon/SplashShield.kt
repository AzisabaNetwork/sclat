package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.setSplashShieldDataWithARmorStand
import be4rjp.sclat.data.DataMgr.setSplashShieldDataWithPlayer
import be4rjp.sclat.data.DataMgr.splashShieldDataMapWithPlayer
import be4rjp.sclat.data.SplashShieldData
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Consumer
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object SplashShield {
    @JvmStatic
    fun splashShieldThrowRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var p: Player = player
                var drop: Item? = null
                var yaw: Float = 0f
                var vec: Vector? = null

                override fun run() {
                    try {
                        if (i == 0) {
                            p.setExp(p.getExp() - 0.59f)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), ItemStack(Material.ACACIA_FENCE))
                            drop!!.setVelocity(p.getEyeLocation().getDirection().multiply(0.7))
                            yaw = p.getEyeLocation().getYaw()
                            val v = p.getEyeLocation().getDirection().normalize()
                            vec = (Vector(v.getX(), 0.0, v.getZ())).normalize()
                        }

                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (drop!!.getWorld() === o_player.getWorld()) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            drop!!.getLocation(),
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
                        }

                        if (!getPlayerData(p)!!.isInMatch()) {
                            drop!!.remove()
                            cancel()
                        }

                        if (drop!!.getLocation().getY() < 0 || drop!!.getLocation().getY() < p
                                .getLocation()
                                .getY() - 100 || drop!!.isDead()
                        ) {
                            cancel()
                        }

                        if (drop!!.isOnGround()) {
                            val loc = drop!!.getLocation()
                            loc.setYaw(yaw)
                            try {
                                for (ssdata in splashShieldDataMapWithPlayer.values) {
                                    if (ssdata!!.player === p) {
                                        for (`as` in ssdata.armorStandList!!) `as`!!.remove()
                                        ssdata.task!!.cancel()
                                    }
                                }
                            } catch (e: Exception) {
                            }
                            val ssdata = SplashShieldData(p)
                            SplashShield.splashShieldRunnable(p, loc.clone(), vec!!, ssdata)
                            drop!!.remove()
                            cancel()
                            return
                        }

                        i++
                    } catch (e: Exception) {
                        cancel()
                        drop!!.remove()
                    }
                }
            }
        if (player.getExp() > 0.6f) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.setCanUseSubWeapon(true)
                }
            }
        cooltime.runTaskLater(plugin, 10)
    }

    fun splashShieldRunnable(
        player: Player,
        loc: Location,
        vec: Vector,
        ssdata: SplashShieldData,
    ) {
        val list: MutableList<ArmorStand> = mutableListOf()
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var c: Int = 0
                var p: Player = player
                var pv: Vector = vec.clone()

                override fun run() {
                    try {
                        if (c == 0) {
                            p.getWorld().playSound(loc, Sound.ENTITY_ARMOR_STAND_FALL, 1f, 1f)
                            val pv2 = pv.clone().multiply(-0.25)
                            val yaw = loc.getYaw()
                            val vec1 = Vector(pv.clone().getZ() * -1, 0.0, pv.clone().getX()).normalize()
                            val vec2 = Vector(pv.clone().getZ(), 0.0, pv.clone().getX() * -1).normalize()

                            val rayTrace1 = RayTrace(loc.clone().add(0.0, 0.8, 0.0).toVector(), vec1)
                            val positions1: ArrayList<Vector> = rayTrace1.traverse(3.0, 0.2)

                            val rayTrace2 = RayTrace(loc.clone().add(0.0, 0.8, 0.0).toVector(), vec2)
                            val positions2: ArrayList<Vector> = rayTrace2.traverse(3.0, 0.2)

                            val as1 =
                                player.getWorld().spawn<ArmorStand>(
                                    loc.clone().add(0.0, -0.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(135.0)))
                                    },
                                )
                            list.add(as1)
                            val as2 =
                                player.getWorld().spawn<ArmorStand>(
                                    loc.clone().add(0.0, 0.0, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(135.0)))
                                    },
                                )
                            list.add(as2)
                            val as3 =
                                player.getWorld().spawn<ArmorStand>(
                                    loc.clone().add(0.0, 0.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(135.0)))
                                    },
                                )
                            list.add(as3)
                            val as4 =
                                player.getWorld().spawn<ArmorStand>(
                                    loc.clone().add(0.0, 1.05, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(135.0)))
                                    },
                                )
                            list.add(as4)
                            val as5 =
                                player.getWorld().spawn<ArmorStand>(
                                    loc.clone().add(0.0, -0.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(135.0)))
                                    },
                                )
                            list.add(as5)
                            val l6 = positions2.get(5)!!.toLocation(loc.getWorld()!!).add(0.0, 0.25, 0.0)
                            l6.setYaw(yaw)
                            val as6 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l6,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                            armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(270.0)))
                                        },
                                    )
                            list.add(as6)
                            val l7 = positions1.get(4)!!.toLocation(loc.getWorld()!!)
                            l7.setYaw(yaw)
                            val as7 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l7,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                            armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(45.0)))
                                        },
                                    )
                            list.add(as7)
                            val l8 = positions1.get(7)!!.toLocation(loc.getWorld()!!)
                            l8.setYaw(yaw)
                            val as8 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l8,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                            armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(45.0)))
                                        },
                                    )
                            list.add(as8)
                            val l9 = positions2.get(4)!!.toLocation(loc.getWorld()!!)
                            l9.setYaw(yaw)
                            val as9 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l9,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                            armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(45.0)))
                                        },
                                    )
                            list.add(as9)
                            val l10 = positions2.get(7)!!.toLocation(loc.getWorld()!!)
                            l10.setYaw(yaw)
                            val as10 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l10,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                            armorStand.setHeadPose(EulerAngle(0.0, 0.0, Math.toRadians(45.0)))
                                        },
                                    )
                            list.add(as10)
                            val l11 = positions2.get(4)!!.toLocation(loc.getWorld()!!).add(0.0, -0.45, 0.0)
                            l11.setYaw(yaw)
                            val as11 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l11,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                        },
                                    )
                            list.add(as11)
                            val l12 =
                                positions2.get(3)!!.toLocation(loc.getWorld()!!).clone().add(
                                    pv2.getX(),
                                    -0.1,
                                    pv2.getZ(),
                                )
                            l12.setYaw(yaw)
                            val as12 =
                                player.getWorld().spawn<ArmorStand>(
                                    l12.add(vec1.clone().normalize().multiply(0.05)),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setSmall(true)
                                    },
                                )
                            as12.setSmall(true)
                            list.add(as12)
                            val l13 =
                                positions2.get(3)!!.toLocation(loc.getWorld()!!).clone().add(
                                    pv2.getX(),
                                    -0.5,
                                    pv2.getZ(),
                                )
                            l13.setYaw(yaw)
                            val as13 =
                                player.getWorld().spawn<ArmorStand>(
                                    l13.add(vec1.clone().normalize().multiply(0.05)),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setMarker(true)
                                        armorStand.setVisible(false)
                                        armorStand.setBasePlate(false)
                                        armorStand.setGravity(false)
                                        armorStand.setSmall(true)
                                    },
                                )
                            as13.setSmall(true)
                            list.add(as13)
                            val l14 = positions2.get(10)!!.toLocation(loc.getWorld()!!)
                            l14.setYaw(yaw)
                            val as14 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        l14,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setMarker(true)
                                            armorStand.setVisible(false)
                                            armorStand.setBasePlate(false)
                                            armorStand.setGravity(false)
                                        },
                                    )
                            list.add(as14)

                            ssdata.armorStandList = list
                            ssdata.isDeploy = (false)

                            var i = 1
                            for (a in list) {
                                setSplashShieldDataWithARmorStand(a, ssdata)
                                DataMgr.ssa.add(a)
                                a.setCustomName("SplashShield")

                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (i <= 5) {
                                        (target as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                a.getEntityId(),
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(ItemStack(Material.BLAZE_ROD)),
                                            ),
                                        )
                                    }
                                    if (i > 11 && i < 14) {
                                        (target as CraftPlayer)
                                            .getHandle()
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    a.getEntityId(),
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            getPlayerData(p)!!.team.teamColor!!.glass!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                    }
                                }
                                i++
                            }
                        }

                        if (c == 10) {
                            var i = 1
                            for (a in list) {
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (i > 5 && i <= 11) {
                                        (target as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                a.getEntityId(),
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(ItemStack(Material.STICK)),
                                            ),
                                        )
                                    }
                                }
                                i++
                            }
                            p.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                        }

                        if (c == 15) {
                            ssdata.isDeploy = (true)
                            for (a in list) {
                                a.setMarker(false)
                            }
                        }

                        if (c >= 15 && c % 2 == 0) {
                            val vec1 = Vector(pv.clone().getZ() * -1, 0.0, pv.clone().getX()).normalize()
                            val vec2 = Vector(pv.clone().getZ(), 0.0, pv.clone().getX() * -1).normalize()

                            val rayTrace1 = RayTrace(loc.clone().add(0.0, 2.7, 0.0).toVector(), vec1)
                            val positions1: ArrayList<Vector> = rayTrace1.traverse(3.0, 0.38)

                            val rayTrace2 = RayTrace(loc.clone().add(0.0, 2.7, 0.0).toVector(), vec2)
                            val positions2: ArrayList<Vector> = rayTrace2.traverse(3.0, 0.38)

                            val sv = pv.clone().multiply(-0.25)

                            val bd =
                                getPlayerData(p)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            ray@ for (i in 0..<positions1.size - 4) {
                                val position = positions1.get(i)!!.toLocation(p.getLocation().getWorld()!!)
                                PaintMgr.PaintHightestBlock(position, p, false, false)
                                val damage = 10.0
                                val rayTrace4 =
                                    RayTrace(
                                        position.clone().add(sv.getX(), 0.0, sv.getZ()).toVector(),
                                        Vector(0, -1, 0),
                                    )
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (target.getWorld() === p.getWorld()) {
                                        if (rayTrace4.intersects(BoundingBox(target as Entity), 3.0, 0.5) &&
                                            getPlayerData(target)!!.team != getPlayerData(p)!!.team &&
                                            target.getGameMode() == GameMode.ADVENTURE
                                        ) {
                                            giveDamage(player, target, damage, "subWeapon")

                                            // AntiNoDamageTime
                                            val task: BukkitRunnable =
                                                object : BukkitRunnable() {
                                                    var p: Player = target

                                                    override fun run() {
                                                        target.setNoDamageTicks(0)
                                                    }
                                                }
                                            task.runTaskLater(plugin, 1)
                                        }
                                    }
                                }

                                for (o_player in plugin.getServer().getOnlinePlayers()) {
                                    o_player.spawnParticle<BlockData?>(
                                        Particle.FALLING_DUST,
                                        position.clone().add(sv.getX(), 0.0, sv.getZ()),
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        200.0,
                                        bd,
                                    )
                                }
                            }

                            ray@ for (i in 0..<positions2.size - 1) {
                                val position = positions2.get(i)!!.toLocation(p.getLocation().getWorld()!!)
                                PaintMgr.PaintHightestBlock(position, p, false, false)
                                val damage = 10.0
                                val rayTrace4 =
                                    RayTrace(
                                        position.clone().add(sv.getX(), 0.0, sv.getZ()).toVector(),
                                        Vector(0, -1, 0),
                                    )
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (target.getWorld() === p.getWorld()) {
                                        if (rayTrace4.intersects(BoundingBox(target as Entity), 3.0, 0.5) &&
                                            getPlayerData(target)!!.team != getPlayerData(p)!!.team &&
                                            target.getGameMode() == GameMode.ADVENTURE
                                        ) {
                                            giveDamage(player, target, damage, "subWeapon")

                                            // AntiNoDamageTime
                                            val task: BukkitRunnable =
                                                object : BukkitRunnable() {
                                                    var p: Player = target

                                                    override fun run() {
                                                        target.setNoDamageTicks(0)
                                                    }
                                                }
                                            task.runTaskLater(plugin, 1)
                                        }
                                    }
                                }
                                for (o_player in plugin.getServer().getOnlinePlayers()) {
                                    if (i == 0 || i == 1 || i == 2) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.FALLING_DUST,
                                            position.clone().add(sv.getX(), -0.2, sv.getZ()),
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            200.0,
                                            bd,
                                        )
                                    } else {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.FALLING_DUST,
                                            position.clone().add(sv.getX(), 0.0, sv.getZ()),
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            200.0,
                                            bd,
                                        )
                                    }
                                }
                            }
                        }

                        if (c == 110) {
                            var i = 1
                            for (a in list) {
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (i == 12) {
                                        (target as CraftPlayer)
                                            .getHandle()
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    a.getEntityId(),
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack
                                                        .asNMSCopy(ItemStack(Material.WHITE_STAINED_GLASS)),
                                                ),
                                            )
                                    }
                                }
                                i++
                            }
                        }

                        if (c > 200 || !getPlayerData(p)!!.isInMatch() || ssdata.damage > 80) {
                            for (a in list) a.remove()
                            list.get(0).getWorld().playSound(
                                list.get(0).getLocation(),
                                Sound.ENTITY_ITEM_BREAK,
                                0.8f,
                                0.8f,
                            )
                            cancel()
                            return
                        }
                        c++
                    } catch (e: Exception) {
                        cancel()
                        for (a in list) a.remove()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
        ssdata.task = task
        setSplashShieldDataWithPlayer(player, ssdata)
    }
}
