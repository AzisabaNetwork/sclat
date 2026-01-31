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
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object Burst {
    @JvmStatic
    fun BurstCooltime(player: Player) {
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

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    Burstshoot(player)
                }
            }
        if (data.canRollerShoot) {
            delay.runTaskLater(
                plugin,
                data
                    .weaponClass
                    ?.mainWeapon!!
                    .delay
                    .toLong(),
            )
            data.canRollerShoot = false
        }
    }

    fun Burstshoot(player: Player) {
        val otoNumber = notDuplicateNumber.toString()
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    c++
                    val q = getPlayerData(p)!!.weaponClass?.mainWeapon!!.rollerShootQuantity
                    Shoot(
                        p,
                        q == 3 && getPlayerData(p)!!.weaponClass?.mainWeapon!!.shootTick == 2,
                        otoNumber,
                    )
                    if (c == q) cancel()
                }
            }
        task.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .weaponClass
                ?.mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun Shoot(
        player: Player,
        sound: Boolean,
        otoNumber: String?,
    ) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass?.mainWeapon!!.needInk
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
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()

        PaintMgr.paintHightestBlock(player.location, player, true, true)

        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team?.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass?.mainWeapon!!.shootSpeed)
        val random = data.weaponClass?.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass?.mainWeapon!!.distanceTick
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.velocity = vec
        ball.shooter = player
        val name = notDuplicateNumber.toString() + ":Burst:" + otoNumber
        DataMgr.mws.add(name)
        if (sound) DataMgr.tsl.add(name)
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
                var addedFallVec: Boolean = false
                var p: Player = player
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass?.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team
                                ?.teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings?.ShowEffect_MainWeaponInk()!!) {
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
                                            0,
                                            0.0,
                                            -1.0,
                                            0.0,
                                            1.0,
                                            bd,
                                        )
                                    }
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
                    // if(i != tick)
                    if ((Random().nextInt(5)) == 0) PaintMgr.paintHightestBlock(inkball!!.location, p, false, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
