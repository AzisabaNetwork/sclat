package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GlowingAPI.setGlowing
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.EntitySquid
import net.minecraft.server.v1_14_R1.EntityTypes
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityTeleport
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
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
object MultiMissile {
    @JvmStatic
    fun mmLockRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var ps: MutableMap<Player?, EntitySquid> = HashMap()
                var asl: MutableMap<Entity?, EntityArmorStand> = HashMap()
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100000, 10))
                            p.inventory.clear()
                            val item = ItemStack(Material.PRISMARINE_SHARD)
                            val meta = item.itemMeta
                            meta!!.setDisplayName("プレイヤーを狙って右クリックで発射")
                            item.itemMeta = meta
                            for (count in 0..8) {
                                player.inventory.setItem(count, item)
                            }
                            player.updateInventory()

                            getPlayerData(p)!!.isUsingMM = true
                            val nmsWorld = (p.world as CraftWorld).handle
                            for (op in plugin.server.onlinePlayers) {
                                sclatLogger.debug("For player: ${op.name}")
                                if (getPlayerData(op)!!.isInMatch &&
                                    op.world === p.world &&
                                    (op.name != p.name) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val loc: Location = op.location
                                    val es = EntitySquid(EntityTypes.SQUID, nmsWorld)
                                    es.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                                    es.isInvisible = true
                                    es.isNoGravity = true
                                    es.isNoAI = true
                                    ps[op] = es
                                    (p as CraftPlayer)
                                        .handle
                                        .playerConnection
                                        .sendPacket(PacketPlayOutSpawnEntityLiving(es))
                                } else {
                                    sclatLogger.debug("Player ${op.name} ignored.")
                                }
                            }
                            for (armorStand in p.world.entities) {
                                if (armorStand is ArmorStand) {
                                    if (armorStand.customName == null) continue
                                    if ((armorStand.customName != "Path") &&
                                        (armorStand.customName != "21") &&
                                        (armorStand.customName != "100") &&
                                        (armorStand.customName != "SplashShield") &&
                                        (armorStand.customName != "Kasa")
                                    ) {
                                        val loc = armorStand.location
                                        val eas =
                                            EntityArmorStand(
                                                nmsWorld,
                                                loc.x,
                                                loc.y,
                                                loc.z,
                                            )
                                        eas.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                                        eas.isInvisible = true
                                        eas.isSmall = armorStand.isSmall
                                        eas.setBasePlate(armorStand.hasBasePlate())
                                        eas.isNoGravity = true
                                        asl[armorStand] = eas
                                        (p as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(PacketPlayOutSpawnEntityLiving(eas))
                                    }
                                }
                            }
                        }
                        if (c != 0) {
                            sclatLogger.debug("Non-0 ticking for MultiMissile")
                            for (op in plugin.server.onlinePlayers) {
                                if (getPlayerData(op)!!.isInMatch &&
                                    op.world === p.world &&
                                    (op.name != p.name) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val es: EntitySquid = ps[op]!!
                                    val loc: Location = op.location
                                    es.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                                    val requireGlowing = mmCheckCanLock(p, op)
                                    setGlowing(es.bukkitEntity, p, requireGlowing)
                                    sclatLogger.debug("Set grow for ${op.name} to $requireGlowing")
                                    (p as CraftPlayer)
                                        .handle
                                        .playerConnection
                                        .sendPacket(PacketPlayOutEntityTeleport(es))
                                }
                            }
                            for (e in p.world.entities) {
                                if (e is ArmorStand) {
                                    val `as` = e
                                    if (`as`.customName == null) continue
                                    if ((`as`.customName != "Path") &&
                                        (`as`.customName != "21") &&
                                        (`as`.customName != "100") &&
                                        (`as`.customName != "SplashShield") &&
                                        (`as`.customName != "Kasa")
                                    ) {
                                        val eas: EntityArmorStand = asl[`as`]!!
                                        val loc = `as`.location
                                        eas.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                                        setGlowing(eas.bukkitEntity, p, mmCheckCanLock(p, `as`))
                                        (p as CraftPlayer)
                                            .handle
                                            .playerConnection
                                            .sendPacket(PacketPlayOutEntityTeleport(eas))
                                    }
                                }
                            }
                        }
                        if (!getPlayerData(p)!!.isUsingMM || c == 200) {
                            sclatLogger.debug("Shoot!!! by MultiMissile")
                            val targetList: MutableList<Entity> = ArrayList()
                            var count = 0
                            for (op in plugin.server.onlinePlayers) {
                                if (getPlayerData(op)!!.isInMatch &&
                                    op.world === p.world &&
                                    (op.name != p.name) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val es: EntitySquid = ps[op]!!
                                    (p as CraftPlayer)
                                        .handle
                                        .playerConnection
                                        .sendPacket(PacketPlayOutEntityDestroy(es.bukkitEntity.entityId))
                                    if (mmCheckCanLock(p, op)) {
                                        op.sendTitle("", ChatColor.RED.toString() + "ミサイル接近中！", 0, 40, 4)
                                        op.playSound(op.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                                        op.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30, 1))
                                        targetList.add(op)
                                        count++
                                    } else {
                                        sclatLogger.debug("Ignored for ${op.name}")
                                    }
                                }
                            }
                            for (armorStand in p.world.entities) {
                                if (armorStand is ArmorStand) {
                                    if (armorStand.customName == null) continue
                                    if ((armorStand.customName != "Path") &&
                                        (armorStand.customName != "21") &&
                                        (armorStand.customName != "100") &&
                                        (armorStand.customName != "SplashShield") &&
                                        (armorStand.customName != "Kasa")
                                    ) {
                                        val eas: EntityArmorStand = asl[armorStand]!!
                                        (p as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityDestroy(eas.bukkitEntity.entityId),
                                        )
                                        val loc = armorStand.location
                                        eas.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                                        if (mmCheckCanLock(p, armorStand)) {
                                            targetList.add(armorStand)
                                            armorStand.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30, 1))
                                            count++
                                        }
                                    }
                                }
                            }

                            for (e in targetList) mmShootRunnable(p, e, if (count >= 4) 2 else 4)

                            if (p.hasPotionEffect(PotionEffectType.SLOW)) p.removePotionEffect(PotionEffectType.SLOW)
                            p.inventory.clear()
                            WeaponClassMgr.setWeaponClass(p)
                            getPlayerData(p)!!.isUsingSP = true
                            getPlayerData(p)!!.isUsingMM = false
                            fireworksRunnable(p)
                            SPWeaponMgr.setSPCoolTimeAnimation(p, 100)

                            sclatLogger.debug("Cancelled! after SPWeaponMgr")
                            cancel()
                        }
                        if (!getPlayerData(p)!!.isInMatch || !p.isOnline || p.gameMode == GameMode.SPECTATOR) {
                            getPlayerData(p)!!.isUsingSP = false
                            sclatLogger.debug("Cancelled! after disable usingSP")
                            cancel()
                        }
                        c++
                    } catch (e: Exception) {
                        sclatLogger.error("Failed to process lock", e)
                        e.printStackTrace()
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun fireworksRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var i: Int = 0

                override fun run() {
                    try {
                        p.world.spawn<Firework?>(p.location, Firework::class.java)
                        i++
                        if (i == 5) cancel()
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }

    fun mmShootRunnable(
        shooter: Player,
        target: Entity,
        i: Int,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var s: Player = shooter
                var c: Int = 0

                override fun run() {
                    if (target is Player) {
                        val t = target
                        if (c == i || t.gameMode == GameMode.SPECTATOR || !getPlayerData(s)!!.isInMatch) cancel()
                    } else {
                        if (c == i || !getPlayerData(s)!!.isInMatch) cancel()
                    }
                    mmRunnable(s, target)
                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 10)
    }

    fun mmRunnable(
        shooter: Player,
        target: Entity,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var s: Player = shooter
                var t: Entity = target
                var tl: Location = target.location
                var drop: Item? = null
                var ball: Snowball? = null
                var c: Int = 0
                var reached: Boolean = false

                override fun run() {
                    if (c == 0) {
                        drop =
                            shooter.world.dropItem(
                                t.location.add(0.0, 40.0, 0.0),
                                ItemStack(getPlayerData(s)!!.team!!.teamColor!!.wool!!),
                            )
                        drop!!.setGravity(false)
                        ball = s.world.spawnEntity(drop!!.location, EntityType.SNOWBALL) as Snowball
                        ball!!.setGravity(false)
                        ball!!.shooter = s
                        ball!!.velocity = Vector(0, 0, 0)
                        for (o_player in plugin.server.onlinePlayers) {
                            val connection = (o_player as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                        }
                        if (t is Player) {
                            if (getPlayerData(t as Player)!!.isInMatch) {
                                tl =
                                    getPlayerData(t as Player?)!!.playerGroundLocation!!
                            }
                        }
                        setSnowballIsHit(ball, false)
                    }

                    if (!getPlayerData(s)!!.isInMatch || !s.isOnline || drop!!.isDead) {
                        drop!!.remove()
                        ball!!.remove()
                        cancel()
                    }

                    val dl = drop!!.location

                    if (!drop!!.isOnGround) {
                        ball!!.teleport(drop!!.location)
                        ball!!.velocity = drop!!.velocity
                    }

                    if (dl.distanceSquared(tl) < 100) { // 10^2
                        reached = true
                    }

                    if (t is Player) {
                        if ((t as Player).gameMode == GameMode.SPECTATOR || !(t as Player).isOnline) {
                            reached =
                                true
                        }
                    }

                    // if(!reached)
                    // tl = t.getLocation();
                    if (!reached) {
                        drop!!.velocity =
                            (Vector(tl.x - dl.x, tl.y - dl.y, tl.z - dl.z))
                                .normalize()
                                .multiply(0.8)
                    } else {
                        drop!!.velocity = drop!!.velocity.add(Vector(0.0, -0.1, 0.0))
                    }

                    val bd =
                        getPlayerData(s)!!
                            .team
                            ?.teamColor!!
                            .wool!!
                            .createBlockData()
                    for (o_player in plugin.server.onlinePlayers) {
                        if (o_player.world === drop!!.location.world) {
                            if (o_player
                                    .location
                                    .distanceSquared(drop!!.location) < Sclat.particleRenderDistanceSquared
                            ) {
                                if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon()) {
                                    o_player.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        drop!!.location,
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

                    if (getSnowballIsHit(ball)) {
                        // 半径
                        val maxDist = 3.0
                        val maxDistSquared = 9.0 // 3^2

                        // 爆発音
                        s.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(drop!!.location, maxDist, 25, s)

                        // 塗る
                        var i = 0
                        while (i <= maxDist) {
                            val pLocs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 20)
                            for (loc in pLocs) {
                                PaintMgr.paint(loc, s, false)
                            }
                            i++
                        }

                        // 攻撃判定の処理
                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.isInMatch || target.world !== s.world) continue
                            if (target.location.distanceSquared(drop!!.location) <= maxDistSquared) {
                                val damage = (maxDist - target.location.distance(drop!!.location)) * 14
                                if (getPlayerData(s)!!.team != getPlayerData(target)!!.team &&
                                    target.gameMode == GameMode.ADVENTURE
                                ) {
                                    giveDamage(s, target, damage, "spWeapon")

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

                        for (`as` in s.world.entities) {
                            if (`as`.location.distanceSquared(drop!!.location) <= maxDistSquared) {
                                if (`as` is ArmorStand) {
                                    val damage = (maxDist - `as`.location.distance(drop!!.location)) * 2
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, s)
                                }
                            }
                        }

                        drop!!.remove()
                        cancel()
                    }

                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun mmSquidRunnable(
        shooter: Player,
        target: Player,
    ) {
        val nmsWorld = (target.world as CraftWorld).handle
        val loc = target.location
        val es = EntitySquid(EntityTypes.SQUID, nmsWorld)
        es.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        es.isInvisible = true
        es.isNoGravity = true
        es.isNoAI = true
        es.setFlag(6, true)
        (shooter as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutSpawnEntityLiving(es))
    }

    fun mmCheckCanLock(
        sp: Player,
        target: Entity,
    ): Boolean {
        val sv = sp.eyeLocation.direction.normalize()
        val tl = target.location
        val sl = sp.location
        val tpv = (Vector(tl.x - sl.x, tl.y - sl.y, tl.z - sl.z)).normalize()
        val angle = sv.angle(tpv)
        return angle < 0.4f
    }
}
