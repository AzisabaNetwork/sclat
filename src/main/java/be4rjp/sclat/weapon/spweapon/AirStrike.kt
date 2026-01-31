package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.MapKitMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
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
    fun AirStrikeRunnable(
        player: Player,
        localized: Boolean,
    ) {
        val f = player.getWorld().spawn<Firework?>(player.getLocation(), Firework::class.java)
        player.getInventory().clear()
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
                    player.getWorld(),
                    (player.getLocation().getBlockX() + vec.getBlockX()).toDouble(),
                    i.toDouble(),
                    (player.getLocation().getBlockZ() + vec.getBlockZ()).toDouble(),
                )
            val block = player.getWorld().getBlockAt(locc)
            if (block.getType() != Material.AIR) {
                c = i
                break
            }
        }
        val y = c
        val ploc = player.getLocation()
        val tloc =
            Location(
                player.getWorld(),
                (player.getLocation().getBlockX() + vec.getBlockX()).toDouble(),
                y.toDouble(),
                (player.getLocation().getBlockZ() + vec.getBlockZ()).toDouble(),
            )
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var c: Int = 0

                override fun run() {
                    if (c == 0) getPlayerData(player)!!.setIsUsingSP(true)
                    var random = 18.0
                    // 集中砲火用
                    if (localized) {
                        random = 7.0
                    }
                    //
                    val loc =
                        Location(
                            ploc.getWorld(),
                            ploc.getBlockX() + vec.getBlockX() + (Math.random() * random - random / 2),
                            (y + 50).toDouble(),
                            ploc.getBlockZ() + vec.getBlockZ() + (Math.random() * random - random / 2),
                        )
                    StrikeRunnable(player, localized, loc)
                    if (c == 15 || !getPlayerData(player)!!.isInMatch()) {
                        // player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                        cancel()
                    }
                    c++
                }
            }
        if (localized) {
            task.runTaskTimer(plugin, 5, 5)
        } else {
            task.runTaskTimer(plugin, 50, 10)
        }

        val effect: BukkitRunnable =
            object : BukkitRunnable() {
                var c: Int = 0

                override fun run() {
                    val rayTrace = RayTrace(tloc.toVector(), Vector(0, 1, 0))
                    val positions = rayTrace.traverse(50.0, 0.8)
                    check@ for (vector in positions) {
                        val position = vector.toLocation(player.getLocation().getWorld()!!)
                        val dustOptions =
                            Particle.DustOptions(
                                getPlayerData(player)!!.team.teamColor!!.bukkitColor!!,
                                1f,
                            )
                        player.getWorld().spawnParticle<Particle.DustOptions?>(
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
                    if (c == 100 || !getPlayerData(player)!!.isInMatch()) {
                        getPlayerData(player)!!.setIsUsingSP(false)
                        cancel()
                    }
                    c++
                }
            }
        effect.runTaskTimer(plugin, 0, 2)
    }

    fun StrikeRunnable(
        player: Player,
        localized: Boolean,
        loc: Location,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null

                override fun run() {
                    if (c == 0) {
                        val bom = ItemStack(getPlayerData(p)!!.team.teamColor!!.wool!!).clone()
                        val bom_m = bom.getItemMeta()
                        bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                        bom.setItemMeta(bom_m)
                        drop = p.getWorld().dropItem(loc, bom)
                        if (localized) {
                            drop!!.setVelocity(Vector(0, -4, 0))
                        } else {
                            drop!!.setVelocity(Vector(0, -1, 0))
                        }
                    }

                    if (drop!!.isOnGround()) {
                        // 半径

                        var maxDist = 4.0
                        var maxDistSquared = 16.0 // 4^2
                        if (localized) {
                            maxDist = 5.0
                            maxDistSquared = 25.0 // 4^2
                        }

                        // 爆発音
                        player.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(drop!!.getLocation(), maxDist, 25, player)

                        // 塗る
                        var i = 0
                        while (i <= maxDist) {
                            val p_locs: MutableList<Location> = getSphere(drop!!.getLocation(), i.toDouble(), 20)
                            for (loc in p_locs) {
                                PaintMgr.Paint(loc, p, false)
                            }
                            i++
                        }

                        // 攻撃判定の処理
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.isInMatch()) continue
                            if (target.getLocation().distanceSquared(drop!!.getLocation()) <= maxDistSquared) {
                                val damage: Double
                                if (localized) {
                                    damage = (maxDist - target.getLocation().distance(drop!!.getLocation())) * 5
                                } else {
                                    damage = (maxDist - target.getLocation().distance(drop!!.getLocation())) * 7
                                }
                                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                    target.getGameMode() == GameMode.ADVENTURE
                                ) {
                                    giveDamage(player, target, damage, "spWeapon")

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
                            if (`as`.getLocation().distanceSquared(drop!!.getLocation()) <= maxDistSquared) {
                                if (`as` is ArmorStand) {
                                    val damage = (maxDist - `as`.getLocation().distance(drop!!.getLocation())) * 7
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                        }

                        drop!!.remove()
                        cancel()
                        return
                    }

                    // ボムの視認用エフェクト
                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) {
                            val dustOptions =
                                Particle.DustOptions(
                                    getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                    1f,
                                )
                            o_player.spawnParticle<Particle.DustOptions?>(
                                Particle.REDSTONE,
                                drop!!.getLocation(),
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
                    x = drop!!.getLocation().getX()
                    z = drop!!.getLocation().getZ()

                    if (c > 2000 || !getPlayerData(p)!!.isInMatch()) {
                        drop!!.remove()
                        cancel()
                        return
                    }
                }
            }

        task.runTaskTimer(plugin, 0, 1)
    }
}
