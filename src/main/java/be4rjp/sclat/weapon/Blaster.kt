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
                    data!!.setCanRollerShoot(true)
                }
            }
        if (data!!.getCanRollerShoot()) {
            delay1.runTaskLater(
                plugin,
                data
                    .getWeaponClass()
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
        if (data.getCanRollerShoot()) {
            delay.runTaskLater(
                plugin,
                data
                    .getWeaponClass()
                    .mainWeapon!!
                    .delay
                    .toLong(),
            )
            data.setCanRollerShoot(false)
        }
    }

    fun Shoot(player: Player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        data!!.setCanRollerShoot(false)
        if (player.getExp() <=
            (
                data.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.setExp(
            player.getExp() -
                (
                    data.getWeaponClass().mainWeapon!!.needInk
                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                    ).toFloat(),
        )
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .getLocation()
                .getDirection()
                .multiply(getPlayerData(player)!!.getWeaponClass().mainWeapon!!.shootSpeed)
        val random = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.random
        val distick = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.distanceTick
        if (!player.isOnGround()) {
            vec.add(
                Vector(
                    Math.random() * random - random / 2,
                    0.0,
                    Math.random() * random - random / 2,
                ),
            )
        }
        ball.setVelocity(vec)
        ball.setShooter(player)
        ball.setGravity(false)
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        DataMgr.tsl.add(name)
        ball.setCustomName(name)
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
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootSpeed / 17)

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
                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        if (getPlayerData(o_player)!!.settings.ShowEffect_MainWeaponInk()) {
                            if (o_player.getWorld() ===
                                inkball!!.getWorld()
                            ) {
                                if (o_player
                                        .getLocation()
                                        .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    o_player.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        inkball!!.getLocation(),
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

                    if (i >= tick && !inkball!!.isDead()) {
                        // 半径
                        val maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei

                        // 爆発音
                        player.getWorld().playSound(inkball!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(inkball!!.getLocation(), maxDist, 25, player)

                        // バリアをはじく
                        repelBarrier(inkball!!.getLocation(), maxDist, player)

                        // 塗る
                        run {
                            var i = 0
                            while (i <= maxDist - 1) {
                                val p_locs = getSphere(inkball!!.getLocation(), i.toDouble(), 20)
                                for (loc in p_locs) {
                                    PaintMgr.Paint(loc, p, false)
                                    PaintMgr.PaintHightestBlock(loc, p, false, false)
                                }
                                i++
                            }
                        }

                        // 攻撃判定の処理
                        for (`as` in player.getWorld().getEntities()) {
                            if (`as` is ArmorStand) {
                                if (`as`.getCustomName() != null) {
                                    if (`as`.getLocation().distanceSquared(ball.getLocation()) <= maxDist * maxDist) {
                                        try {
                                            if (`as`.getCustomName() == "Kasa") {
                                                val kasaData = getKasaDataFromArmorStand(`as`)
                                                if (getPlayerData(kasaData!!.player)!!.team !=
                                                    DataMgr
                                                        .getPlayerData(p)!!
                                                        .team
                                                ) {
                                                    inkball!!.remove()
                                                    cancel()
                                                }
                                            } else if (`as`.getCustomName() == "SplashShield") {
                                                val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                    DataMgr
                                                        .getPlayerData(p)!!
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

                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.isInMatch()) continue
                            if (target.getLocation().distance(inkball!!.getLocation()) <= maxDist + 1) {
                                var damage = 10.0
                                if (data.getWeaponClass().mainWeapon!!.isManeuver) {
                                    damage =
                                        data.getWeaponClass().mainWeapon!!.blasterExDamage
                                } else {
                                    damage = (
                                        (maxDist + 1 - target.getLocation().distance(inkball!!.getLocation())) *
                                            data.getWeaponClass().mainWeapon!!.blasterExDamage
                                        )
                                }
                                if (damage > data.getWeaponClass().mainWeapon!!.damage) {
                                    damage = data.getWeaponClass().mainWeapon!!.damage
                                }
                                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
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
                            if (`as` is ArmorStand) {
                                if (`as`.getLocation().distanceSquared(inkball!!.getLocation()) <= (maxDist + 1) *
                                    (maxDist + 1)
                                ) {
                                    var damage = (
                                        (maxDist + 1 - `as`.getLocation().distance(inkball!!.getLocation())) *
                                            data.getWeaponClass().mainWeapon!!.blasterExDamage
                                        )
                                    if (damage > data.getWeaponClass().mainWeapon!!.damage) {
                                        damage = data.getWeaponClass().mainWeapon!!.damage
                                    }
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                        }

                        inkball!!.remove()
                    }
                    if (i != tick) PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, false, true)
                    if (inkball!!.isDead()) cancel()
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
        val maxDist = data!!.getWeaponClass().mainWeapon!!.blasterExHankei

        // 爆発音
        player.getWorld().playSound(blastcenter, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

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
        for (target in plugin.getServer().getOnlinePlayers()) {
            if (!getPlayerData(target)!!.isInMatch()) continue
            if (target.getLocation().distance(blastcenter) <= maxDist + 1) {
                var damage = 10.0
                if (data.getWeaponClass().mainWeapon!!.isManeuver) {
                    damage =
                        data.getWeaponClass().mainWeapon!!.blasterExDamage
                } else {
                    damage = (
                        (maxDist - target.getLocation().distance(blastcenter)) *
                            data.getWeaponClass().mainWeapon!!.blasterExDamage * 0.4
                        )
                }
                if (damage > data.getWeaponClass().mainWeapon!!.damage) {
                    damage = data.getWeaponClass().mainWeapon!!.damage
                }
                if (damage < 0.1) {
                    damage = 0.1
                }
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
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
            if (`as` is ArmorStand) {
                if (`as`.getLocation().distanceSquared(blastcenter) <= (maxDist + 1) * (maxDist + 1)) {
                    try {
                        var damage = (
                            (maxDist + 1 - `as`.getLocation().distance(blastcenter)) *
                                data.getWeaponClass().mainWeapon!!.blasterExDamage
                            )
                        if (damage > data.getWeaponClass().mainWeapon!!.damage) {
                            damage = data.getWeaponClass().mainWeapon!!.damage
                        }
                        if (`as`.getCustomName() == "Kasa") {
                            val kasaData = getKasaDataFromArmorStand(`as`)
                            if (getPlayerData(kasaData!!.player)!!.team != getPlayerData(player)!!.team) {
                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                            }
                        } else if (`as`.getCustomName() == "SplashShield") {
                            val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                            if (getPlayerData(splashShieldData!!.player)!!.team !=
                                DataMgr
                                    .getPlayerData(player)!!
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
