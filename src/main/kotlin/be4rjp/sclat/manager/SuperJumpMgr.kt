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
        if (player.getWorld() !== loc.getWorld()) return

        if (player.getLocation().distance(loc) <= 3) {
            player.sendMessage(ChatColor.RED.toString() + "目的地が近すぎます！")
            player.playNote(player.getLocation(), Instrument.BASS_GUITAR, Note.flat(0, Note.Tone.G))
            return
        }

        player.getInventory().clear()
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 40000, 10))
        getPlayerData(player)!!.armor = 0.0
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    if (player.hasPotionEffect(PotionEffectType.SLOW)) player.removePotionEffect(PotionEffectType.SLOW)
                    if (p.getGameMode() != GameMode.SPECTATOR) {
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
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
        if (player.getLocation().distance(toloc) <= 3) {
            player.setVelocity(Vector(0, 2, 0))
            return
        }

        val from = player.getLocation().clone()
        val to = toloc
        val vec = Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()).normalize()
        player.setGameMode(GameMode.SPECTATOR)
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

                    val position = positions.get(i).toLocation(p.getLocation().getWorld()!!)
                    val py = (
                        (abs((positions.size / 2) - i).toDouble().pow(2.0) * -1 * coef) +
                            ((positions.size / 2).toDouble().pow(2.0) * coef)
                    )
                    val y = if (py > 100) 100 + py / 2.8 else py
                    val tloc = Location(p.getWorld(), position.getX(), y + position.getY(), position.getZ())
                    val pvec =
                        Vector(
                            tloc.getX() - p.getLocation().getX(),
                            tloc.getY() - p.getLocation().getY(),
                            tloc.getZ() - p.getLocation().getZ(),
                        ).multiply(0.17)
                    p.setVelocity(pvec)
                    if (tloc.distance(p.getLocation()) < 15) i++
                    if (i == positions.size - 2) {
                        p.setGameMode(GameMode.ADVENTURE)
                        WeaponClassMgr.setWeaponClass(p)
                        p.closeInventory()
                        p.getInventory().setHeldItemSlot(0)
                    }

                    if (t > 200 || (p.isOnGround() && t >= 20)) { // スタック回避
                        p.setGameMode(GameMode.ADVENTURE)
                        WeaponClassMgr.setWeaponClass(p)
                        p.closeInventory()
                        p.getInventory().setHeldItemSlot(0)
                        p.teleport(toloc.clone().add(0.0, 4.0, 0.0))
                        getPlayerData(player)!!.isJumping = (false)
                        cancel()
                    }

                    if (i == positions.size || !getPlayerData(p)!!.isInMatch || !p.isOnline() || getPlayerData(p)!!.isUsingTyakuti) {
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
                        val nmsWorld = (p.getWorld() as CraftWorld).getHandle()
                        val `as` = EntityArmorStand(nmsWorld, toloc.getX(), toloc.getY(), toloc.getZ())
                        `as`.setPosition(toloc.getX(), toloc.getY(), toloc.getZ())
                        `as`.setInvisible(true)
                        `as`.setNoGravity(true)
                        `as`.setBasePlate(false)
                        `as`.setCustomName(
                            CraftChatMessage.fromStringOrNull(
                                getPlayerData(p)!!.team!!.teamColor!!.colorCode + "↓↓↓  くコ:彡  ↓↓↓",
                            ),
                        )
                        `as`.setCustomNameVisible(true)
                        `as`.setSmall(true)
                        id = `as`.getBukkitEntity().getEntityId()
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (p.getWorld() === target.getWorld()) {
                                (target as CraftPlayer)
                                    .getHandle()
                                    .playerConnection
                                    .sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
                            }
                        }
                    }
                    // エフェクト
                    val r = 0.5
                    val x = to.getX() + r * cos(c.toDouble())
                    val y = to.getY() + 0.4
                    val z = to.getZ() + r * sin(c.toDouble())
                    val tl = Location(p.getWorld(), x, y, z)
                    val dustOptions =
                        Particle.DustOptions(
                            getPlayerData(p)!!.team!!.teamColor!!.bukkitColor!!,
                            1f,
                        )
                    p
                        .getWorld()
                        .spawnParticle<Particle.DustOptions?>(Particle.REDSTONE, tl, 1, 0.0, 0.1, 0.0, 50.0, dustOptions)
                    if (p.getGameMode() == GameMode.ADVENTURE || !getPlayerData(p)!!.isInMatch || !p.isOnline()) {
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (p.getWorld() === target.getWorld()) {
                                (target as CraftPlayer)
                                    .getHandle()
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
