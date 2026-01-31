package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Consumer
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object CurlingBomb {
    @JvmStatic
    fun curlingBombRunnable(player: Player) {
        val pVector = player.eyeLocation.direction
        val vec = Vector(pVector.getX(), 0.0, pVector.getZ()).normalize().multiply(0.5)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var aVec: Vector = vec.clone()
                var bloc: Location? = null
                var i: Int = 0
                lateinit var as1: ArmorStand
                lateinit var as2: ArmorStand
                lateinit var as3: ArmorStand
                var fb: FallingBlock? = null

                override fun run() {
                    try {
                        if (i == 0) {
                            if (!getPlayerData(player)!!.isBombRush) player.exp = player.exp - 0.59f

                            as1 =
                                player.world.spawn<ArmorStand>(
                                    player.location,
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.isVisible = false
                                        armorStand.isSmall = true
                                    },
                                )
                            as2 =
                                player.world.spawn<ArmorStand>(
                                    player.location.add(0.0, 0.0, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.isVisible = false
                                        armorStand.setGravity(false)
                                        armorStand.isMarker = true
                                    },
                                )
                            val loc = player.location.add(0.0, -0.4, 0.0)
                            loc.yaw = 90f
                            as3 =
                                player
                                    .world
                                    .spawn<ArmorStand>(
                                        loc,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.isVisible = false
                                            armorStand.setGravity(false)
                                            armorStand.isSmall = true
                                        },
                                    )

                            fb =
                                player.world.spawnFallingBlock(
                                    player.location,
                                    Material.QUARTZ_SLAB.createBlockData(),
                                )
                            fb!!.setGravity(false)
                            fb!!.dropItem = false
                            fb!!.setHurtEntities(false)

                            as2.addPassenger(fb!!)
                        }

                        val aloc = as1.location.add(0.0, -0.4, 0.0)
                        aloc.yaw = 90f
                        val as1l = as1.location
                        (as2 as CraftArmorStand).handle.setPositionRotation(
                            as1l.x,
                            as1l.y,
                            as1l.z,
                            0f,
                            0f,
                        )
                        as3.teleport(aloc)
                        fb!!.ticksLived = 1

                        if (i >= 10 && as1.isOnGround) {
                            if (bloc!!.x == as1l.x && bloc!!.z != as1l.z) {
                                aVec =
                                    Vector(aVec.getX() * -1, 0.0, aVec.getZ())
                            }
                            if (bloc!!.z == as1l.z && bloc!!.x != as1l.x) {
                                aVec =
                                    Vector(aVec.getX(), 0.0, aVec.getZ() * -1)
                            }
                        }

                        if (as1.isOnGround) as1.velocity = aVec

                        PaintMgr.PaintHightestBlock(as1l, player, false, true)

                        bloc = as1l.clone()

                        if (i % 10 == 0) {
                            for (o_player in plugin
                                .server
                                .onlinePlayers) {
                                (o_player as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            as3.entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(
                                                ItemStack(
                                                    getPlayerData(player)!!.team!!.teamColor!!.wool!!,
                                                ),
                                            ),
                                        ),
                                    )
                            }
                        }

                        if (i >= 70 && i <= 80) {
                            if (i % 2 == 0) player.world.playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                        }

                        // エフェクト
                        if (i % 2 == 0) {
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.server.onlinePlayers) {
                                if (getPlayerData(target)!!.settings!!.ShowEffect_Bomb()) {
                                    if (target.world ===
                                        player.world
                                    ) {
                                        if (target
                                                .location
                                                .distanceSquared(as1l) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            target.spawnParticle<BlockData?>(
                                                Particle.BLOCK_DUST,
                                                as1l,
                                                2,
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
                            // 攻撃判定
                            for (target in plugin.server.onlinePlayers) {
                                if (getPlayerData(target)!!.settings!!.ShowEffect_Bomb()) {
                                    if (target.world === player.world) {
                                        if (target.location.distance(as1l) <= 1.2) {
                                            val damage = 2.0
                                            if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                                target.gameMode == GameMode.ADVENTURE
                                            ) {
                                                giveDamage(player, target, damage, "subWeapon")

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

                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(as1l) <= 1.2) {
                                    if (`as` is ArmorStand) {
                                        val damage = 2.0
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        if (`as`.customName != null) {
                                            if (`as`.customName == "SplashShield" ||
                                                `as`.customName == "Kasa"
                                            ) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (i == 90 || !player.isOnline || !getPlayerData(player)!!.isInMatch) {
                            // 半径
                            val maxDist = 3.0

                            // 爆発音
                            player.world.playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(as1l, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(as1l, maxDist, player)

                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val pLocs: MutableList<Location> = getSphere(as1l, i.toDouble(), 20)
                                    for (loc in pLocs) {
                                        PaintMgr.Paint(loc, player, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(as1l) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            try {
                                                if (`as`.customName == "Kasa") {
                                                    val kasaData = getKasaDataFromArmorStand(`as`)
                                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                                        getPlayerData(player)!!
                                                            .team
                                                    ) {
                                                        as1.remove()
                                                        as2.remove()
                                                        as3.remove()
                                                        fb!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.customName == "SplashShield") {
                                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                        getPlayerData(player)!!
                                                            .team
                                                    ) {
                                                        as1.remove()
                                                        as2.remove()
                                                        as3.remove()
                                                        fb!!.remove()
                                                        cancel()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                            }
                                        }
                                    }
                                }
                            }

                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== player.world) continue
                                if (target.location.distance(as1l) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.location.distance(as1l)) * 4 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "subWeapon")

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

                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(as1l) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        val damage = (maxDist - `as`.location.distance(as1l)) * 7
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        if (`as`.customName != null) {
                                            if (`as`.customName == "SplashShield" ||
                                                `as`.customName == "Kasa"
                                            ) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            as1.remove()
                            as2.remove()
                            as3.remove()
                            fb!!.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        as1.remove()
                        as2.remove()
                        as3.remove()
                        fb!!.remove()
                        cancel()
                    }
                }
            }
        if (player.exp > 0.6 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 10)
    }
}
