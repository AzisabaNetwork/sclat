package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
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
    fun ShootBlaster(player: Player) {
        val data = getPlayerData(player)
        val delay1: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.canRollerShoot = true
                }
            }
        if (data!!.canRollerShoot) {
            delay1.runTaskLater(
                plugin,
                data
                    .weaponClass
                    .mainWeapon!!
                    .coolTime
                    .toLong(),
            )
        }

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    Shoot(player)
                }
            }
        if (data.canRollerShoot) {
            delay.runTaskLater(
                plugin,
                data
                    .weaponClass
                    .mainWeapon!!
                    .delay
                    .toLong(),
            )
            data.canRollerShoot = false
        }
    }

    fun Shoot(player: Player) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        data!!.canRollerShoot = false
        if (player.exp <=
            (
                data.weaponClass.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.exp = player.exp -
            (
                data.weaponClass.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).handle.item = CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass.mainWeapon!!.shootSpeed)
        val random = getPlayerData(player)!!.weaponClass.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass.mainWeapon!!.distanceTick
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
        mainSnowballNameMap.put(name, ball)
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
                    ).multiply(getPlayerData(p)!!.weaponClass.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    val bd =
                        getPlayerData(p)!!
                            .team.teamColor!!
                            .wool!!
                            .createBlockData()
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings.ShowEffect_MainWeaponInk()) {
                            if (o_player.world ===
                                inkball!!.world
                            ) {
                                if (o_player
                                        .location
                                        .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    o_player.spawnParticle<BlockData?>(
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
                        val maxDist = data.weaponClass.mainWeapon!!.blasterExHankei

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
                                val p_locs = getSphere(inkball!!.location, i.toDouble(), 20)
                                for (loc in p_locs) {
                                    PaintMgr.Paint(loc, p, false)
                                    PaintMgr.PaintHightestBlock(loc, p, false, false)
                                }
                                i++
                            }
                        }

                        // 攻撃判定の処理
                        for (`as` in player.world.entities) {
                            if (`as` is ArmorStand) {
                                if (`as`.customName != null) {
                                    if (`as`.location.distanceSquared(ball.location) <= maxDist * maxDist) {
                                        try {
                                            if (`as`.customName == "Kasa") {
                                                val kasaData = getKasaDataFromArmorStand(`as`)
                                                if (getPlayerData(kasaData!!.player)!!.team !=
                                                    getPlayerData(p)!!
                                                        .team
                                                ) {
                                                    inkball!!.remove()
                                                    cancel()
                                                }
                                            } else if (`as`.customName == "SplashShield") {
                                                val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
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
                        }

                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.isInMatch) continue
                            if (target.location.distance(inkball!!.location) <= maxDist + 1) {
                                var damage = 10.0
                                if (data.weaponClass.mainWeapon!!.isManeuver) {
                                    damage =
                                        data.weaponClass.mainWeapon!!.blasterExDamage
                                } else {
                                    damage = (
                                        (maxDist + 1 - target.location.distance(inkball!!.location)) *
                                            data.weaponClass.mainWeapon!!.blasterExDamage
                                        )
                                }
                                if (damage > data.weaponClass.mainWeapon!!.damage) {
                                    damage = data.weaponClass.mainWeapon!!.damage
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

                        for (`as` in player.world.entities) {
                            if (`as` is ArmorStand) {
                                if (`as`.location.distanceSquared(inkball!!.location) <= (maxDist + 1) *
                                    (maxDist + 1)
                                ) {
                                    var damage = (
                                        (maxDist + 1 - `as`.location.distance(inkball!!.location)) *
                                            data.weaponClass.mainWeapon!!.blasterExDamage
                                        )
                                    if (damage > data.weaponClass.mainWeapon!!.damage) {
                                        damage = data.weaponClass.mainWeapon!!.damage
                                    }
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                        }

                        inkball!!.remove()
                    }
                    if (i != tick) PaintMgr.PaintHightestBlock(inkball!!.location, p, false, true)
                    if (inkball!!.isDead) cancel()
                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun Explode(
        player: Player,
        blastcenter: Location,
    ) {
        val data = getPlayerData(player)
        // 半径
        val maxDist = data!!.weaponClass.mainWeapon!!.blasterExHankei

        // 爆発音
        player.world.playSound(blastcenter, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

        // 爆発エフェクト
        createInkExplosionEffect(blastcenter, maxDist, 25, player)

        // バリアをはじく
        repelBarrier(blastcenter, maxDist, player)

        // 塗る
        var i = 0
        while (i <= maxDist - 1) {
            val p_locs = getSphere(blastcenter, i.toDouble(), 20)
            for (loc in p_locs) {
                PaintMgr.Paint(loc, player, false)
                PaintMgr.PaintHightestBlock(loc, player, false, false)
            }
            i++
        }

        // 攻撃判定の処理
        // for (Entity as : player.getWorld().getEntities()) {
        // if (as instanceof ArmorStand) {
        // if (as.getCustomName() != null) {
        // if (as.getLocation().distanceSquared(blastcenter) <= (maxDist + 1)*(maxDist +
        // 1)) {
        // try {
        // if (as.getCustomName().equals("Kasa")) {
        // KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
        // if (DataMgr.getPlayerData(kasaData.player).getTeam() !=
        // DataMgr.getPlayerData(player).getTeam()) {
        // cancel();
        // }
        // } else if (as.getCustomName().equals("SplashShield")) {
        // SplashShieldData splashShieldData =
        // DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
        // if (DataMgr.getPlayerData(splashShieldData.player).getTeam() !=
        // DataMgr.getPlayerData(player).getTeam()) {
        // cancel();
        // }
        // }
        // }catch (Exception e){}
        // }
        // }
        // }
        // }
        for (target in plugin.server.onlinePlayers) {
            if (!getPlayerData(target)!!.isInMatch) continue
            if (target.location.distance(blastcenter) <= maxDist + 1) {
                var damage = 10.0
                if (data.weaponClass.mainWeapon!!.isManeuver) {
                    damage =
                        data.weaponClass.mainWeapon!!.blasterExDamage
                } else {
                    damage = (
                        (maxDist - target.location.distance(blastcenter)) *
                            data.weaponClass.mainWeapon!!.blasterExDamage * 0.4
                        )
                }
                if (damage > data.weaponClass.mainWeapon!!.damage) {
                    damage = data.weaponClass.mainWeapon!!.damage
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

        for (`as` in player.world.entities) {
            if (`as` is ArmorStand) {
                if (`as`.location.distanceSquared(blastcenter) <= (maxDist + 1) * (maxDist + 1)) {
                    try {
                        var damage = (
                            (maxDist + 1 - `as`.location.distance(blastcenter)) *
                                data.weaponClass.mainWeapon!!.blasterExDamage
                            )
                        if (damage > data.weaponClass.mainWeapon!!.damage) {
                            damage = data.weaponClass.mainWeapon!!.damage
                        }
                        if (`as`.customName == "Kasa") {
                            val kasaData = getKasaDataFromArmorStand(`as`)
                            if (getPlayerData(kasaData!!.player)!!.team != getPlayerData(player)!!.team) {
                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                            }
                        } else if (`as`.customName == "SplashShield") {
                            val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                            if (getPlayerData(splashShieldData!!.player)!!.team !=
                                getPlayerData(player)!!
                                    .team
                            ) {
                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}
