package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
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
object QuickBomb {
    @JvmStatic
    fun QuickBomRunnable(player: Player) {
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

                override fun run() {
                    try {
                        if (c == 0) {
                            p_vec = p.getEyeLocation().getDirection()
                            if (!getPlayerData(player)!!.getIsBombRush()) p.setExp(p.getExp() - 0.39f)
                            val bom = ItemStack(getPlayerData(p)!!.team.teamColor!!.wool!!).clone()
                            val bom_m = bom.getItemMeta()
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.setItemMeta(bom_m)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), bom)
                            drop!!.setVelocity(p_vec!!.clone())
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball>(Snowball::class.java)
                            ball!!.setVelocity(Vector(0, 0, 0))
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                val connection = (o_player as CraftPlayer).getHandle().playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                            }
                            p_vec = p.getEyeLocation().getDirection()
                        }

                        if (!drop!!.isOnGround() &&
                            !(
                                drop!!.getVelocity().getX() == 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() != 0.0
                                ) &&
                            !(
                                drop!!.getVelocity().getX() != 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() == 0.0
                                )
                        ) {
                            ball!!.setVelocity(drop!!.getVelocity())
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround()) {
                            // 半径

                            val maxDist = 3.0

                            // 爆発音
                            player.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.getLocation(), maxDist, 15, player)

                            // バリアをはじく
                            repelBarrier(drop!!.getLocation(), maxDist, player)

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
                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.getCustomName() != null) {
                                            try {
                                                if (`as`.getCustomName() == "Kasa") {
                                                    val kasaData = getKasaDataFromArmorStand(`as`)
                                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                                        DataMgr
                                                            .getPlayerData(p)!!
                                                            .team
                                                    ) {
                                                        drop!!.remove()
                                                        cancel()
                                                    }
                                                } else if (`as`.getCustomName() == "SplashShield") {
                                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                        DataMgr
                                                            .getPlayerData(p)!!
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

                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== p.getWorld()) continue
                                if (target.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    val damage = (
                                        (maxDist - target.getLocation().distance(drop!!.getLocation())) * 5 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.getGameMode() == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "subWeapon")

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
                                if (`as`.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (`as` is ArmorStand) {
                                        val damage = (
                                            (maxDist - `as`.getLocation().distance(drop!!.getLocation())) * 5 *
                                                Gear.getGearInfluence(p, Gear.Type.SUB_SPEC_UP)
                                            )
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                        if (`as`.getCustomName() != null) {
                                            if (`as`.getCustomName() == "SplashShield" ||
                                                `as`.getCustomName() == "Kasa"
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
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (o_player.getWorld() === drop!!.getLocation().getWorld()) {
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
                        x = drop!!.getLocation().getX()
                        z = drop!!.getLocation().getZ()

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

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.setCanUseSubWeapon(true)
                }
            }
        cooltime.runTaskLater(plugin, 10)

        if (player.getExp() > 0.4 || getPlayerData(player)!!.getIsBombRush()) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }
}
