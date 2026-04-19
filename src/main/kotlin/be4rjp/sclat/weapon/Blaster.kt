package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.extension.armorstands
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.shape.Sphere.getSphere
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Blaster {
    @JvmStatic
    fun shootBlaster(player: Player) {
        val data = getPlayerData(player)
        val mainWeapon = data?.weaponClass?.mainWeapon ?: return
        if (data.canRollerShoot) {
            val delay1: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        val data = getPlayerData(player)
                        data!!.canRollerShoot = true
                    }
                }
            delay1.runTaskLater(plugin, mainWeapon.coolTime.toLong())

            val delay: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        shoot(player)
                    }
                }
            delay.runTaskLater(plugin, mainWeapon.delay.toLong())
            data.canRollerShoot = false
        }
    }

    fun shoot(player: Player) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        val mainWeapon = data?.weaponClass?.mainWeapon!!
        data.canRollerShoot = false
        if (player.exp <=
            (
                mainWeapon.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.exp -=
            (
                mainWeapon.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team?.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass?.mainWeapon!!.shootSpeed)
        val random = getPlayerData(player)!!.weaponClass?.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass?.mainWeapon!!.distanceTick
        if (!player.isOnGround) {
            vec.add(
                Vector(
                    Math.random() * random - random / 2,
                    0.0,
                    Math.random() * random - random / 2,
                ),
            )
        }
        ball.velocity = vec
        ball.shooter = player
        ball.setGravity(false)
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        DataMgr.tsl.add(name)
        ball.customName = name
        mainSnowballNameMap[name] = ball
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick

                // Vector fallvec;
                var origvec: Vector = vec
                var inkball: Snowball? = ball
                var p: Player = player
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass?.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    val bd =
                        getPlayerData(p)!!
                            .team
                            ?.teamColor!!
                            .wool!!
                            .createBlockData()
                    for (oPlayer in plugin.server.onlinePlayers) {
                        if (getPlayerData(oPlayer)!!.settings?.showEffectMainWeaponInk()!!) {
                            if (oPlayer.world ===
                                inkball!!.world
                            ) {
                                if (oPlayer
                                        .location
                                        .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    oPlayer.spawnParticle<BlockData?>(
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

                    if (i >= tick && !inkball!!.isDead) {
                        // 半径
                        val maxDist = mainWeapon.blasterExHankei

                        // 爆発音
                        player.world.playSound(inkball!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(inkball!!.location, maxDist, 25, player)

                        // バリアをはじく
                        repelBarrier(inkball!!.location, maxDist, player)

                        // 塗る
                        run {
                            var i = 0
                            while (i <= maxDist - 1) {
                                val pLocs = getSphere(inkball!!.location, i.toDouble(), 20)
                                for (loc in pLocs) {
                                    PaintMgr.paint(loc, p, false)
                                    PaintMgr.paintHightestBlock(loc, p, false, false)
                                }
                                i++
                            }
                        }

                        // 攻撃判定の処理
                        for (armorStand in player.world.armorstands) {
                            if (armorStand.customName != null) {
                                if (armorStand.location.distanceSquared(ball.location) <= maxDist * maxDist) {
                                    try {
                                        if (armorStand.customName == "Kasa") {
                                            val kasaData = getKasaDataFromArmorStand(armorStand)
                                            if (getPlayerData(kasaData!!.player)!!.team !=
                                                getPlayerData(p)!!
                                                    .team
                                            ) {
                                                inkball!!.remove()
                                                cancel()
                                            }
                                        } else if (armorStand.customName == "SplashShield") {
                                            val splashShieldData = getSplashShieldDataFromArmorStand(armorStand)
                                            if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                getPlayerData(p)!!
                                                    .team
                                            ) {
                                                inkball!!.remove()
                                                cancel()
                                            }
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }

                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.isInMatch) continue
                            if (target.location.distance(inkball!!.location) <= maxDist + 1) {
                                var damage = 10.0
                                damage =
                                    if (mainWeapon.isManeuver) {
                                        mainWeapon.blasterExDamage
                                    } else {
                                        (
                                            (maxDist + 1 - target.location.distance(inkball!!.location)) *
                                                mainWeapon.blasterExDamage
                                        )
                                    }
                                if (damage > mainWeapon.damage) {
                                    damage = mainWeapon.damage
                                }
                                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                    target.gameMode == GameMode.ADVENTURE
                                ) {
                                    giveDamage(player, target, damage, "killed")

                                    // AntiNoDamageTime
                                    val task: BukkitRunnable =
                                        object : BukkitRunnable() {
                                            override fun run() {
                                                target.noDamageTicks = 0
                                            }
                                        }
                                    task.runTaskLater(plugin, 1)
                                }
                            }
                        }

                        for (entity in player.world.armorstands) {
                            if (entity.location.distanceSquared(inkball!!.location) <= (maxDist + 1) *
                                (maxDist + 1)
                            ) {
                                var damage = (
                                    (maxDist + 1 - entity.location.distance(inkball!!.location)) *
                                        mainWeapon.blasterExDamage
                                )
                                if (damage > mainWeapon.damage) {
                                    damage = mainWeapon.damage
                                }
                                ArmorStandMgr.giveDamageArmorStand(entity, damage, p)
                            }
                        }

                        inkball!!.remove()
                    }
                    if (i != tick) PaintMgr.paintHightestBlock(inkball!!.location, p, false, true)
                    if (inkball!!.isDead) cancel()
                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun explode(
        player: Player,
        blastcenter: Location,
    ) {
        val data = getPlayerData(player)
        // 半径
        val mainWeapon = data!!.weaponClass?.mainWeapon ?: return
        val maxDist = mainWeapon.blasterExHankei

        // 爆発音
        player.world.playSound(blastcenter, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

        // 爆発エフェクト
        createInkExplosionEffect(blastcenter, maxDist, 25, player)

        // バリアをはじく
        repelBarrier(blastcenter, maxDist, player)

        // 塗る
        var i = 0
        while (i <= maxDist - 1) {
            val pLocs = getSphere(blastcenter, i.toDouble(), 20)
            for (loc in pLocs) {
                PaintMgr.paint(loc, player, false)
                PaintMgr.paintHightestBlock(loc, player, false, false)
            }
            i++
        }

        // 攻撃判定の処理
//        for (entity in player.world.entities) {
//            if (entity !is ArmorStand) return
//            val entityName = entity.customName ?: return
//            if (entity.location.distanceSquared(blastcenter) > (maxDist + 1) * (maxDist + 1)) return
//            try {
//                when (entityName) {
//                    "Kasa" -> {
//                        val kasaData = DataMgr.getKasaDataFromArmorStand(entity)
//                        if(DataMgr.getPlayerData(kasaData.player).team != DataMgr.getPlayerData(player).team) {
//                            cancel()
//                        }
//                    }
//
//                    "SplashShield" -> {
//                        val splashShieldData = DataMgr.getSplashShieldDataFromArmorStand(entity)
//                        if(DataMgr.getPlayerData(splashShieldData.player).team != DataMgr.getPlayerData(player).team) {
//                            cancel()
//                        }
//                    }
//
//                    else -> {}
//                }
//            } catch (_: Exception) {
//            }
//        }

        for (target in plugin.server.onlinePlayers) {
            if (!getPlayerData(target)!!.isInMatch) continue
            if (target.location.distance(blastcenter) <= maxDist + 1) {
                var damage = 10.0
                damage =
                    if (mainWeapon.isManeuver) {
                        mainWeapon.blasterExDamage
                    } else {
                        (
                            (maxDist - target.location.distance(blastcenter)) *
                                mainWeapon.blasterExDamage * 0.4
                        )
                    }
                if (damage > mainWeapon.damage) {
                    damage = mainWeapon.damage
                }
                if (damage < 0.1) {
                    damage = 0.1
                }
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
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

        for (armorStand in player.world.getEntitiesByClasses(ArmorStand::class.java)) {
            if (armorStand !is ArmorStand) return // Todo: may be its not need.
            if (armorStand.location.distanceSquared(blastcenter) > (maxDist + 1) * (maxDist + 1)) return
            try {
                var damage = (
                    (maxDist + 1 - armorStand.location.distance(blastcenter)) *
                        mainWeapon.blasterExDamage
                )
                if (damage > mainWeapon.damage) {
                    damage = mainWeapon.damage
                }
                if (armorStand.customName == "Kasa") {
                    val kasaData = getKasaDataFromArmorStand(armorStand)
                    if (getPlayerData(kasaData!!.player)!!.team != getPlayerData(player)!!.team) {
                        ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
                    }
                } else if (armorStand.customName == "SplashShield") {
                    val splashShieldData = getSplashShieldDataFromArmorStand(armorStand)
                    if (getPlayerData(splashShieldData!!.player)!!.team != getPlayerData(player)!!.team) {
                        ArmorStandMgr.giveDamageArmorStand(armorStand, damage, player)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
