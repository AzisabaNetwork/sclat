package be4rjp.sclat.weapon

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object Hound {
    @JvmStatic
    fun houndRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    if (!data.isUsingManeuver && data.canShoot) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch &&
                            data.canRollerShoot
                        ) {
                            shoot(p)
                            data.canRollerShoot = false
                            houndCooltime(p)
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun houndCooltime(player: Player?) {
        val data = getPlayerData(player)
        val delay1: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.canRollerShoot = true
                }
            }
        delay1.runTaskLater(
            plugin,
            data!!
                .weaponClass!!
                .mainWeapon!!
                .coolTime
                .toLong(),
        )
    }

    @JvmStatic
    fun houndEXRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(p)

                override fun run() {
                    try {
                        if (!data!!.isInMatch || !p.isOnline) {
                            data!!.isSliding = false
                            cancel()
                            return
                        }
                        if (!data!!.isSneaking && data!!.isSliding) {
                            data!!.isSneaking = false
                            data!!.isSliding = false
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun shoot(player: Player) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        val pVector = player.eyeLocation.direction
        val vec =
            Vector(pVector.getX(), 0.0, pVector.getZ())
                .normalize()
                .multiply(data!!.weaponClass!!.mainWeapon!!.shootSpeed)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var aVec: Vector = vec.clone()
                var bloc: Location? = null
                var i: Int = 0
                var as1: ArmorStand? = null
                var heightdiff: Double = 0.0

                // 半径
                var maxDist: Double = 1.0
                var saveY: Double = 0.0
                var explodetick: Int = data.weaponClass?.mainWeapon!!.rollerShootQuantity
                var climbSpeed: Float = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.rollerNeedInk

                override fun run() {
                    try {
                        if (i == 0) {
                            saveY = player.location.y
                            player.exp -=
                                (
                                    data.weaponClass?.mainWeapon!!.needInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                                ).toFloat()

                            as1 =
                                player.world.spawn(
                                    player.location,
                                    ArmorStand::class.java,
                                ) { armorStand: ArmorStand ->
                                    armorStand.isVisible = false
                                    armorStand.isSmall = true
                                }
                            GlowingAPI.setGlowing(as1!!, player, true)
                            data.setArmorlist(as1)
                        }

                        val aloc = as1!!.location.add(0.0, -0.4, 0.0)
                        aloc.yaw = 90f
                        val as1l = as1!!.location

                        if (i >= 5) {
                            if ((bloc!!.x == as1l.x || bloc!!.z == as1l.z)) {
                                if (entityWallHit(as1!!, pVector)) {
                                    aVec = Vector(pVector.getX() * 0.03, climbSpeed.toDouble(), pVector.getZ() * 0.03)
                                } else {
                                    aVec = Vector(vec.getX(), -0.4, vec.getZ())
                                }
                                // 壁を塗る
                                for (i in 0..1) {
                                    val pLocs: MutableList<Location> = getSphere(as1l, i.toDouble(), 30)
                                    for (loc in pLocs) {
                                        PaintMgr.paint(loc, player, false)
                                    }
                                }
                            } else if (aVec.getY() > 0 && !entityWallHit(as1!!, pVector)) {
                                aVec = Vector(vec.getX(), 0.0, vec.getZ())
                            } else if (!as1!!.isOnGround) {
                                aVec = Vector(vec.getX(), -0.4, vec.getZ())
                            }
                        }

                        as1!!.velocity = aVec

                        PaintMgr.paintHightestBlock(as1l, player, false, true)

                        bloc = as1l.clone()

                        if (i >= explodetick - 20 && i <= explodetick - 10) {
                            if (i % 2 == 0) player.world.playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                        }

                        // エフェクト
                        if (i % 2 == 0) {
                            val bd =
                                data.team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.server.onlinePlayers) {
                                // if (DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb()){
                                if (target.world === player.world) {
                                    if (target.location.distanceSquared(as1l) < Sclat.particleRenderDistanceSquared) {
                                        target.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            as1l,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            1.0,
                                            bd,
                                        )
                                    }
                                }
                                // }
                            }
                        }

                        if (i == explodetick ||
                            !player.isOnline ||
                            !data.isInMatch ||
                            (
                                data.isSneaking &&
                                    data.getArmorlist(
                                        0,
                                    ) === as1 &&
                                    !data.isSliding
                            )
                        ) {
                            if (data.isSneaking) {
                                data.isSliding = true
                            }
                            heightdiff = as1!!.location.y - saveY
                            if (heightdiff > 7.9) {
                                maxDist = data.weaponClass?.mainWeapon!!.blasterExHankei + 2
                            } else if (1.8 < heightdiff && heightdiff <= 7.9) {
                                maxDist = data.weaponClass?.mainWeapon!!.blasterExHankei + 1
                            } else if (-1.5 <= heightdiff && heightdiff <= 1.8) {
                                maxDist = data.weaponClass?.mainWeapon!!.blasterExHankei
                            } else if (-10 <= heightdiff && heightdiff < -1.5) {
                                maxDist = data.weaponClass?.mainWeapon!!.blasterExHankei - 1
                                if (maxDist <= 0) {
                                    maxDist = 1.0
                                }
                            } else if (heightdiff < -10) {
                                maxDist = data.weaponClass?.mainWeapon!!.blasterExHankei - 2
                                if (maxDist <= 0) {
                                    maxDist = 1.0
                                }
                            }

                            data.subArmorlist(as1)

                            // 爆発音
                            player.world.playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(as1l, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(as1l, (maxDist / 2).toInt().toDouble(), player)

                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val pLocs: MutableList<Location> = getSphere(as1l, i.toDouble(), 20)
                                    for (loc in pLocs) {
                                        PaintMgr.paint(loc, player, false)
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
                                                    if (getPlayerData(kasaData!!.player)!!.team != data.team) {
                                                        as1!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.customName == "SplashShield") {
                                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                    if (getPlayerData(splashShieldData!!.player)!!.team != data.team) {
                                                        as1!!.remove()
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
                                    val damage =
                                        exdamage(
                                            heightdiff,
                                            maxDist - target.location.distance(as1l),
                                            data.weaponClass?.mainWeapon!!.blasterExDamage
                                                * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP),
                                        )
                                    if (data.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
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

                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(as1l) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        val damage =
                                            exdamage(
                                                heightdiff,
                                                maxDist - `as`.location.distance(as1l),
                                                data.weaponClass?.mainWeapon!!.blasterExDamage
                                                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP),
                                            )
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

                            as1!!.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        as1!!.remove()
                        data.subArmorlist(as1)
                        cancel()
                    }
                }
            }
        if (player.exp >
            (
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        ) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        // BukkitRunnable cooltime = new BukkitRunnable(){
        // @Override
        // public void run(){
        // DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
        // }
        // };
        // cooltime.runTaskLater(Main.getPlugin(), 10);
    }

    private fun entityWallHit(
        stand: ArmorStand,
        direction: Vector,
    ): Boolean {
        val entityLocation = stand.location.clone().add(Vector(0.0, 0.3, 0.0))
        val distance = 0.7 // レイの長さ
        // if (result != null && result.getHitBlockFace() != null) {
        // 壁に接触している場合の処理
        // 壁に接触していない場合の処理
        return stand.world.rayTraceBlocks(entityLocation, direction, distance) != null
    }

    private fun exdamage(
        heightDiff: Double,
        mag: Double,
        dm: Double,
    ): Double {
        if (7.9 < heightDiff) {
            return mag * dm * 0.7 + dm * 1.7
        } else if (3.9 < heightDiff && heightDiff <= 7.9) {
            return mag * dm * 0.8 + dm * 0.9
        } else if (1.8 < heightDiff && heightDiff <= 3.9) {
            return mag * dm * 0.9 + dm * 0.1
        } else if (-1.5 <= heightDiff && heightDiff <= 1.8) {
            return mag * dm * 0.9 + dm * 0.25
        } else if (-5 <= heightDiff && heightDiff < -1.5) {
            return mag * dm * 0.5 + dm * 0.1
        } else if (-10 <= heightDiff && heightDiff < -5) {
            return mag * dm * 0.3
        } else if (heightDiff < -10) {
            return mag * dm * 0.2
        } else {
            return 0.0
        }
    }
}
