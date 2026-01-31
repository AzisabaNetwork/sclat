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
import org.bukkit.util.Consumer
import org.bukkit.util.Vector

object Hound {
    @JvmStatic
    fun HoundRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }

                    if (!data.getIsUsingManeuver() && data.getCanShoot()) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch() &&
                            data.getCanRollerShoot()
                        ) {
                            Shoot(p)
                            data.setCanRollerShoot(false)
                            HoundCooltime(p)
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .getWeaponClass()
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun HoundCooltime(player: Player?) {
        val data = getPlayerData(player)
        val delay1: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.setCanRollerShoot(true)
                }
            }
        delay1.runTaskLater(
            plugin,
            data!!
                .getWeaponClass()
                .mainWeapon!!
                .coolTime
                .toLong(),
        )
    }

    @JvmStatic
    fun HoundEXRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(p)

                override fun run() {
                    try {
                        if (!data!!.isInMatch() || !p.isOnline()) {
                            data!!.setIsSliding(false)
                            cancel()
                            return
                        }
                        if (!data!!.getIsSneaking() && data!!.getIsSliding()) {
                            data!!.setIsSneaking(false)
                            data!!.setIsSliding(false)
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun Shoot(player: Player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        val pVector = player.getEyeLocation().getDirection()
        val vec =
            Vector(pVector.getX(), 0.0, pVector.getZ())
                .normalize()
                .multiply(data!!.getWeaponClass().mainWeapon!!.shootSpeed)
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
                var explodetick: Int = data.getWeaponClass().mainWeapon!!.rollerShootQuantity
                var climbSpeed: Float = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.rollerNeedInk

                override fun run() {
                    try {
                        if (i == 0) {
                            saveY = player.getLocation().getY()
                            player.setExp(
                                player.getExp() -
                                    (
                                        data.getWeaponClass().mainWeapon!!.needInk
                                            * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                            Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                                        ).toFloat(),
                            )

                            as1 =
                                player.getWorld().spawn<ArmorStand>(
                                    player.getLocation(),
                                    ArmorStand::class.java,
                                    Consumer { armorStand: ArmorStand ->
                                        armorStand.setVisible(false)
                                        armorStand.setSmall(true)
                                    },
                                )
                            GlowingAPI.setGlowing(as1!!, player, true)
                            data.setArmorlist(as1)
                        }

                        val aloc = as1!!.getLocation().add(0.0, -0.4, 0.0)
                        aloc.setYaw(90f)
                        val as1l = as1!!.getLocation()

                        if (i >= 5) {
                            if ((bloc!!.getX() == as1l.getX() || bloc!!.getZ() == as1l.getZ())) {
                                if (Hound.EntityWallHit(as1!!, pVector)) {
                                    aVec = Vector(pVector.getX() * 0.03, climbSpeed.toDouble(), pVector.getZ() * 0.03)
                                } else {
                                    aVec = Vector(vec.getX(), -0.4, vec.getZ())
                                }
                                // 壁を塗る
                                for (i in 0..1) {
                                    val p_locs: MutableList<Location> = getSphere(as1l, i.toDouble(), 30)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, player, false)
                                    }
                                }
                            } else if (aVec.getY() > 0 && !Hound.EntityWallHit(as1!!, pVector)) {
                                aVec = Vector(vec.getX(), 0.0, vec.getZ())
                            } else if (!as1!!.isOnGround()) {
                                aVec = Vector(vec.getX(), -0.4, vec.getZ())
                            }
                        }

                        as1!!.setVelocity(aVec)

                        PaintMgr.PaintHightestBlock(as1l, player, false, true)

                        bloc = as1l.clone()

                        if (i >= explodetick - 20 && i <= explodetick - 10) {
                            if (i % 2 == 0) player.getWorld().playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                        }

                        // エフェクト
                        if (i % 2 == 0) {
                            val bd =
                                data.team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                // if (DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb()){
                                if (target.getWorld() === player.getWorld()) {
                                    if (target.getLocation().distanceSquared(as1l) < Sclat.particleRenderDistanceSquared) {
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

                        if (i == explodetick || !player.isOnline() || !data.isInMatch() || (
                                data.getIsSneaking() && data.getArmorlist(
                                    0,
                                ) === as1 && !data.getIsSliding()
                                )
                        ) {
                            if (data.getIsSneaking()) {
                                data.setIsSliding(true)
                            }
                            heightdiff = as1!!.getLocation().getY() - saveY
                            if (heightdiff > 7.9) {
                                maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei + 2
                            } else if (1.8 < heightdiff && heightdiff <= 7.9) {
                                maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei + 1
                            } else if (-1.5 <= heightdiff && heightdiff <= 1.8) {
                                maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei
                            } else if (-10 <= heightdiff && heightdiff < -1.5) {
                                maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei - 1
                                if (maxDist <= 0) {
                                    maxDist = 1.0
                                }
                            } else if (heightdiff < -10) {
                                maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei - 2
                                if (maxDist <= 0) {
                                    maxDist = 1.0
                                }
                            }

                            data.subArmorlist(as1)

                            // 爆発音
                            player.getWorld().playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(as1l, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(as1l, (maxDist / 2).toInt().toDouble(), player)

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
                                                    if (getPlayerData(kasaData!!.player)!!.team != data.team) {
                                                        as1!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.getCustomName() == "SplashShield") {
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

                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== player.getWorld()) continue
                                if (target.getLocation().distance(as1l) <= maxDist) {
                                    val damage =
                                        exdamage(
                                            heightdiff,
                                            maxDist - target.getLocation().distance(as1l),
                                            data.getWeaponClass().mainWeapon!!.blasterExDamage
                                                * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP),
                                        )
                                    if (data.team != getPlayerData(target)!!.team &&
                                        target.getGameMode() == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "killed")

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
                                        val damage =
                                            exdamage(
                                                heightdiff,
                                                maxDist - `as`.getLocation().distance(as1l),
                                                data.getWeaponClass().mainWeapon!!.blasterExDamage
                                                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP),
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
        if (player.getExp() >
            (
                data.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        // BukkitRunnable cooltime = new BukkitRunnable(){
        // @Override
        // public void run(){
        // DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
        // }
        // };
        // cooltime.runTaskLater(Main.getPlugin(), 10);
    }

    private fun EntityWallHit(
        stand: ArmorStand,
        direction: Vector,
    ): Boolean {
        val entityLocation = stand.getLocation().clone().add(Vector(0.0, 0.3, 0.0))
        val distance = 0.7 // レイの長さ
        // if (result != null && result.getHitBlockFace() != null) {
        // 壁に接触している場合の処理
        // 壁に接触していない場合の処理
        return stand.getWorld().rayTraceBlocks(entityLocation, direction, distance) != null
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
