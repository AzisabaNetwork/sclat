package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.getSprinklerFromplayer
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.data.DataMgr.snowballNameMap
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import be4rjp.sclat.weapon.Gear
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
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
object Sprinkler {
    @JvmStatic
    fun sprinklerRunnable(player: Player) {
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
                var ball: Snowball? = null
                var ndn: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            if (!getPlayerData(player)!!.isBombRush) {
                                p.exp = p.exp - (0.59 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()
                            }
                            val bom = ItemStack(Material.BIRCH_FENCE_GATE).clone()
                            val bomM = bom.itemMeta
                            ndn = notDuplicateNumber
                            bomM!!.setLocalizedName(ndn.toString())
                            bom.itemMeta = bomM
                            drop = p.world.dropItem(p.eyeLocation, bom)
                            drop!!.velocity = p.eyeLocation.direction
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball?>(Snowball::class.java)
                            ball!!.velocity = Vector(0, 0, 0)
                            setSnowballIsHit(ball, false)
                            ball!!.customName = ndn.toString()
                            snowballNameMap.put(ndn.toString(), ball)
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.server.onlinePlayers) {
                                val connection = (o_player as CraftPlayer).handle.playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                            }
                            pVec = p.eyeLocation.direction
                        }

                        ball = snowballNameMap.get(ndn.toString())

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
                            val `as` = getSprinklerFromplayer(player)
                            `as`!!.isVisible = false
                            `as`.setHelmet(ItemStack(Material.AIR))
                            `as`.teleport(drop!!.location.add(0.0, -0.4, 0.0))
                            `as`.customName = "21"
                            sprinklerRunnable2(`as`, p)
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // 視認用エフェクト
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

        if (player.exp > (0.6 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()) {
            task.runTaskTimer(
                plugin,
                0,
                1,
            )
        } else {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }
    }

    fun sprinklerRunnable2(
        `as`: ArmorStand,
        player: Player?,
    ) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (target in plugin.server.onlinePlayers) {
                        if (`as`.world !== target.world) continue
                        (target as CraftPlayer).handle.playerConnection.sendPacket(
                            PacketPlayOutEntityEquipment(
                                `as`.entityId,
                                EnumItemSlot.HEAD,
                                CraftItemStack.asNMSCopy(
                                    ItemStack(getPlayerData(player)!!.team!!.teamColor!!.glass!!),
                                ),
                            ),
                        )
                    }
                    `as`.world.playSound(`as`.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                    // as.setHelmet(new
                    // ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().glass));
                }
            }
        delay.runTaskLater(plugin, 10)
    }
}
