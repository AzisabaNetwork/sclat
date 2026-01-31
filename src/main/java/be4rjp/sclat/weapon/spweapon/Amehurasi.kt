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
    fun AmehurasiDropRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var cb: Boolean = false
                var l: Location = p.getLocation()
                var cc: Int = 0
                var c: Int = 0
                var drop: Item? = null
                var vec: Vector? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            getPlayerData(player)!!.setIsUsingAmehurashi(true)
                            val bom = ItemStack(Material.BEACON).clone()
                            val bom_m = bom.getItemMeta()
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.setItemMeta(bom_m)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), bom)
                            drop!!.setVelocity(p.getEyeLocation().getDirection())
                            p_vec = p.getEyeLocation().getDirection()
                            vec = Vector(p_vec!!.getX(), 0.0, p_vec!!.getZ()).normalize()
                        }

                        if (drop!!.isOnGround()) {
                            Amehurasi.AmehurasiRunnable(p, drop!!.getLocation(), vec!!)
                            drop!!.remove()
                            cancel()
                        }

                        if (drop!!.getLocation().getY() <= 0 || drop!!.isDead()) {
                            getPlayerData(player)!!.setIsUsingAmehurashi(false)
                            WeaponClassMgr.setWeaponClass(p)
                            cancel()
                        }

                        // 視認用エフェクト
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (o_player.getWorld() === drop!!.getWorld()) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
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
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        if (!getPlayerData(player)!!.getIsUsingAmehurashi()) task.runTaskTimer(plugin, 0, 1)
    }

    fun AmehurasiRunnable(
        player: Player,
        loc: Location,
        vec: Vector,
    ) {
        getPlayerData(player)!!.setIsUsingSP(true)
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
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) {
                                    for (loc in locList) {
                                        if (Random().nextInt(3) == 1) {
                                            if (o_player.getWorld() === loc.getWorld()) {
                                                if (o_player
                                                        .getLocation()
                                                        .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                                ) {
                                                    val dustOptions =
                                                        Particle.DustOptions(
                                                            getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
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
                                val position = positions4.get(i)!!.toLocation(p.getLocation().getWorld()!!)

                                if (position.getBlock().getType() != Material.AIR) break

                                val maxDist = 6.5
                                val maxDistSquared = 42.25 // 6.5^2
                                val damage = 2.0
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (!getPlayerData(target)!!.isInMatch()) continue
                                    if (target.getWorld() !== p.getWorld()) continue
                                    if (target.getLocation().distanceSquared(position) <= maxDistSquared &&
                                        Random().nextInt(100) == 0
                                    ) {
                                        if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                            target.getGameMode() == GameMode.ADVENTURE
                                        ) {
                                            giveDamage(p, target, damage, "spWeapon")

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
                                    if (`as` is ArmorStand &&
                                        `as`
                                            .getLocation()
                                            .distanceSquared(position) <= maxDistSquared && Random().nextInt(100) == 0
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    }
                                }
                            }
                        }

                        for (loc in locList) {
                            if (Random().nextInt(200) == 1) SnowballAmehurasiRunnable(p, loc)
                        }
                        if (c == 260 || !getPlayerData(p)!!.isInMatch()) {
                            getPlayerData(player)!!.setIsUsingSP(false)
                            // p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                            cancel()
                        }
                        c++
                    } catch (e: Exception) {
                        cancel()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun SnowballAmehurasiRunnable(
        player: Player,
        loc: Location,
    ) {
        val ball = player.getWorld().spawnEntity(loc, EntityType.SNOWBALL) as Snowball
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        ball.setShooter(player)
        ball.setCustomName("Amehurasi")
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var inkball: Snowball = ball
                var p: Player = player

                override fun run() {
                    if (i % 2 == 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team.teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) {
                                if (o_player.getWorld() ===
                                    inkball.getWorld()
                                ) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(inkball.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball.getLocation(),
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

                    if (inkball.isDead()) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }
}
