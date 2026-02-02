package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
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
object Sensor {
    @JvmStatic
    fun sensorRunnable(player: Player) {
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
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            pVec = p.eyeLocation.direction
                            if (!getPlayerData(player)!!.isBombRush) p.exp = p.exp - 0.39f
                            val bom = ItemStack(Material.DISPENSER).clone()
                            val bomM = bom.itemMeta
                            bomM!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bomM
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = pVec!!
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball>(Snowball::class.java)
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

                            val maxDist = 5 * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)

                            // 爆発音
                            player.world.playSound(drop!!.location, Sound.ENTITY_ARROW_SHOOT, 1f, 2f)

                            // 爆発エフェクト
                            val sLocs = getSphere(drop!!.location, maxDist, 15)
                            for (o_player in plugin.server.onlinePlayers) {
                                if (getPlayerData(o_player)!!.settings!!.showEffectBombEx()) {
                                    for (loc in sLocs) {
                                        if (o_player.world === loc.world) {
                                            if (o_player
                                                    .location
                                                    .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions = Particle.DustOptions(Color.BLACK, 1f)
                                                o_player.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    loc,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    1.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // あたり判定の処理
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distance(drop!!.location) <= maxDist) {
                                    if (getPlayerData(player)!!.team!!.iD !=
                                        getPlayerData(target)!!
                                            .team!!
                                            .iD
                                    ) {
                                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
                                    }
                                }
                            }

                            for (`as` in player.world.entities) {
                                if (`as`.location.distance(drop!!.location) <= maxDist) {
                                    if (`as`.customName != null) {
                                        if (`as`.customName == null) continue
                                        if (`as` is ArmorStand &&
                                            (`as`.customName != "Path") &&
                                            (`as`.customName != "21") &&
                                            (`as`.customName != "100") &&
                                            (`as`.customName != "SplashShield") &&
                                            (`as`.customName != "Kasa")
                                        ) {
                                            `as`
                                                .addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
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
                        cancel()
                        drop!!.remove()
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
        cooltime.runTaskLater(plugin, 8)

        if (player.exp > 0.3 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
