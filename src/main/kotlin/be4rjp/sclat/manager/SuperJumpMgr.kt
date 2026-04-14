package be4rjp.sclat.manager

import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Location
import org.bukkit.Note
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 *
 * @author Be4rJP
 */
object SuperJumpMgr {
    @JvmStatic
    fun superJumpCollTime(
        player: Player,
        loc: Location,
        nearspawnpoint: Boolean,
    ) {
        if (player.world !== loc.world) return

        if (player.location.distance(loc) <= 3) {
            player.sendMessage(ChatColor.RED.toString() + "目的地が近すぎます！")
            player.playNote(player.location, Instrument.BASS_GUITAR, Note.flat(0, Note.Tone.G))
            return
        }

        player.inventory.clear()
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 40000, 10))
        getPlayerData(player)!!.armor = 0.0
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    if (player.hasPotionEffect(PotionEffectType.SLOW)) player.removePotionEffect(PotionEffectType.SLOW)
                    if (p.gameMode != GameMode.SPECTATOR) {
                        p.world.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
                        superJumpRunnable(p, loc)
                    }
                }
            }
        if (nearspawnpoint) {
            task.runTaskLater(plugin, 15)
        } else {
            if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.GLOWING,
                        50,
                        1,
                    ),
                )
            }
            task.runTaskLater(plugin, 50)
        }
    }

    fun superJumpRunnable(
        player: Player,
        toloc: Location,
    ) {
        if (player.location.distance(toloc) <= 3) {
            player.velocity = Vector(0, 2, 0)
            return
        }

        val from = player.location.clone()
        val to = toloc
        val vec = Vector(to.x - from.x, to.y - from.y, to.z - from.z).normalize()
        player.gameMode = GameMode.SPECTATOR
        getPlayerData(player)!!.isJumping = (true)
        val rayTrace1 = RayTrace(from.toVector(), vec)
        val positions: ArrayList<Vector> = rayTrace1.traverse(from.distance(to), 1.0)

        val coef = 0.16 / (from.distance(to) / 40).pow(2.0)

        /*
         * ray : for(int i = 1; i < positions.size();i++){ Location position =
         * positions.get(i).toLocation(player.getLocation().getWorld()); //double y =
         * (Math.pow(Math.abs((positions.size() / 2) - i), 2) * -1 * coef) +
         * (Math.abs(to.getY() - from.getY()) / 2) +
         * (Math.pow(Math.sqrt(Math.pow(from.distance(to), 2) +
         * (Math.pow(Math.abs(to.getY() - from.getY()) / 2, 2) * -4)) / 2, 2) * coef);
         * double y = (Math.pow(Math.abs((positions.size() / 2) - i), 2) * -1 * coef) +
         * (Math.pow(positions.size() / 2, 2) * coef); Location tloc = new
         * Location(player.getWorld(), position.getX(), y + position.getY(),
         * position.getZ()); Particle.DustOptions dustOptions = new
         * Particle.DustOptions(Color.BLUE, 1); player.spawnParticle(Particle.REDSTONE,
         * tloc, 1, 0, 0, 0, 1, dustOptions); }
         */
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var i: Int = 1
                var t: Int = 0

                override fun run() {
                    if (getPlayerData(p)!!.isDead) cancel()

                    val position = positions[i].toLocation(p.location.world!!)
                    val py = (
                        (abs((positions.size / 2) - i).toDouble().pow(2.0) * -1 * coef) +
                            ((positions.size / 2).toDouble().pow(2.0) * coef)
                    )
                    val y = if (py > 100) 100 + py / 2.8 else py
                    val tloc = Location(p.world, position.x, y + position.y, position.z)
                    val pvec =
                        Vector(
                            tloc.x - p.location.x,
                            tloc.y - p.location.y,
                            tloc.z - p.location.z,
                        ).multiply(0.17)
                    p.velocity = pvec
                    if (tloc.distance(p.location) < 15) i++
                    if (i == positions.size - 2) {
                        p.gameMode = GameMode.ADVENTURE
                        WeaponClassMgr.setWeaponClass(p)
                        p.closeInventory()
                        p.inventory.heldItemSlot = 0
                    }

                    if (t > 200 || (p.isOnGround && t >= 20)) { // スタック回避
                        p.gameMode = GameMode.ADVENTURE
                        WeaponClassMgr.setWeaponClass(p)
                        p.closeInventory()
                        p.inventory.heldItemSlot = 0
                        p.teleport(toloc.clone().add(0.0, 4.0, 0.0))
                        getPlayerData(player)!!.isJumping = (false)
                        cancel()
                    }

                    if (i == positions.size || !getPlayerData(p)!!.isInMatch || !p.isOnline || getPlayerData(p)!!.isUsingTyakuti) {
                        getPlayerData(player)!!.isJumping = (false)
                        cancel()
                    }

                    t++
                }
            }
        task.runTaskTimer(plugin, 0, 1)

        val effect: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0
                var id: Int = 0

                override fun run() {
                    if (c == 0) {
                        val nmsWorld = (p.world as CraftWorld).handle
                        val `as` = EntityArmorStand(nmsWorld, toloc.x, toloc.y, toloc.z)
                        `as`.setPosition(toloc.x, toloc.y, toloc.z)
                        `as`.isInvisible = true
                        `as`.isNoGravity = true
                        `as`.setBasePlate(false)
                        `as`.customName =
                            CraftChatMessage.fromStringOrNull(
                                getPlayerData(p)!!.team!!.teamColor!!.colorCode + "↓↓↓  くコ:彡  ↓↓↓",
                            )
                        `as`.customNameVisible = true
                        `as`.isSmall = true
                        id = `as`.bukkitEntity.entityId
                        for (target in plugin.server.onlinePlayers) {
                            if (p.world === target.world) {
                                (target as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
                            }
                        }
                    }
                    // エフェクト
                    val r = 0.5
                    val x = to.x + r * cos(c.toDouble())
                    val y = to.y + 0.4
                    val z = to.z + r * sin(c.toDouble())
                    val tl = Location(p.world, x, y, z)
                    val dustOptions =
                        Particle.DustOptions(
                            getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                            1f,
                        )
                    p
                        .world
                        .spawnParticle<Particle.DustOptions?>(Particle.REDSTONE, tl, 1, 0.0, 0.1, 0.0, 50.0, dustOptions)
                    if (p.gameMode == GameMode.ADVENTURE || !getPlayerData(p)!!.isInMatch || !p.isOnline) {
                        for (target in plugin.server.onlinePlayers) {
                            if (p.world === target.world) {
                                (target as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(PacketPlayOutEntityDestroy(id))
                            }
                        }
                        cancel()
                    }
                    c++
                }
            }
        effect.runTaskTimer(plugin, 0, 1)
    }
}
