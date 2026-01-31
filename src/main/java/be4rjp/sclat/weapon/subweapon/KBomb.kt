package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.data.DataMgr.snowballNameMap
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object KBomb {
    @JvmStatic
    fun KBomRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var cb: Boolean = false
                var l: Location = p.location
                var cc: Int = 0
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null
                var ndn: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            if (!getPlayerData(player)!!.isBombRush) p.exp = p.exp - 0.59f
                            val bom = ItemStack(getPlayerData(p)!!.team.teamColor!!.concrete!!).clone()
                            val bom_m = bom.itemMeta
                            ndn = notDuplicateNumber
                            bom_m!!.setLocalizedName(ndn.toString())
                            bom.itemMeta = bom_m
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = p.eyeLocation.direction
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball?>(Snowball::class.java)
                            ball!!.velocity = Vector(0, 0, 0)
                            ball!!.customName = ndn.toString()
                            setSnowballIsHit(ball, false)
                            snowballNameMap.put(ndn.toString(), ball)

                            for (o_player in plugin.server.onlinePlayers) {
                                val connection = (o_player as CraftPlayer).handle.playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                            }
                            p_vec = p.eyeLocation.direction
                        }

                        ball = snowballNameMap.get(ndn.toString())

                        if (!drop!!.isOnGround &&
                            !(
                                drop!!.velocity.getX() == 0.0 && drop!!
                                    .velocity
                                    .getZ() != 0.0
                                ) &&
                            !(
                                drop!!.velocity.getX() != 0.0 && drop!!
                                    .velocity
                                    .getZ() == 0.0
                                )
                        ) {
                            ball!!.velocity = drop!!.velocity
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround) cb = true

                        if (!cb) l = drop!!.location

                        if (cb) {
                            drop!!.setGravity(false)
                            drop!!.velocity = Vector(0, 0, 0)
                            cc++
                        }

                        if (cc >= 40 && cc < 50) {
                            if (cc % 2 == 0) {
                                player
                                    .world
                                    .playSound(drop!!.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f)
                            }
                        }

                        if (cc == 60) {
                            // 爆発音

                            player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.location, 5.0, 15, player)

                            val maxDist = 4.0

                            // バリアをはじく
                            repelBarrier(drop!!.location, maxDist, player)

                            // 塗る
                            var i = 0
                            while (i <= maxDist) {
                                val p_locs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 14)
                                for (loc in p_locs) {
                                    PaintMgr.Paint(loc, p, false)
                                }
                                i++
                            }

                            // 攻撃判定の処理
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distance(drop!!.location) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.location.distance(drop!!.location)) * 17 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "subWeapon")

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
                                if (`as`.location.distance(drop!!.location) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        val damage = (maxDist - `as`.location.distance(drop!!.location)) * 12
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
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (o_player.world === drop!!.location.world) {
                                    if (o_player
                                            .location
                                            .distanceSquared(drop!!.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
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

                        x = drop!!.location.x
                        z = drop!!.location.z

                        if (c > 1000) {
                            drop!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        cancel()
                        drop!!.remove()
                        plugin.logger.warning(e.message)
                    }
                }
            }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 10)

        if (player.exp > 0.6 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
