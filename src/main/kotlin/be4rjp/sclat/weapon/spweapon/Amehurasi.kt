package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getXZCircle
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
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
object Amehurasi {
    @JvmStatic
    fun amehurasiDropRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var pVec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var blockCheck: Boolean = false
                var cb: Boolean = false
                var l: Location = p.location
                var cc: Int = 0
                var c: Int = 0
                var drop: Item? = null
                var vec: Vector? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            getPlayerData(player)!!.isUsingAmehurashi = true
                            val bom = ItemStack(Material.BEACON).clone()
                            val bomM = bom.itemMeta
                            bomM!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bomM
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = p.eyeLocation.direction
                            pVec = p.eyeLocation.direction
                            vec = Vector(pVec!!.getX(), 0.0, pVec!!.getZ()).normalize()
                        }

                        if (drop!!.isOnGround) {
                            amehurasiRunnable(p, drop!!.location, vec!!)
                            drop!!.remove()
                            cancel()
                        }

                        if (drop!!.location.y <= 0 || drop!!.isDead) {
                            getPlayerData(player)!!.isUsingAmehurashi = false
                            WeaponClassMgr.setWeaponClass(p)
                            cancel()
                        }

                        // 視認用エフェクト
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.showEffectBomb()) {
                                if (o_player.world === drop!!.world) {
                                    if (o_player
                                            .location
                                            .distanceSquared(drop!!.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            drop!!.location,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            50.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }

                        c++

                        if (c > 1000) {
                            drop!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
                        sclatLogger.warn(e.message)
                    }
                }
            }
        if (!getPlayerData(player)!!.isUsingAmehurashi) task.runTaskTimer(plugin, 0, 1)
    }

    fun amehurasiRunnable(
        player: Player,
        loc: Location,
        vec: Vector,
    ) {
        getPlayerData(player)!!.isUsingSP = true
        SPWeaponMgr.setSPCoolTimeAnimation(player, 260)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0
                var locList: MutableList<Location> = getXZCircle(loc.clone().add(0.0, 18.0, 0.0), 8.0, 1.0, 100)

                override fun run() {
                    try {
                        if (c % 4 == 0) {
                            locList.clear()
                            locList =
                                getXZCircle(
                                    loc.clone().add(vec.getX() * c / 12, 18.0, vec.getZ() * c / 12),
                                    8.0,
                                    1.0,
                                    100,
                                )
                        }

                        // 雲エフェクト
                        if (c % 2 == 0) {
                            for (o_player in plugin.server.onlinePlayers) {
                                if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon()) {
                                    for (loc in locList) {
                                        if (Random().nextInt(3) == 1) {
                                            if (o_player.world === loc.world) {
                                                if (o_player
                                                        .location
                                                        .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                                ) {
                                                    val dustOptions =
                                                        Particle.DustOptions(
                                                            getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                                                            3f,
                                                        )
                                                    o_player.spawnParticle<Particle.DustOptions?>(
                                                        Particle.REDSTONE,
                                                        loc,
                                                        1,
                                                        1.0,
                                                        1.0,
                                                        1.0,
                                                        1.0,
                                                        dustOptions,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (c >= 30) {
                            // 攻撃判定
                            val rayTrace4 =
                                RayTrace(
                                    loc.clone().add(vec.getX() * c / 12, 18.0, vec.getZ() * c / 12).toVector(),
                                    Vector(0, -1, 0),
                                )
                            val positions4: ArrayList<Vector> = rayTrace4.traverse(300.0, 1.0)
                            for (i in 1..<positions4.size) {
                                val position = positions4[i].toLocation(p.location.world!!)

                                if (position.block.type != Material.AIR) break

                                val maxDistSquared = 42.25 // 6.5^2
                                val damage = 2.0
                                for (target in plugin.server.onlinePlayers) {
                                    if (!getPlayerData(target)!!.isInMatch) continue
                                    if (target.world !== p.world) continue
                                    if (target.location.distanceSquared(position) <= maxDistSquared &&
                                        Random().nextInt(100) == 0
                                    ) {
                                        if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                            target.gameMode == GameMode.ADVENTURE
                                        ) {
                                            giveDamage(p, target, damage, "spWeapon")

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
                                    if (`as` is ArmorStand &&
                                        `as`
                                            .location
                                            .distanceSquared(position) <= maxDistSquared &&
                                        Random().nextInt(100) == 0
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    }
                                }
                            }
                        }

                        for (loc in locList) {
                            if (Random().nextInt(200) == 1) snowballAmehurasiRunnable(p, loc)
                        }
                        if (c == 260 || !getPlayerData(p)!!.isInMatch) {
                            getPlayerData(player)!!.isUsingSP = false
                            // p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                            cancel()
                        }
                        c++
                    } catch (e: Exception) {
                        cancel()
                        sclatLogger.warn(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun snowballAmehurasiRunnable(
        player: Player,
        loc: Location,
    ) {
        val ball = player.world.spawnEntity(loc, EntityType.SNOWBALL) as Snowball
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        ball.shooter = player
        ball.customName = "Amehurasi"
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var inkball: Snowball = ball
                var p: Player = player

                override fun run() {
                    if (i % 2 == 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon()) {
                                if (o_player.world ===
                                    inkball.world
                                ) {
                                    if (o_player
                                            .location
                                            .distanceSquared(inkball.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball.location,
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
                    }

                    if (inkball.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }
}
