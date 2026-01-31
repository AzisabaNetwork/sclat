package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
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
object Slosher {
    @JvmStatic
    fun ShootSlosher(player: Player) {
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
                    for (i in 0..<data.getWeaponClass().mainWeapon!!.rollerShootQuantity) {
                        Shoot(player, null)
                    }
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

    fun Shoot(
        player: Player,
        v: Vector?,
    ) {
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.getExp() <=
            (
                data!!.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
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
        var vec: Vector? =
            player
                .getLocation()
                .getDirection()
                .multiply(getPlayerData(player)!!.getWeaponClass().mainWeapon!!.shootSpeed)
        if (v != null) vec = v
        val random = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.random
        val distick = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.distanceTick
        vec!!.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random / 1.5 - random / 3,
                Math.random() * random - random / 2,
            ),
        )
        ball.setVelocity(vec)
        ball.setShooter(player)
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        ball.setCustomName(name)
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
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    try {
                        inkball = mainSnowballNameMap.get(name)

                        if (inkball != ball) {
                            i += getSnowballHitCount(name) - 1
                            setSnowballHitCount(name, 0)
                        }
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) continue
                            if (target.getWorld() === inkball!!.getWorld()) {
                                if (target
                                        .getLocation()
                                        .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team.teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        inkball!!.getLocation(),
                                        3,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0,
                                        bd,
                                    )
                                }
                            }
                        }

                        PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, false, true)

                        if (i >= tick && !addedFallVec) {
                            inkball!!.setVelocity(fallvec)
                            addedFallVec = true
                        }
                        if (i >= tick && i <= tick + 15) {
                            inkball!!.setVelocity(
                                inkball!!.getVelocity().add(Vector(0.0, -0.1, 0.0)),
                            )
                        }
                        if (inkball!!.isDead()) {
                            // 半径
                            val maxDist = data.getWeaponClass().mainWeapon!!.blasterExHankei

                            // 爆発音
                            player
                                .getWorld()
                                .playSound(inkball!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(inkball!!.getLocation(), maxDist, 25, player)

                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val p_locs = getSphere(inkball!!.getLocation(), i.toDouble(), 20)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, p, false)
                                        PaintMgr.PaintHightestBlock(loc, p, false, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch()) continue
                                if (target.getLocation().distanceSquared(inkball!!.getLocation()) <= maxDist * maxDist) {
                                    val damage = (
                                        (maxDist - target.getLocation().distance(inkball!!.getLocation())) *
                                            data.getWeaponClass().mainWeapon!!.blasterExDamage
                                        )
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
                                    if (`as`.getLocation().distanceSquared(inkball!!.getLocation()) <= maxDist * maxDist) {
                                        val damage = (
                                            (maxDist - `as`.getLocation().distance(inkball!!.getLocation())) *
                                                data.getWeaponClass().mainWeapon!!.blasterExDamage
                                            )
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                    }
                                }
                            }
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
}
