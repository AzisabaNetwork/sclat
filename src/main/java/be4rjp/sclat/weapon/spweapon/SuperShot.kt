package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object SuperShot {
    @JvmStatic
    fun setSuperShot(player: Player) {
        getPlayerData(player)!!.isUsingSP = true
        getPlayerData(player)!!.setIsUsingSS(true)
        SPWeaponMgr.setSPCoolTimeAnimation(player, 100)

        val it: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    player.inventory.clear()
                    player.updateInventory()

                    val item = ItemStack(Material.SUGAR_CANE)
                    val meta = item.itemMeta
                    meta!!.setDisplayName("右クリックで発射！")
                    item.itemMeta = meta
                    for (count in 0..8) {
                        player.inventory.setItem(count, item)
                        if (count % 2 != 0) player.inventory.setItem(count, ItemStack(Material.AIR))
                    }
                    player.updateInventory()
                    player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 101, 1))
                }
            }
        it.runTaskLater(plugin, 2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    if (getPlayerData(p)!!.isInMatch) {
                        getPlayerData(p)!!.isUsingSP = false
                        getPlayerData(p)!!.setIsUsingSS(false)
                        player.inventory.clear()
                        WeaponClassMgr.setWeaponClass(p)
                    }
                }
            }
        task.runTaskLater(plugin, 100)
    }

    @JvmStatic
    fun Shot(player: Player) {
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            val direction = Vector(0, 1, 0)
            var headdis = 8.5
            val playerLocation = player.location
            while (headdis > 3) {
                if (player.world.rayTraceBlocks(playerLocation, direction, headdis) != null) {
                    headdis -= 1.0
                } else {
                    break
                }
            }
            player.world.playSound(playerLocation, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.5f, 1.2f)
            player.world.playSound(playerLocation, Sound.ENTITY_WITHER_SHOOT, 0.3f, 2f)

            val ploc = player.eyeLocation.add(0.0, -1.5, 0.0)
            val pvec = player.eyeLocation.direction
            val vec = Vector(pvec.getX(), 0.0, pvec.getZ())
            val vv1 = Vector(pvec.getZ() * -1, 0.0, pvec.getX()).normalize().multiply(0.3)
            val vv2 = Vector(pvec.getZ(), 0.0, pvec.getX() * -1).normalize().multiply(0.3)
            val vec1 = Vector(pvec.getX(), 0.0, pvec.getZ()).normalize().multiply(1)
            val vec2 = Vector(pvec.getX(), 0.0, pvec.getZ()).normalize().multiply(1.3)
            val vec3 = Vector(pvec.getX(), 0.0, pvec.getZ()).normalize().multiply(1.6)
            val loc1 = ploc.clone().add(vec1)
            val loc2 = ploc.clone().add(vec2)
            val loc3 = ploc.clone().add(vec3)
            val loc4 = loc2.clone().add(vv1)
            val loc5 = loc2.clone().add(vv2)

            player.velocity = vec.clone().multiply(-0.5)

            var y = 0.0
            while (y <= headdis) {
                ShootSnowball(player, loc1.clone().add(0.0, y, 0.0), vec.clone().normalize().multiply(1.8))
                ShootSnowball(player, loc3.clone().add(0.0, y, 0.0), vec.clone().normalize().multiply(1.8))
                ShootSnowball(player, loc4.clone().add(0.0, y, 0.0), vec.clone().normalize().multiply(1.8))
                ShootSnowball(player, loc5.clone().add(0.0, y, 0.0), vec.clone().normalize().multiply(1.8))
                y += 0.5
            }
        }

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    getPlayerData(p)!!.canUseSubWeapon = true
                }
            }
        task.runTaskLater(plugin, 20)
    }

    fun ShootSnowball(
        player: Player,
        loc: Location,
        vec: Vector,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            val i = ItemStack(getPlayerData(p)!!.team.teamColor!!.wool!!).clone()
                            val i_m = i.itemMeta
                            i_m!!.setLocalizedName(notDuplicateNumber.toString())
                            i.itemMeta = i_m
                            drop = p.world.dropItem(loc, i)
                            drop!!.velocity = vec
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.world.spawnEntity(loc, EntityType.SNOWBALL) as Snowball
                            ball!!.shooter = p
                            ball!!.velocity = vec
                            ball!!.customName = "SuperShot"
                            ball!!.shooter = p
                            for (o_player in plugin.server.onlinePlayers) {
                                val connection = (o_player as CraftPlayer).handle.playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                            }
                        }
                        drop!!.velocity = ball!!.velocity

                        PaintMgr.PaintHightestBlock(ball!!.location, p, false, false)

                        if (Random().nextInt(20) == 0) {
                            val bd =
                                getPlayerData(p)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (o_player in plugin.server.onlinePlayers) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) {
                                    if (o_player.world ===
                                        ball!!.world
                                    ) {
                                        if (o_player
                                                .location
                                                .distanceSquared(ball!!.location) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            o_player.spawnParticle<BlockData?>(
                                                Particle.BLOCK_DUST,
                                                ball!!.location,
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

                        if (ball!!.isDead || drop!!.isDead || !p.isOnline || !getPlayerData(p)!!.isInMatch) {
                            ball!!.remove()
                            drop!!.remove()
                            cancel()
                        }

                        c++
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
                        plugin.logger.warning(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
