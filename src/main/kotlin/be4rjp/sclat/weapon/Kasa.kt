package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import be4rjp.sclat.data.DataMgr.setKasaDataWithPlayer
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.data.KasaData
import be4rjp.sclat.manager.MainWeaponMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Kasa {
    @JvmStatic
    fun shootKasa(player: Player) {
        if (player.isSneaking) return

        val data = getPlayerData(player)
        val delay1: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.canRollerShoot = true
                }
            }
        if (data!!.canRollerShoot) {
            delay1.runTaskLater(
                plugin,
                data
                    .weaponClass!!
                    .mainWeapon!!
                    .coolTime
                    .toLong(),
            )
        }

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    var sound = false
                    for (i in 0..<data.weaponClass?.mainWeapon!!.rollerShootQuantity) {
                        val `is` = shoot(player, null)
                        if (`is`) sound = true
                    }
                    player.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9f, 1.3f)
                    if (sound) {
                        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                    }
                }
            }
        if (data.canRollerShoot) {
            delay.runTaskLater(
                plugin,
                data
                    .weaponClass!!
                    .mainWeapon!!
                    .delay
                    .toLong(),
            )
            data.canRollerShoot = false
        }
    }

    fun shoot(
        player: Player,
        v: Vector?,
    ): Boolean {
        if (player.gameMode == GameMode.SPECTATOR) return false

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass!!.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
            return true
        }
        player.exp -=
            (
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        var vec: Vector? =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.shootSpeed)
        if (v != null) vec = v
        val random = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.distanceTick
        vec!!.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random / 1.5 - random / 3,
                Math.random() * random - random / 2,
            ),
        )
        ball.velocity = vec
        ball.shooter = player
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        ball.customName = name
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick
                var inkball: Snowball? = ball
                var p: Player = player
                var addedFallVec: Boolean = false
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) continue
                            if (target.world === inkball!!.world) {
                                if (target
                                        .location
                                        .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team!!
                                            .teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        inkball!!.location,
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0,
                                        bd,
                                    )
                                }
                            }
                        }
                    }

                    if (i >= tick && !addedFallVec) {
                        inkball!!.velocity = fallvec
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.velocity = inkball!!.velocity.add(Vector(0.0, -0.1, 0.0))
                    }
                    if (i != tick) PaintMgr.paintHightestBlock(inkball!!.location, p, true, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)

        return false
    }

    @JvmStatic
    fun kasaRunnable(
        player: Player,
        big: Boolean,
    ) {
        val kdata = KasaData(player)
        setKasaDataWithPlayer(player, kdata)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var i: Int = 0
                var list: MutableList<ArmorStand> = ArrayList()
                var weapon: Boolean = false
                var sound: Boolean = true

                var as1: ArmorStand? = null
                var as2: ArmorStand? = null
                var as3: ArmorStand? = null
                var as4: ArmorStand? = null
                var as5: ArmorStand? = null
                var as6: ArmorStand? = null
                var as7: ArmorStand? = null

                override fun run() {
                    try {
                        val data = getPlayerData(p)

                        try {
                            weapon = MainWeaponMgr.equalWeapon(p)
                        } catch (e: Exception) {
                            weapon = false
                        }

                        if (data!!.isSneaking && kdata.damage <= 200) {
                            if (!sound) {
                                sound = true
                                p.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                            }
                        } else {
                            sound = false
                        }

                        val ploc = player.location
                        val pvec = player.eyeLocation.direction.normalize()
                        val vec = Vector(pvec.getX(), 0.0, pvec.getZ()).normalize().multiply(1.3)
                        val mvec = vec.clone().normalize().multiply(-1.2)
                        val aml = ploc.clone().add(vec.getX(), -1.15, vec.getZ())
                        val aml2 = ploc.clone().add(vec.getX() * 0.8, -1.15, vec.getZ() * 0.8)
                        val v1 = Vector(vec.getZ() * -1, 0.0, vec.getX()).normalize().multiply(0.31)
                        val v2 = Vector(vec.getZ(), 0.0, vec.getX() * -1).normalize().multiply(0.31)
                        val rl = aml.clone().add(v1)
                        val rl2 = aml2.clone().add(v1)
                        val ll = aml.clone().add(v2)
                        val ll2 = aml2.clone().add(v2)

                        if (i == 0) {
                            as1 =
                                player.world.spawnEntity(
                                    rl.clone().add(0.0, 0.2, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as2 =
                                player.world.spawnEntity(
                                    rl2.clone().add(0.0, -0.52, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as3 =
                                player.world.spawnEntity(
                                    ll.clone().add(0.0, 0.2, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as4 =
                                player.world.spawnEntity(
                                    ll2.clone().add(0.0, -0.52, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as5 =
                                player
                                    .world
                                    .spawnEntity(
                                        aml.clone().add(mvec.getX(), 0.35, mvec.getZ()),
                                        EntityType.ARMOR_STAND,
                                    ) as ArmorStand
                            as6 =
                                player.world.spawnEntity(
                                    rl.clone().add(0.0, 0.8, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as7 =
                                player.world.spawnEntity(
                                    ll.clone().add(0.0, 0.8, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            // as5.setSmall(true);
                            list.add(as1!!)
                            list.add(as2!!)
                            list.add(as4!!)
                            list.add(as3!!)
                            list.add(as5!!)
                            list.add(as6!!)
                            list.add(as7!!)

                            kdata.armorStandList = list

                            var c = 1
                            for (`as` in list) {
                                // as.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
                                `as`.setBasePlate(false)
                                `as`.isVisible = false
                                `as`.setGravity(false)
                                `as`.customName = "Kasa"
                                setKasaDataWithARmorStand(`as`, kdata)
                            /*
                             * if(c <= 4){ for (Player o_player :
                             * Main.getPlugin().getServer().getOnlinePlayers()) {
                             * ((CraftPlayer)o_player).getHandle().playerConnection.sendPacket(new
                             * PacketPlayOutEntityEquipment(as.getEntityId(), EnumItemSlot.HEAD,
                             * CraftItemStack.asNMSCopy(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))));
                             * } } if(c == 5){ for (Player o_player :
                             * Main.getPlugin().getServer().getOnlinePlayers()) {
                             * ((CraftPlayer)o_player).getHandle().playerConnection.sendPacket(new
                             * PacketPlayOutEntityEquipment(as.getEntityId(), EnumItemSlot.HEAD,
                             * CraftItemStack.asNMSCopy(new ItemStack(Material.END_ROD)))); } }
                             */
                                c++
                            }

                            as1!!.headPose = EulerAngle(Math.toRadians(350.0), 0.0, 0.0)
                            as2!!.headPose = EulerAngle(Math.toRadians(10.0), 0.0, 0.0)
                            as3!!.headPose = EulerAngle(Math.toRadians(350.0), 0.0, 0.0)
                            as4!!.headPose = EulerAngle(Math.toRadians(10.0), 0.0, 0.0)
                            as5!!.headPose = EulerAngle(Math.toRadians(30.0), 0.0, 0.0)
                        }

                        if (i != 0) {
                            if (p.isSneaking &&
                                kdata.damage <= 200 &&
                                weapon &&
                                p.gameMode == GameMode.ADVENTURE
                            ) {
                                as1!!.teleport(rl.clone().add(0.0, 0.2, 0.0))
                                as2!!.teleport(rl2.clone().add(0.0, -0.52, 0.0))
                                as3!!.teleport(ll.clone().add(0.0, 0.2, 0.0))
                                as4!!.teleport(ll2.clone().add(0.0, -0.52, 0.0))
                                as5!!.teleport(aml.clone().add(mvec.getX(), 0.35, mvec.getZ()))
                                as6!!.teleport(rl.clone().add(0.0, 1.0, 0.0))
                                as7!!.teleport(ll.clone().add(0.0, 1.0, 0.0))

                                if (i % 10 == 0) {
                                    armorStandItemDelay(list, p, kdata)
                                }
                            } else {
                                if (i % 5 == 0) {
                                    for (`as` in list) {
                                        for (o_player in plugin.server.onlinePlayers) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(ItemStack(Material.AIR)),
                                                ),
                                            )
                                        }
                                    }
                                    armorStandTeleportDelay(list, p, kdata)
                                }
                            }
                        }

                        if (i % 100 == 0) {
                            if (kdata.damage - 50 > 0) {
                                kdata.damage = (kdata.damage - 50)
                            } else {
                                kdata.damage = (0).toDouble()
                            }
                        }

                        if (p.gameMode == GameMode.SPECTATOR) kdata.damage = (0).toDouble()

                        if (!data.isInMatch || !p.isOnline) {
                            for (`as` in list) `as`.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }

        val bigktask: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var i: Int = 0
                var c: Int = 0
                var `is`: Boolean = true
                var pageCooltime: Int = getPlayerData(p)!!.weaponClass!!.mainWeapon!!.rollerWidth

                override fun run() {
                    if (p.isSneaking && `is` && p.gameMode != GameMode.SPECTATOR && !getPlayerData(p)!!.isUsingTyakuti) {
                        `is` = false
                        camping(p)
                        getPlayerData(p)!!.mainItemGlow = false
                        if (!getPlayerData(p)!!.isUsingSP) WeaponClassMgr.setWeaponClass(p)
                    }
                    if (!`is`) {
                        c++
                        if (c == pageCooltime) {
                            `is` = true
                            c = 0
                            getPlayerData(p)!!.mainItemGlow = true
                            if (!getPlayerData(p)!!.isUsingSP) WeaponClassMgr.setWeaponClass(p)
                        }
                    }
                    i++
                    if (!p.isOnline || !getPlayerData(p)!!.isInMatch) {
                        cancel()
                    }
                }
            }

        if (big) {
            bigktask.runTaskTimer(plugin, 0, 1)
        } else {
            task.runTaskTimer(plugin, 0, 1)
        }
    }

    fun camping(player: Player) {
        val kdata = KasaData(player)
        setKasaDataWithPlayer(player, kdata)
        player.world.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var i: Int = 0
                var bp: Boolean = false
                var squid: Boolean = true
                var kasaSpeed: Float = getPlayerData(p)!!.weaponClass!!.mainWeapon!!.rollerNeedInk
                var pageCooltime: Int = getPlayerData(p)!!.weaponClass!!.mainWeapon!!.rollerWidth

                var dir: Vector = Vector(1, 0, 0)

                var list: MutableList<ArmorStand> = ArrayList()
                var ul: MutableList<ArmorStand> = ArrayList()
                var dl: MutableList<ArmorStand> = ArrayList()

                var as1: ArmorStand? = null
                var as2: ArmorStand? = null
                var as3: ArmorStand? = null
                var as4: ArmorStand? = null
                var as5: ArmorStand? = null
                var as6: ArmorStand? = null
                var as7: ArmorStand? = null
                var as8: ArmorStand? = null
                var as9: ArmorStand? = null
                var as10: ArmorStand? = null
                var as11: ArmorStand? = null
                var as12: ArmorStand? = null
                var as13: ArmorStand? = null
                var as14: ArmorStand? = null
                var as15: ArmorStand? = null
                var as16: ArmorStand? = null
                var as17: ArmorStand? = null
                var as18: ArmorStand? = null
                var as19: ArmorStand? = null
                var as20: ArmorStand? = null

                // ArmorStand as21;
                // ArmorStand as22;
                var las: ArmorStand? = null

                override fun run() {
                    try {
                        var loc = p.location.add(0.0, -1.7, 0.0)

                        if (bp) {
                            if (las!!.isOnGround) las!!.velocity = dir.clone().multiply(kasaSpeed)
                            loc = las!!.location.add(0.0, -1.7, 0.0)
                        }
                        val pv = p.eyeLocation.direction.normalize()
                        var vec = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                        if (bp) vec = dir
                        val mvec = vec.clone().multiply(-1)
                        val floc = loc.add(vec.clone().multiply(1.5))
                        val vec1 = Vector(vec.getZ() * -1, 0.0, vec.getX())
                        val vec2 = Vector(vec.getZ(), 0.0, vec.getX() * -1)

                        val l1 = floc.clone().add(vec1.clone().multiply(0.6))
                        val l2 = floc.clone().add(vec1.clone().multiply(1.2))

                        val r1 = floc.clone().add(vec2.clone().multiply(0.6))
                        val r2 = floc.clone().add(vec2.clone().multiply(1.2))

                        if (i == 0) {
                            as1 =
                                p.world.spawnEntity(
                                    floc.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as2 =
                                p.world.spawnEntity(
                                    floc.clone().add(0.0, 0.6, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as3 =
                                p.world.spawnEntity(
                                    floc.clone().add(0.0, 1.2, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as4 =
                                p.world.spawnEntity(
                                    floc.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand

                            as5 =
                                p.world.spawnEntity(
                                    l1.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as6 =
                                p
                                    .world
                                    .spawnEntity(l1.clone().add(0.0, 0.6, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as7 =
                                p
                                    .world
                                    .spawnEntity(l1.clone().add(0.0, 1.2, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as8 =
                                p.world.spawnEntity(
                                    l1.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand

                            as9 =
                                p.world.spawnEntity(
                                    l2.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as10 =
                                p
                                    .world
                                    .spawnEntity(l2.clone().add(0.0, 0.6, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as11 =
                                p
                                    .world
                                    .spawnEntity(l2.clone().add(0.0, 1.2, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as12 =
                                p.world.spawnEntity(
                                    l2.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand

                            as13 =
                                p.world.spawnEntity(
                                    r1.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as14 =
                                p
                                    .world
                                    .spawnEntity(r1.clone().add(0.0, 0.6, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as15 =
                                p
                                    .world
                                    .spawnEntity(r1.clone().add(0.0, 1.2, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as16 =
                                p.world.spawnEntity(
                                    r1.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand

                            as17 =
                                p.world.spawnEntity(
                                    r2.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as18 =
                                p
                                    .world
                                    .spawnEntity(r2.clone().add(0.0, 0.6, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as19 =
                                p
                                    .world
                                    .spawnEntity(r2.clone().add(0.0, 1.2, 0.0), EntityType.ARMOR_STAND) as ArmorStand
                            as20 =
                                p.world.spawnEntity(
                                    r2.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand

                            list.add(as16!!)
                            list.add(as4!!)
                            list.add(as2!!)
                            list.add(as11!!)
                            list.add(as20!!)
                            list.add(as3!!)
                            list.add(as6!!)
                            list.add(as14!!)
                            list.add(as7!!)
                            list.add(as9!!)
                            list.add(as10!!)
                            list.add(as12!!)
                            list.add(as8!!)
                            list.add(as13!!)
                            list.add(as15!!)
                            list.add(as1!!)
                            list.add(as17!!)
                            list.add(as5!!)
                            list.add(as19!!)
                            list.add(as18!!)

                            dl.add(as1!!)
                            ul.add(as4!!)
                            dl.add(as5!!)
                            ul.add(as8!!)
                            dl.add(as9!!)
                            ul.add(as12!!)
                            dl.add(as13!!)
                            ul.add(as16!!)
                            dl.add(as17!!)
                            ul.add(as20!!)

                            val aslist: MutableList<ArmorStand> = ArrayList(list)
                            kdata.armorStandList = aslist
                            kdata.damage = (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.slideNeedINK).toDouble()

                            for (`as` in list) {
                                // as.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
                                `as`.setBasePlate(false)
                                `as`.isVisible = false
                                `as`.setGravity(false)
                                `as`.customName = "Kasa"
                                setKasaDataWithARmorStand(`as`, kdata)
                            }

                            for (`as` in ul) {
                                `as`.headPose = EulerAngle(Math.toRadians(160.0), 0.0, 0.0)
                            }

                            for (`as` in dl) {
                                `as`.headPose = EulerAngle(Math.toRadians(20.0), 0.0, 0.0)
                            }
                        }

                        if (i >= 0) {
                            as1!!.teleport(floc.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)))
                            as2!!.teleport(floc.clone().add(0.0, 0.6, 0.0))
                            as3!!.teleport(floc.clone().add(0.0, 1.2, 0.0))
                            as4!!.teleport(floc.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)))

                            as5!!.teleport(l1.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)))
                            as6!!.teleport(l1.clone().add(0.0, 0.6, 0.0))
                            as7!!.teleport(l1.clone().add(0.0, 1.2, 0.0))
                            as8!!.teleport(l1.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)))

                            as9!!.teleport(l2.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)))
                            as10!!.teleport(l2.clone().add(0.0, 0.6, 0.0))
                            as11!!.teleport(l2.clone().add(0.0, 1.2, 0.0))
                            as12!!.teleport(l2.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)))

                            as13!!.teleport(r1.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)))
                            as14!!.teleport(r1.clone().add(0.0, 0.6, 0.0))
                            as15!!.teleport(r1.clone().add(0.0, 1.2, 0.0))
                            as16!!.teleport(r1.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)))

                            as17!!.teleport(r2.clone().add(0.0, -0.05, 0.0).add(mvec.clone().multiply(0.38)))
                            as18!!.teleport(r2.clone().add(0.0, 0.6, 0.0))
                            as19!!.teleport(r2.clone().add(0.0, 1.2, 0.0))
                            as20!!.teleport(r2.clone().add(0.0, 3.15, 0.0).add(mvec.clone().multiply(0.9)))

                            if (i % 2 == 0) {
                                val asl = as4!!.location
                                val bd =
                                    getPlayerData(p)!!
                                        .team!!
                                        .teamColor!!
                                        .wool!!
                                        .createBlockData()
                                for (target in plugin.server.onlinePlayers) {
                                    if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk() &&
                                        target.world === p.world &&
                                        target
                                            .location
                                            .distanceSquared(asl) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        target.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            asl,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            1.0,
                                            bd,
                                        )
                                    }
                                }

                                for (`as` in ul) {
                                    PaintMgr.paintHightestBlock(`as`.location, p, false, false)
                                }
                            }

                            if (i % 3 == 0) {
                                for (`as` in dl) {
                                    val rayTrace = RayTrace(`as`.location.toVector(), Vector(0, 1, 0))
                                    val damage = 1.0
                                    for (target in plugin.server.onlinePlayers) {
                                        if (!getPlayerData(target)!!.isInMatch) continue
                                        if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                            target.gameMode == GameMode.ADVENTURE
                                        ) {
                                            if (rayTrace.intersects(BoundingBox(target as Entity), 5.0, 0.05)) {
                                                giveDamage(player, target, damage, "killed")

                                                // AntiNoDamageTime
                                                val task: BukkitRunnable =
                                                    object : BukkitRunnable() {
                                                        var p: Player = target

                                                        override fun run() {
                                                            target.noDamageTicks = 0
                                                        }
                                                    }
                                                task.runTaskLater(plugin, 1)
                                            }
                                        }
                                    }
                                }
                            }

                            var c = 1
                            for (`as` in list) {
                                val data = getPlayerData(player)
                                var team = data!!.match!!.team0
                                if (team == data.team) team = data.match!!.team1
                                for (o_player in plugin.server.onlinePlayers) {
                                    if (kdata.damage == 0.0) {
                                        (o_player as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                    ),
                                                ),
                                            )
                                    }
                                    if (kdata.damage > 0 && kdata.damage <= 50) {
                                        if (c == 1) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                        } else {
                                            (o_player as CraftPlayer)
                                                .handle
                                                .playerConnection
                                                .sendPacket(
                                                    PacketPlayOutEntityEquipment(
                                                        `as`.entityId,
                                                        EnumItemSlot.HEAD,
                                                        CraftItemStack.asNMSCopy(
                                                            ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                        ),
                                                    ),
                                                )
                                        }
                                    }
                                    if (kdata.damage > 50 && kdata.damage <= 100) {
                                        if (c <= 2) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                        } else {
                                            (o_player as CraftPlayer)
                                                .handle
                                                .playerConnection
                                                .sendPacket(
                                                    PacketPlayOutEntityEquipment(
                                                        `as`.entityId,
                                                        EnumItemSlot.HEAD,
                                                        CraftItemStack.asNMSCopy(
                                                            ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                        ),
                                                    ),
                                                )
                                        }
                                    }
                                    if (kdata.damage > 100 && kdata.damage <= 150) {
                                        if (c <= 3) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                        } else {
                                            (o_player as CraftPlayer)
                                                .handle
                                                .playerConnection
                                                .sendPacket(
                                                    PacketPlayOutEntityEquipment(
                                                        `as`.entityId,
                                                        EnumItemSlot.HEAD,
                                                        CraftItemStack.asNMSCopy(
                                                            ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                        ),
                                                    ),
                                                )
                                        }
                                    }
                                    if (kdata.damage > 150) {
                                        if (c <= 4) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                        } else {
                                            (o_player as CraftPlayer)
                                                .handle
                                                .playerConnection
                                                .sendPacket(
                                                    PacketPlayOutEntityEquipment(
                                                        `as`.entityId,
                                                        EnumItemSlot.HEAD,
                                                        CraftItemStack.asNMSCopy(
                                                            ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                        ),
                                                    ),
                                                )
                                        }
                                    }
                                }
                                c++
                            }
                        }

                        if ((
                                p
                                    .inventory
                                    .itemInMainHand
                                    .type == Material.AIR ||
                                    !p.isSneaking ||
                                    p.gameMode == GameMode.SPECTATOR
                            ) &&
                            squid &&
                            i < 39
                        ) {
                            squid = false
                            i = 39
                        }

                        if (i == 40) {
                            bp = true
                            dir = vec.clone().multiply(1)
                            las = p.world.spawnEntity(p.location, EntityType.ARMOR_STAND) as ArmorStand
                            las!!.isVisible = false
                            las!!.setGravity(true)
                            las!!.customName = "Kasa"
                            val l: MutableList<ArmorStand> = kdata.armorStandList
                            l.add(las!!)
                            kdata.armorStandList = l
                            setKasaDataWithARmorStand(las, kdata)
                            p.playNote(p.location, Instrument.STICKS, Note.flat(1, Note.Tone.C))
                            p.playSound(p.location, Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 1f, 1.2f)
                        }

                        if (i == pageCooltime + 20 || kdata.damage > 200 || !p.isOnline || !getPlayerData(p)!!.isInMatch) {
                            if (kdata.damage <= 200 && getPlayerData(p)!!.isInMatch) {
                                as1!!
                                    .world
                                    .playSound(as1!!.location, Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                            }

                            for (`as` in list) {
                                `as`.remove()
                            }
                            las!!.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun armorStandItemDelay(
        list: MutableList<ArmorStand>,
        player: Player?,
        kdata: KasaData,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    val data = getPlayerData(player)
                    var team = data!!.match!!.team0
                    if (team == data.team) team = data.match!!.team1

                    var c = 1
                    for (`as` in list) {
                        if (c <= 4) {
                            for (o_player in plugin.server.onlinePlayers) {
                                if (kdata.damage == 0.0) {
                                    (o_player as CraftPlayer)
                                        .handle
                                        .playerConnection
                                        .sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack
                                                    .asNMSCopy(ItemStack(Material.WHITE_STAINED_GLASS_PANE)),
                                            ),
                                        )
                                }
                                if (kdata.damage > 0 && kdata.damage <= 50) {
                                    if (c == 1) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                    } else {
                                        (o_player as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                    ),
                                                ),
                                            )
                                    }
                                }
                                if (kdata.damage > 50 && kdata.damage <= 100) {
                                    if (c <= 2) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                    } else {
                                        (o_player as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                    ),
                                                ),
                                            )
                                    }
                                }
                                if (kdata.damage > 100 && kdata.damage <= 150) {
                                    if (c <= 3) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                    } else {
                                        (o_player as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                    ),
                                                ),
                                            )
                                    }
                                }
                                if (kdata.damage > 150) {
                                    if (c <= 4) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                `as`.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                    } else {
                                        (o_player as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    `as`.entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(Material.WHITE_STAINED_GLASS_PANE),
                                                    ),
                                                ),
                                            )
                                    }
                                }
                            }
                        }

                        if (c == 5) {
                            for (o_player in plugin.server.onlinePlayers) {
                                (o_player as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            `as`.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(Material.END_ROD)),
                                        ),
                                    )
                            }
                        }

                        c++
                    }
                }
            }
        task.runTaskLater(plugin, 10)
    }

    fun armorStandTeleportDelay(
        list: MutableList<ArmorStand>,
        player: Player,
        kdata: KasaData?,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (`as` in list) `as`.teleport(player.location.add(0.0, 50.0, 0.0))
                }
            }
        task.runTaskLater(plugin, 3)
    }
}
