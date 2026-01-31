package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Bucket {
    @JvmStatic
    fun ShootBucket(player: Player) {
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
                override fun run() {
                    var sound = false
                    for (i in 0..<data.getWeaponClass().mainWeapon!!.rollerShootQuantity) {
                        val `is` = Shoot(player, null)
                        if (`is`) sound = true
                    }
                    if (sound) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                }
            }
        val delay2: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player
                var c: Int = 0
                var sound: Boolean = false

                override fun run() {
                    c++
                    val q = 2
                    for (i in 0..<data.getWeaponClass().mainWeapon!!.rollerShootQuantity) {
                        val `is` = Shoot(player, null)
                        if (`is`) sound = true
                    }
                    if (sound) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                    if (c == q) cancel()
                }
            }
        if (data.getCanRollerShoot()) {
            // delay.runTaskLater(Main.getPlugin(),
            // data.getWeaponClass().getMainWeapon().delay);
            delay2.runTaskTimer(
                plugin,
                0,
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
    ): Boolean {
        if (player.getGameMode() == GameMode.SPECTATOR) return false

        val data = getPlayerData(player)
        if (player.getExp() <=
            (
                data!!.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
            return true
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
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }
                    if (i != 0) {
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (target.getWorld() !== p.getWorld()) continue
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
                        inkball!!.setVelocity(fallvec)
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.setVelocity(
                            inkball!!.getVelocity().add(Vector(0.0, -0.1, 0.0)),
                        )
                    }
                    if (i != tick) PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, true, true)
                    if (inkball!!.isDead()) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)

        return false
    }

    @JvmStatic
    fun BucketHealRunnable(
        player: Player,
        level: Int,
    ) {
        val delay3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var Ctime: Int = 200
                var bh_recharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (level >= 1) {
                        Ctime = 100
                    }
                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }
                    if (data.getIsSneaking() && bh_recharge && player.getGameMode() == GameMode.ADVENTURE) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, Ctime, level))
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.4f, 1.5f)
                        bh_recharge = false
                        val healtask: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    bh_recharge = true
                                }
                            }
                        healtask.runTaskLater(plugin, Ctime.toLong())
                    }
                }
            }
        delay3.runTaskTimer(plugin, 0, 1)
    }
}
