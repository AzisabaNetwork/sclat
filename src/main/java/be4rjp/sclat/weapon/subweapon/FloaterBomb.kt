package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.packet.EntityPackets.sendDestroyEntities
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
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
object FloaterBomb {
    @JvmStatic
    fun FloaterBombRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null
                var onground: Boolean = false
                var turn: Boolean = false

                override fun run() {
                    try {
                        if (c == 0) {
                            turn = false
                            onground = player.isOnGround
                            p_vec = p.eyeLocation.direction
                            if (!onground) {
                                p_vec = p_vec!!.normalize().multiply(1.1)
                            } else {
                                p_vec = p_vec!!.normalize().multiply(0.95)
                            }
                            if (!getPlayerData(player)!!.isBombRush) p.exp = p.exp - 0.47f
                            val bom = ItemStack(getPlayerData(p)!!.team.teamColor!!.wool!!).clone()
                            val bom_m = bom.itemMeta
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bom_m
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = p_vec!!.clone()
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball>(Snowball::class.java)
                            ball!!.velocity = Vector(0, 0, 0)
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.server.onlinePlayers) {
                                // PlayerConnection connection = ((CraftPlayer)
                                // o_player).getHandle().playerConnection;
                                // connection.sendPacket(new PacketPlayOutEntityDestroy(ball.getEntityId()));
                                sendDestroyEntities(o_player, ball!!.entityId)
                            }
                            p_vec = p.eyeLocation.direction
                        }

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
                        if (c == 9 && onground) {
                            p_vec =
                                p
                                    .eyeLocation
                                    .direction
                                    .normalize()
                                    .multiply(0.8)
                            drop!!.velocity = p_vec!!
                            ball!!.velocity = p_vec!!
                            turn = true
                            player.world.playSound(drop!!.location, Sound.BLOCK_NOTE_BLOCK_SNARE, 1f, 1f)
                        }
                        if (c == 12 && !onground) {
                            p_vec =
                                p
                                    .eyeLocation
                                    .direction
                                    .normalize()
                                    .multiply(0.6)
                            drop!!.velocity = p_vec!!
                            ball!!.velocity = p_vec!!
                            turn = true
                            player.world.playSound(drop!!.location, Sound.BLOCK_NOTE_BLOCK_SNARE, 1f, 1f)
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround) {
                            // 半径

                            var maxDist = 3.0
                            if (!turn) {
                                maxDist = 2.0
                            }
                            // 爆発ダメージ
                            val ExDamage = 4.0

                            // if(onground) {
                            // ExDamage = 4.0;
                            // }

                            // 爆発音
                            player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.location, maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(drop!!.location, maxDist, player)

                            // 塗る
                            var i = 0
                            while (i <= maxDist) {
                                val p_locs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 20)
                                for (loc in p_locs) {
                                    PaintMgr.Paint(loc, p, false)
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
                                    var damage = (
                                        (maxDist - target.location.distance(drop!!.location) * 0.7) *
                                            ExDamage * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (!turn) {
                                        damage = damage * 0.9
                                    }
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
                                        var damage = (
                                            (maxDist - `as`.location.distance(drop!!.location) * 0.7) *
                                                ExDamage * Gear.getGearInfluence(p, Gear.Type.SUB_SPEC_UP)
                                            )
                                        if (!turn) {
                                            damage = damage * 0.9
                                        }
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

                        if (c > 500) {
                            drop!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
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

        if (player.exp > 0.48 || getPlayerData(player)!!.isBombRush) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
