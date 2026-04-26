package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.MapKitMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.shape.Sphere.getSphere
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Firework
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object AirStrike {
    @JvmStatic
    fun airStrikeRunnable(
        player: Player,
        localized: Boolean,
    ) {
        player.world.spawn(player.location, Firework::class.java)
        player.inventory.clear()
        SPWeaponMgr.setSPCoolTimeAnimation(player, 200)

        val clear: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    WeaponClassMgr.setWeaponClass(player)
                    if (player.hasPotionEffect(PotionEffectType.SLOW)) player.removePotionEffect(PotionEffectType.SLOW)
                }
            }
        clear.runTaskLater(plugin, 20)

        val vec = MapKitMgr.getMapLocationVector(player)

        // int y = player.getWorld().getHighestBlockYAt(vec.getBlockX(),
        // vec.getBlockZ());
        var c = 0
        for (i in 254 downTo 1) {
            val locc =
                Location(
                    player.world,
                    (player.location.blockX + vec.blockX).toDouble(),
                    i.toDouble(),
                    (player.location.blockZ + vec.blockZ).toDouble(),
                )
            val block = player.world.getBlockAt(locc)
            if (block.type != Material.AIR) {
                c = i
                break
            }
        }
        val y = c
        val ploc = player.location
        val tloc =
            Location(
                player.world,
                (player.location.blockX + vec.blockX).toDouble(),
                y.toDouble(),
                (player.location.blockZ + vec.blockZ).toDouble(),
            )
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var cn: Int = 0

                override fun run() {
                    if (cn == 0) getPlayerData(player)!!.isUsingSP = true
                    var random = 18.0
                    // 集中砲火用
                    if (localized) {
                        random = 7.0
                    }
                    //
                    val loc =
                        Location(
                            ploc.world,
                            ploc.blockX + vec.blockX + (Math.random() * random - random / 2),
                            (y + 50).toDouble(),
                            ploc.blockZ + vec.blockZ + (Math.random() * random - random / 2),
                        )
                    strikeRunnable(player, localized, loc)
                    if (cn == 15 || !getPlayerData(player)!!.isInMatch) {
                        // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                        cancel()
                    }
                    cn++
                }
            }
        if (localized) {
            task.runTaskTimer(plugin, 5, 5)
        } else {
            task.runTaskTimer(plugin, 50, 10)
        }

        val effect: BukkitRunnable =
            object : BukkitRunnable() {
                var cnt: Int = 0

                override fun run() {
                    val rayTrace = RayTrace(tloc.toVector(), Vector(0, 1, 0))
                    val positions = rayTrace.traverse(50.0, 0.8)
                    check@ for (vector in positions) {
                        val position = vector.toLocation(player.location.world!!)
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(player)!!.team!!.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        player.world.spawnParticle<Particle.DustOptions?>(
                            Particle.REDSTONE,
                            position,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            1.0,
                            dustOptions,
                        )
                    }
                    if (cnt == 100 || !getPlayerData(player)!!.isInMatch) {
                        getPlayerData(player)!!.isUsingSP = false
                        cancel()
                    }
                    cnt++
                }
            }
        effect.runTaskTimer(plugin, 0, 2)
    }

    fun strikeRunnable(
        player: Player,
        localized: Boolean,
        loc: Location,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var pVec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var blockCheck: Boolean = false
                var c: Int = 0
                var drop: Item? = null

                override fun run() {
                    if (c == 0) {
                        val bom = ItemStack(getPlayerData(p)!!.team!!.teamColor!!.wool!!).clone()
                        val bomM = bom.itemMeta
                        bomM!!.setLocalizedName(notDuplicateNumber.toString())
                        bom.itemMeta = bomM
                        drop = p.world.dropItem(loc, bom)
                        if (localized) {
                            drop!!.velocity = Vector(0, -4, 0)
                        } else {
                            drop!!.velocity = Vector(0, -1, 0)
                        }
                    }

                    if (drop!!.isOnGround) {
                        // 半径

                        var maxDist = 4.0
                        var maxDistSquared = 16.0 // 4^2
                        if (localized) {
                            maxDist = 5.0
                            maxDistSquared = 25.0 // 4^2
                        }

                        // 爆発音
                        player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(drop!!.location, maxDist, 25, player)

                        // 塗る
                        var i = 0
                        while (i <= maxDist) {
                            val pLocs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 20)
                            for (loc in pLocs) {
                                PaintMgr.paint(loc, p, false)
                            }
                            i++
                        }

                        // 攻撃判定の処理
                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.isInMatch) continue
                            if (target.location.distanceSquared(drop!!.location) <= maxDistSquared) {
                                val damage: Double
                                damage =
                                    if (localized) {
                                        (maxDist - target.location.distance(drop!!.location)) * 5
                                    } else {
                                        (maxDist - target.location.distance(drop!!.location)) * 7
                                    }
                                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                    target.gameMode == GameMode.ADVENTURE
                                ) {
                                    giveDamage(player, target, damage, "spWeapon")

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
                            if (`as`.location.distanceSquared(drop!!.location) <= maxDistSquared) {
                                if (`as` is ArmorStand) {
                                    val damage = (maxDist - `as`.location.distance(drop!!.location)) * 7
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                        }

                        drop!!.remove()
                        cancel()
                        return
                    }

                    // ボムの視認用エフェクト
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon()) {
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

                    c++
                    x = drop!!.location.x
                    z = drop!!.location.z

                    if (c > 2000 || !getPlayerData(p)!!.isInMatch) {
                        drop!!.remove()
                        cancel()
                        return
                    }
                }
            }

        task.runTaskTimer(plugin, 0, 1)
    }
}
