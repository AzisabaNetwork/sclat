package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr
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

object Boomerang {
    @JvmStatic
    fun BoomerangRunnable(player: Player) {
        val pVector = player.getEyeLocation().getDirection()
        val vec = Vector(pVector.getX(), pVector.getY(), pVector.getZ()).normalize().multiply(0.6)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var aVec: Vector = vec.clone()
                var bloc: Location? = null
                var i: Int = 0
                lateinit var as1: ArmorStand
                lateinit var as2: ArmorStand
                lateinit var as3: ArmorStand
                var fb: FallingBlock? = null
                var cumbackBoomeran: Boolean = false
                var cumbacktime: Int = 90
                var explode: Boolean = false

                override fun run() {
                    try {
                        if (i == 0) {
                            cumbackBoomeran = false
                            if (!getPlayerData(player)!!.getIsBombRush()) player.setExp(player.getExp() - 0.59f)

                            as1 =
                                player.getWorld().spawn<ArmorStand>(
                                    player.getLocation().add(0.0, 1.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setVisible(false)
                                        armorStand.setSmall(true)
                                    },
                                )
                            as2 =
                                player.getWorld().spawn<ArmorStand>(
                                    player.getLocation().add(0.0, 1.6, 0.0),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setVisible(false)
                                        armorStand.setGravity(false)
                                        armorStand.setMarker(true)
                                        armorStand.setSmall(true)
                                    },
                                )
                            val loc = player.getLocation().add(0.0, 0.8, 0.0)
                            loc.setYaw(90f)
                            as3 =
                                player
                                    .getWorld()
                                    .spawn<ArmorStand>(
                                        loc,
                                        ArmorStand::class.java,
                                        Consumer { armorStand: ArmorStand ->
                                            armorStand.setVisible(false)
                                            armorStand.setGravity(false)
                                            armorStand.setSmall(true)
                                        },
                                    )

                            fb =
                                player.getWorld().spawnFallingBlock(
                                    player.getLocation(),
                                    Material.WHITE_CARPET.createBlockData(),
                                )
                            fb!!.setGravity(false)
                            fb!!.setDropItem(false)
                            fb!!.setHurtEntities(false)

                            as2!!.addPassenger(fb!!)
                        }

                        val aloc = as1!!.getLocation().add(0.0, -0.8, 0.0)
                        aloc.setYaw(90f)
                        val as1l = as1!!.getLocation()
                        (as2 as CraftArmorStand).getHandle().setPositionRotation(
                            as1l.getX(),
                            as1l.getY(),
                            as1l.getZ(),
                            0f,
                            0f,
                        )
                        as3!!.teleport(aloc)
                        fb!!.setTicksLived(1)

                        if (i >= 5 && !cumbackBoomeran) {
                            if (bloc!!.getX() == as1l.getX() && bloc!!.getZ() != as1l.getZ() ||
                                bloc!!.getZ() == as1l.getZ() && bloc!!.getX() != as1l.getX()
                            ) {
                                aVec =
                                    Vector(
                                        player.getLocation().getX() - bloc!!.getX(),
                                        (player.getLocation().getY() + 1.6) - bloc!!.getY(),
                                        player.getLocation().getZ() - bloc!!.getZ(),
                                    ).normalize().multiply(1.0)
                                cumbackBoomeran = true
                                cumbacktime = i
                                for (painti in 0..2) {
                                    val p_locs: MutableList<Location> = getSphere(as1l, painti.toDouble(), 20)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, player, false)
                                    }
                                }
                            } else if (as1!!.isOnGround()) {
                                aVec =
                                    Vector(
                                        player.getLocation().getX() - bloc!!.getX(),
                                        (player.getLocation().getY() + 1.6) - bloc!!.getY(),
                                        player.getLocation().getZ() - bloc!!.getZ(),
                                    ).normalize().multiply(1.0)
                                cumbackBoomeran = true
                                cumbacktime = i
                            } else if (i == 30) {
                                aVec =
                                    Vector(
                                        player.getLocation().getX() - bloc!!.getX(),
                                        (player.getLocation().getY() + 1.6) - bloc!!.getY(),
                                        player.getLocation().getZ() - bloc!!.getZ(),
                                    ).normalize().multiply(1.0)
                                cumbackBoomeran = true
                                cumbacktime = 35
                            }
                        }
                        if (i >= cumbacktime + 5) {
                            if (bloc!!.getX() == as1l.getX() && bloc!!.getZ() != as1l.getZ() ||
                                bloc!!.getZ() == as1l.getZ() && bloc!!.getX() != as1l.getX()
                            ) {
                                explode = true
                            }
                        }
                        if (i >= cumbacktime + 15) {
                            explode = true
                        }
                        as1!!.setVelocity(aVec)

