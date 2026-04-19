package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import be4rjp.sclat.weapon.Gear
import net.azisaba.sclat.core.shape.Sphere.getSphere
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
object QuickBomb {
    @JvmStatic
    fun quickBomRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var pVec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var blockCheck: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            pVec = p.eyeLocation.direction
                            if (!getPlayerData(player)!!.isBombRush) p.exp -= 0.39f
                            val bom = ItemStack(getPlayerData(p)!!.team!!.teamColor!!.wool!!).clone()
                            val bomM = bom.itemMeta
                            bomM!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bomM
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = pVec!!.clone()
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile(Snowball::class.java)
                            ball!!.velocity = Vector(0, 0, 0)
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.server.onlinePlayers) {
                                val connection = (o_player as CraftPlayer).handle.playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                            }
                            pVec = p.eyeLocation.direction
                        }

                        if (!drop!!.isOnGround &&
                            !(
                                drop!!.velocity.getX() == 0.0 &&
                                    drop!!
                                        .velocity
                                        .getZ() != 0.0
                            ) &&
                            !(
                                drop!!.velocity.getX() != 0.0 &&
                                    drop!!
                                        .velocity
                                        .getZ() == 0.0
                            )
                        ) {
                            ball!!.velocity = drop!!.velocity
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround) {
                            // 半径

                            val maxDist = 3.0

                            // 爆発音
                            player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.location, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(drop!!.location, maxDist, player)

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
                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(drop!!.location) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            try {
                                                if (`as`.customName == "Kasa") {
                                                    val kasaData = getKasaDataFromArmorStand(`as`)
                                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                                        getPlayerData(p)!!
                                                            .team
                                                    ) {
                                                        drop!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.customName == "SplashShield") {
                                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                        getPlayerData(p)!!
                                                            .team
                                                    ) {
                                                        drop!!.remove()
                                                        cancel()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                            }
                                        }
                                    }
                                }
                            }

                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distance(drop!!.location) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.location.distance(drop!!.location)) * 5 *
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
                                        val damage = (
                                            (maxDist - `as`.location.distance(drop!!.location)) * 5 *
                                                Gear.getGearInfluence(p, Gear.Type.SUB_SPEC_UP)
                                        )
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                        if (`as`.customName != null) {
                                            if (`as`.customName == "SplashShield" ||
                                                `as`.customName == "Kasa"
                                            ) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ボムの視認用エフェクト
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.showEffectBomb()) {
                                if (o_player.world === drop!!.location.world) {
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
                        x = drop!!.location.x
                        z = drop!!.location.z

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

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 10)

        if (player.exp > 0.4 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
