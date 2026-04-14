package be4rjp.sclat.manager

import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.addPathArmorStandList
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.Match
import be4rjp.sclat.data.Path
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object PathMgr {
    fun setPath(
        player: Player,
        from: Location,
        to: Location,
        path: Path,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var drop: Item? = null
                var c: Int = 0
                var vec: Vector? = null

                override fun run() {
                    if (c == 0) {
                        drop =
                            p.world.dropItem(
                                from.clone().add(0.0, -0.25, 0.0),
                                ItemStack(getPlayerData(p)!!.team!!.teamColor!!.wool!!),
                            )
                        drop!!.setGravity(false)
                        drop!!.customName = notDuplicateNumber.toString()
                        // vec = (to.subtract(from)).toVector().normalize();
                        vec =
                            Vector(to.x - from.x, to.y - from.y, to.z - from.z)
                                .normalize()
                                .multiply(0.5)
                        drop!!.addPassenger(p)
                        getPlayerData(p)!!.isOnPath = (true)
                    }

                    drop!!.velocity = vec!!

                    var `is` =
                        drop!!.location.distanceSquared(from) > from.distanceSquared(to) ||
                            !drop!!
                                .passengers
                                .contains(p) ||
                            !getPlayerData(p)!!.isInMatch ||
                            (
                                p
                                    .inventory
                                    .itemInMainHand
                                    .type != Material.AIR
                            )
                    if (path.getTeam() == null) {
                        `is` = true
                    } else if (path.getTeam() != getPlayerData(p)!!.team) {
                        `is` = true
                    }

                    if (`is`) {
                        getPlayerData(p)!!.isOnPath = (false)
                        drop!!.remove()
                        cancel()
                    }

                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun setupPath(m: Match) {
        for (path in m.mapData!!.pathList) {
            val from = path!!.fromLocation!!.clone()

            val ast: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        val `as` =
                            from.world!!.spawnEntity(
                                from.clone().add(0.0, -0.9, 0.0),
                                EntityType.ARMOR_STAND,
                            ) as ArmorStand
                        `as`.isGlowing = false
                        `as`.setGravity(false)
                        `as`.isVisible = false
                        `as`.isSmall = true
                        `as`.customName = "Path"
                        `as`.isCustomNameVisible = false
                        for (target in plugin.server.onlinePlayers) {
                            if (`as`.world !== target.world) continue
                            (target as CraftPlayer)
                                .handle
                                .playerConnection
                                .sendPacket(
                                    PacketPlayOutEntityEquipment(
                                        `as`.entityId,
                                        EnumItemSlot.HEAD,
                                        CraftItemStack.asNMSCopy(ItemStack(Material.WHITE_STAINED_GLASS)),
                                    ),
                                )
                        }
                        addPathArmorStandList(`as`)
                        path.armorStand = `as`
                    }
                }
            ast.runTaskLater(plugin, 1)

            val effect: BukkitRunnable =
                object : BukkitRunnable() {
                    val path1: Path = path
                    val from: Location = path.fromLocation.clone()
                    val to: Location = path.toLocation!!.clone()
                    val match: Match = m

                    override fun run() {
                        val team = path1.getTeam()
                        val rayTrace =
                            RayTrace(
                                from.toVector(),
                                Vector(to.x - from.x, to.y - from.y, to.z - from.z)
                                    .normalize(),
                            )
                        val positions = rayTrace.traverse(from.distance(to), 0.5)
                        for (vector in positions) {
                            val position = vector.toLocation(from.world!!)
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.settings!!.showEffectChargerLine()) continue
                                val dustOptions: Particle.DustOptions?
                                dustOptions =
                                    if (team == null) {
                                        Particle.DustOptions(Color.WHITE, 1f)
                                    } else {
                                        Particle.DustOptions(team.teamColor!!.bukkitColor!!, 1f)
                                    }
                                target.spawnParticle<Particle.DustOptions?>(
                                    Particle.REDSTONE,
                                    position,
                                    1,
                                    0.0,
                                    0.0,
                                    0.0,
                                    25.0,
                                    dustOptions,
                                )
                            }
                        }
                        if (match.isFinished) {
                            cancel()
                        }
                    }
                }
            effect.runTaskTimer(plugin, 0, 5)

            val task: BukkitRunnable =
                object : BukkitRunnable() {
                    val path1: Path = path
                    val from: Location = path.fromLocation.clone()
                    val to: Location = path.toLocation.clone()
                    val match: Match = m
                    var c: Int = 0

                    override fun run() {
                        val team = path1.getTeam()
                        for (player in plugin.server.onlinePlayers) {
                            if (team != null) {
                                if (getPlayerData(player)!!.isInMatch &&
                                    player.world === from.world &&
                                    player
                                        .inventory
                                        .itemInMainHand
                                        .type == Material.AIR &&
                                    getPlayerData(player)!!.team == team &&
                                    !getPlayerData(player)!!.isOnPath
                                ) {
                                    if (player.location.distanceSquared(from) < 1) { // 1*1
                                        setPath(
                                            player,
                                            from,
                                            to,
                                            path1,
                                        )
                                    }
                                }
                            }
                        }

                        if (c % 10 == 0) {
                            if (team == null) {
                                for (player in plugin.server.onlinePlayers) {
                                    if (from.world === player.world) {
                                        (player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                path1.armorStand!!.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack
                                                    .asNMSCopy(ItemStack(Material.WHITE_STAINED_GLASS)),
                                            ),
                                        )
                                    }
                                }
                            } else {
                                for (player in plugin.server.onlinePlayers) {
                                    if (from.world === player.world) {
                                        (player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                path1.armorStand!!.entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack
                                                    .asNMSCopy(ItemStack(team.teamColor!!.glass!!)),
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        if (match.isFinished) cancel()

                        c++
                    }
                }
            task.runTaskTimer(plugin, 2, 1)
        }
    }
}
