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
    fun MMLockRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var ps: MutableMap<Player?, EntitySquid> = HashMap<Player?, EntitySquid>()
                var asl: MutableMap<Entity?, EntityArmorStand> = HashMap<Entity?, EntityArmorStand>()
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    try {
                        if (c == 0) {
                            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100000, 10))
                            p.getInventory().clear()
                            val item = ItemStack(Material.PRISMARINE_SHARD)
                            val meta = item.getItemMeta()
                            meta!!.setDisplayName("プレイヤーを狙って右クリックで発射")
                            item.setItemMeta(meta)
                            for (count in 0..8) {
                                player.getInventory().setItem(count, item)
                            }
                            player.updateInventory()

                            getPlayerData(p)!!.setIsUsingMM(true)
                            val nmsWorld = (p.getWorld() as CraftWorld).getHandle()
                            for (op in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(op)!!.isInMatch() && op.getWorld() === p.getWorld() && (op.getName() != p.getName()) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val loc: Location = op.getLocation()
                                    val es = EntitySquid(EntityTypes.SQUID, nmsWorld)
                                    es.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
                                    es.setInvisible(true)
                                    es.setNoGravity(true)
                                    es.setNoAI(true)
                                    ps.put(op, es)
                                    (p as CraftPlayer)
                                        .getHandle()
                                        .playerConnection
                                        .sendPacket(PacketPlayOutSpawnEntityLiving(es))
                                }
                            }
                            for (e in p.getWorld().getEntities()) {
                                if (e is ArmorStand) {
                                    val `as` = e
                                    if (`as`.getCustomName() == null) continue
                                    if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                        (`as`.getCustomName() != "100") &&
                                        (`as`.getCustomName() != "SplashShield") &&
                                        (`as`.getCustomName() != "Kasa")
                                    ) {
                                        val loc = `as`.getLocation()
                                        val eas =
                                            EntityArmorStand(
                                                nmsWorld,
                                                loc.getX(),
                                                loc.getY(),
                                                loc.getZ(),
                                            )
                                        eas.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
                                        eas.setInvisible(true)
                                        eas.setSmall(`as`.isSmall())
                                        eas.setBasePlate(`as`.hasBasePlate())
                                        eas.setNoGravity(true)
                                        asl.put(`as`, eas)
                                        (p as CraftPlayer)
                                            .getHandle()
                                            .playerConnection
                                            .sendPacket(PacketPlayOutSpawnEntityLiving(eas))
                                    }
                                }
                            }
                        }
                        if (c != 0) {
                            for (op in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(op)!!.isInMatch() && op.getWorld() === p.getWorld() && (op.getName() != p.getName()) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val es: EntitySquid = ps.get(op)!!
                                    val loc: Location = op.getLocation()
                                    es.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
                                    setGlowing(es.getBukkitEntity(), p, MMCheckCanLock(p, op))
                                    (p as CraftPlayer)
                                        .getHandle()
                                        .playerConnection
                                        .sendPacket(PacketPlayOutEntityTeleport(es))
                                }
                            }
                            for (e in p.getWorld().getEntities()) {
                                if (e is ArmorStand) {
                                    val `as` = e
                                    if (`as`.getCustomName() == null) continue
                                    if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                        (`as`.getCustomName() != "100") &&
                                        (`as`.getCustomName() != "SplashShield") &&
                                        (`as`.getCustomName() != "Kasa")
                                    ) {
                                        val eas: EntityArmorStand = asl.get(`as`)!!
                                        val loc = `as`.getLocation()
                                        eas.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
                                        setGlowing(eas.getBukkitEntity(), p, MMCheckCanLock(p, `as`))
                                        (p as CraftPlayer)
                                            .getHandle()
                                            .playerConnection
                                            .sendPacket(PacketPlayOutEntityTeleport(eas))
                                    }
                                }
                            }
                        }
                        if (!getPlayerData(p)!!.getIsUsingMM() || c == 200) {
                            val targetList: MutableList<Entity> = ArrayList<Entity>()
                            var count = 0
                            for (op in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(op)!!.isInMatch() && op.getWorld() === p.getWorld() && (op.getName() != p.getName()) &&
                                    getPlayerData(
                                        p,
                                    )!!.team != getPlayerData(op)!!.team
                                ) {
                                    val es: EntitySquid = ps.get(op)!!
                                    (p as CraftPlayer)
                                        .getHandle()
                                        .playerConnection
                                        .sendPacket(PacketPlayOutEntityDestroy(es.getBukkitEntity().getEntityId()))
                                    if (MMCheckCanLock(p, op)) {
                                        op.sendTitle("", ChatColor.RED.toString() + "ミサイル接近中！", 0, 40, 4)
                                        op.playSound(op.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                                        op.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30, 1))
                                        targetList.add(op)
                                        count++
                                    }
                                }
                            }
                            for (e in p.getWorld().getEntities()) {
                                if (e is ArmorStand) {
                                    val `as` = e
                                    if (`as`.getCustomName() == null) continue
                                    if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                        (`as`.getCustomName() != "100") &&
                                        (`as`.getCustomName() != "SplashShield") &&
                                        (`as`.getCustomName() != "Kasa")
                                    ) {
                                        val eas: EntityArmorStand = asl.get(`as`)!!
                                        (p as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityDestroy(eas.getBukkitEntity().getEntityId()),
                                        )
                                        val loc = `as`.getLocation()
                                        eas.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
                                        if (MMCheckCanLock(p, `as`)) {
                                            targetList.add(`as`)
                                            `as`.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30, 1))
                                            count++
                                        }
                                    }
                                }
                            }

                            for (e in targetList) MMShootRunnable(p, e, if (count >= 4) 2 else 4)

                            if (p.hasPotionEffect(PotionEffectType.SLOW)) p.removePotionEffect(PotionEffectType.SLOW)
                            p.getInventory().clear()
                            WeaponClassMgr.setWeaponClass(p)
                            getPlayerData(p)!!.setIsUsingSP(true)
                            getPlayerData(p)!!.setIsUsingMM(false)
                            FireworksRunnable(p)
                            SPWeaponMgr.setSPCoolTimeAnimation(p, 100)
                            cancel()
                        }
                        if (!getPlayerData(p)!!.isInMatch() || !p.isOnline() || p.getGameMode() == GameMode.SPECTATOR) {
                            getPlayerData(p)!!.setIsUsingSP(false)
                            cancel()
                        }
                        c++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun FireworksRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var i: Int = 0

                override fun run() {
                    try {
                        val f = p.getWorld().spawn<Firework?>(p.getLocation(), Firework::class.java)
                        i++
                        if (i == 5) cancel()
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }

    fun MMShootRunnable(
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
                        if (c == i || t.getGameMode() == GameMode.SPECTATOR || !getPlayerData(s)!!.isInMatch()) cancel()
                    } else {
                        if (c == i || !getPlayerData(s)!!.isInMatch()) cancel()
                    }
                    MMRunnable(s, target)
                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 10)
    }

    fun MMRunnable(
        shooter: Player,
        target: Entity,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var s: Player = shooter
                var t: Entity = target
                var tl: Location = target.getLocation()
                var drop: Item? = null
                var ball: Snowball? = null
                var c: Int = 0
                var reached: Boolean = false

                override fun run() {
                    if (c == 0) {
                        drop =
                            shooter.getWorld().dropItem(
                                t.getLocation().add(0.0, 40.0, 0.0),
                                ItemStack(getPlayerData(s)!!.team.teamColor!!.wool!!),
                            )
                        drop!!.setGravity(false)
                        ball = s.getWorld().spawnEntity(drop!!.getLocation(), EntityType.SNOWBALL) as Snowball
                        ball!!.setGravity(false)
                        ball!!.setShooter(s)
                        ball!!.setVelocity(Vector(0, 0, 0))
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            val connection = (o_player as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                        }
                        if (t is Player) {
                            if (getPlayerData(t as Player)!!.isInMatch()) {
                                tl =
                                    getPlayerData(t as Player?)!!.playerGroundLocation
                            }
                        }
                        setSnowballIsHit(ball, false)
                    }

                    if (!getPlayerData(s)!!.isInMatch() || !s.isOnline() || drop!!.isDead()) {
                        drop!!.remove()
                        ball!!.remove()
                        cancel()
                    }

                    val dl = drop!!.getLocation()

                    if (!drop!!.isOnGround()) {
                        ball!!.teleport(drop!!.getLocation())
                        ball!!.setVelocity(drop!!.getVelocity())
                    }

                    if (dl.distanceSquared(tl) < 100 /* 10^2 */) {
                        reached = true
                    }

                    if (t is Player) {
                        if ((t as Player).getGameMode() == GameMode.SPECTATOR || !(t as Player).isOnline()) {
                            reached =
                                true
                        }
                    }

                    // if(!reached)
                    // tl = t.getLocation();
                    if (!reached) {
                        drop!!.setVelocity(
                            (Vector(tl.getX() - dl.getX(), tl.getY() - dl.getY(), tl.getZ() - dl.getZ()))
                                .normalize()
                                .multiply(0.8),
                        )
                    } else {
                        drop!!.setVelocity(drop!!.getVelocity().add(Vector(0.0, -0.1, 0.0)))
                    }

                    val bd =
                        getPlayerData(s)!!
                            .team.teamColor!!
                            .wool!!
                            .createBlockData()
                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        if (o_player.getWorld() === drop!!.getLocation().getWorld()) {
                            if (o_player
                                    .getLocation()
                                    .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                            ) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) {
                                    o_player.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        drop!!.getLocation(),
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
                        s.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(drop!!.getLocation(), maxDist, 25, s)

                        // 塗る
                        var i = 0
                        while (i <= maxDist) {
                            val p_locs: MutableList<Location> = getSphere(drop!!.getLocation(), i.toDouble(), 20)
                            for (loc in p_locs) {
                                PaintMgr.Paint(loc, s, false)
                            }
                            i++
                        }

                        // 攻撃判定の処理
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== s.getWorld()) continue
                            if (target.getLocation().distanceSquared(drop!!.getLocation()) <= maxDistSquared) {
                                val damage = (maxDist - target.getLocation().distance(drop!!.getLocation())) * 14
                                if (getPlayerData(s)!!.team != getPlayerData(target)!!.team &&
                                    target.getGameMode() == GameMode.ADVENTURE
                                ) {
                                    giveDamage(s, target, damage, "spWeapon")

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

                        for (`as` in s.getWorld().getEntities()) {
                            if (`as`.getLocation().distanceSquared(drop!!.getLocation()) <= maxDistSquared) {
                                if (`as` is ArmorStand) {
                                    val damage = (maxDist - `as`.getLocation().distance(drop!!.getLocation())) * 2
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

    fun MMSquidRunnable(
        shooter: Player,
        target: Player,
    ) {
        val nmsWorld = (target.getWorld() as CraftWorld).getHandle()
        val loc = target.getLocation()
        val es = EntitySquid(EntityTypes.SQUID, nmsWorld)
        es.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
        es.setInvisible(true)
        es.setNoGravity(true)
        es.setNoAI(true)
        es.setFlag(6, true)
        (shooter as CraftPlayer).getHandle().playerConnection.sendPacket(PacketPlayOutSpawnEntityLiving(es))
    }

    fun MMCheckCanLock(
        sp: Player,
        target: Entity,
    ): Boolean {
        val sv = sp.getEyeLocation().getDirection().normalize()
        val tl = target.getLocation()
        val sl = sp.getLocation()
        val tpv = (Vector(tl.getX() - sl.getX(), tl.getY() - sl.getY(), tl.getZ() - sl.getZ())).normalize()
        val angle = sv.angle(tpv)
        return angle < 0.4f
    }
}
