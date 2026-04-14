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
    fun shootBucket(player: Player) {
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
                    ?.mainWeapon!!
                    .coolTime
                    .toLong(),
            )
        }

        object : BukkitRunnable() {
            override fun run() {
                var sound = false
                for (i in 0..<data.weaponClass?.mainWeapon!!.rollerShootQuantity) {
                    val `is` = shoot(player, null)
                    if (`is`) sound = true
                }
                if (sound) player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
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
                    for (i in 0..<data.weaponClass?.mainWeapon!!.rollerShootQuantity) {
                        val `is` = shoot(player, null)
                        if (`is`) sound = true
                    }
                    if (sound) player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                    if (c == q) cancel()
                }
            }
        if (data.canRollerShoot) {
            // delay.runTaskLater(Main.getPlugin(),
            // data.getWeaponClass().getMainWeapon().delay);
            delay2.runTaskTimer(
                plugin,
                0,
                data
                    .weaponClass
                    ?.mainWeapon!!
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
                data!!.weaponClass?.mainWeapon!!.needInk
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
        (ball as CraftSnowball).handle.item = CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team?.teamColor!!.wool!!))
        var vec: Vector? =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass?.mainWeapon!!.shootSpeed)
        if (v != null) vec = v
        val random = getPlayerData(player)!!.weaponClass?.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass?.mainWeapon!!.distanceTick
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
        mainSnowballNameMap[name] = ball
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
                    ).multiply(getPlayerData(p)!!.weaponClass?.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }
                    if (i != 0) {
                        for (target in plugin.server.onlinePlayers) {
                            if (target.world !== p.world) continue
                            if (!getPlayerData(target)!!.settings?.showEffectMainWeaponInk()!!) continue
                            if (target.world === inkball!!.world) {
                                if (target
                                        .location
                                        .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team
                                            ?.teamColor!!
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
    fun bucketHealRunnable(
        player: Player,
        level: Int,
    ) {
        val delay3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var cTime: Int = 200
                var bhRecharge: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (level >= 1) {
                        cTime = 100
                    }
                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }
                    if (data.isSneaking && bhRecharge && player.gameMode == GameMode.ADVENTURE) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, cTime, level))
                        p.world.playSound(p.location, Sound.ITEM_TRIDENT_RETURN, 1.4f, 1.5f)
                        bhRecharge = false
                        val healtask: BukkitRunnable =
                            object : BukkitRunnable() {
                                // クールタイムを管理しています
                                override fun run() {
                                    bhRecharge = true
                                }
                            }
                        healtask.runTaskLater(plugin, cTime.toLong())
                    }
                }
            }
        delay3.runTaskTimer(plugin, 0, 1)
    }
}