                        PaintMgr.PaintHightestBlock(as1l, player, false, true)

                        bloc = as1l.clone()

                        if (i % 10 == 0) {
                            for (o_player in plugin
                                .getServer()
                                .getOnlinePlayers()) {
                                (o_player as CraftPlayer)
                                    .getHandle()
                                    .playerConnection
                                    .sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            as3!!.getEntityId(),
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(
                                                ItemStack(
                                                    getPlayerData(player)!!.team.teamColor!!.wool!!,
                                                ),
                                            ),
                                        ),
                                    )
                            }
                        }

                        if (i >= cumbacktime + 3 && i <= cumbacktime + 13) {
                            if (i % 2 == 0) player.getWorld().playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                        }

                        // エフェクト
                        if (i % 2 == 0) {
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(target)!!.settings.ShowEffect_Bomb()) {
                                    if (target.getWorld() ===
                                        player.getWorld()
                                    ) {
                                        if (target
                                                .getLocation()
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
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(target)!!.settings.ShowEffect_Bomb()) {
                                    if (target.getWorld() === player.getWorld()) {
                                        if (target.getLocation().distance(as1l) <= 1.2) {
                                            val damage = 0.2
                                            if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
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
                                }
                            }

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(as1l) <= 1.2) {
                                    if (`as` is ArmorStand) {
                                        val damage = 0.2
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        if (`as`.getCustomName() != null) {
                                            if (`as`.getCustomName() == "SplashShield" ||
                                                `as`.getCustomName() == "Kasa"
                                            ) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (i == 90 || !player.isOnline() || !getPlayerData(player)!!.isInMatch() || explode) {
                            // 半径
                            val maxDist = 3.0

                            // 爆発音
                            player.getWorld().playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(as1l, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(as1l, maxDist, player)

                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val p_locs: MutableList<Location> = getSphere(as1l, i.toDouble(), 20)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, player, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(as1l) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.getCustomName() != null) {
                                            try {
                                                if (`as`.getCustomName() == "Kasa") {
                                                    val kasaData = getKasaDataFromArmorStand(`as`)
                                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                                        DataMgr
                                                            .getPlayerData(player)!!
                                                            .team
                                                    ) {
                                                        as1!!.remove()
                                                        as2.remove()
                                                        as3!!.remove()
                                                        fb!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.getCustomName() == "SplashShield") {
                                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                        DataMgr
                                                            .getPlayerData(player)!!
                                                            .team
                                                    ) {
                                                        as1!!.remove()
                                                        as2.remove()
                                                        as3!!.remove()
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

                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== player.getWorld()) continue
                                if (target.getLocation().distance(as1l) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.getLocation().distance(as1l)) * 1 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
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

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(as1l) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        val damage = (
                                            (maxDist - `as`.getLocation().distance(as1l)) * 1 *
                                                Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                            )
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        if (`as`.getCustomName() != null) {
                                            if (`as`.getCustomName() == "SplashShield" ||
                                                `as`.getCustomName() == "Kasa"
                                            ) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            as1!!.remove()
                            as2.remove()
                            as3!!.remove()
                            fb!!.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        as1!!.remove()
                        as2!!.remove()
                        as3!!.remove()
                        fb!!.remove()
                        cancel()
                    }
                }
            }
        if (player.getExp() > 0.6 || getPlayerData(player)!!.getIsBombRush()) {
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
}
