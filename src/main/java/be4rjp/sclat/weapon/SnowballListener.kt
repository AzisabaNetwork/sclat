package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.addSnowballHitCount
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.data.DataMgr.snowballIsHitMap
import be4rjp.sclat.data.DataMgr.snowballNameMap
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Timer
import java.util.TimerTask

/**
 *
 * @author Be4rJP
 */
class SnowballListener : Listener {
    @EventHandler
    fun onBlockHit(event: ProjectileHitEvent) {
        if (Sclat.type == ServerType.LOBBY) return

        // EntityDamage
        if (event.getHitEntity() != null) {
            if (snowballIsHitMap.containsKey(event.getEntity() as Snowball)) {
                setSnowballIsHit(event.getEntity() as Snowball, true)
                if (event.getEntity().getCustomName() != null) {
                    if (event.getEntity().getCustomName() == "JetPack" ||
                        event.getEntity().getCustomName() == "SuperShot"
                    ) {
                        val projectile = event.getEntity()
                        val shooter = projectile.getShooter() as Player?
                        if (event.getHitEntity() is Player) {
                            val target = event.getHitEntity() as Player?
                            if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                                target!!.getGameMode() == GameMode.ADVENTURE
                            ) {
                                if (!getPlayerData(shooter)!!.getIsUsingSP()) SPWeaponMgr.addSPCharge(shooter)

                                SclatUtil.giveDamage(shooter, target, 47.0, "spWeapon")
                            }
                        } else if (event.getHitEntity() is ArmorStand) {
                            val `as` = event.getHitEntity() as ArmorStand?
                            ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                        }
                    }
                }
            } else {
                val projectile = event.getEntity()
                val shooter = projectile.shooter as Player
                if (event.getHitEntity() is Player) {
                    val target = event.hitEntity as Player
                    if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                        target.getGameMode() == GameMode.ADVENTURE
                    ) {
                        if (!getPlayerData(shooter)!!.getIsUsingSP()) SPWeaponMgr.addSPCharge(shooter)
                        if (getPlayerData(target)!!.armor > 0) {
                            target.getWorld().playSound(
                                target.getLocation(),
                                Sound.ENTITY_SPLASH_POTION_BREAK,
                                1f,
                                1.5f,
                            )
                            if (getPlayerData(target)!!.armor > 10000) {
                                val vec = projectile.getVelocity()
                                val v = Vector(vec.getX(), 0.0, vec.getZ()).normalize()
                                target.setVelocity(Vector(v.getX(), 0.2, v.getZ()).multiply(0.33))
                            }
                        }
                        if (projectile.getCustomName() != null) {
                            if (projectile.getCustomName() == "Sprinkler" ||
                                projectile.getCustomName() == "Amehurasi"
                            ) {
                                if (projectile.getCustomName() == "Sprinkler") {
                                    SclatUtil.giveDamage(
                                        shooter,
                                        target,
                                        4.0,
                                        "subWeapon",
                                    )
                                } else if (projectile.getCustomName() == "Amehurasi") {
                                    SclatUtil.giveDamage(
                                        shooter,
                                        target,
                                        4.0,
                                        "spWeapon",
                                    )
                                }
                                PaintMgr.Paint(target.getLocation(), shooter, true)
                            }

                            if (projectile.getCustomName() == "SuperShot") {
                                shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                SclatUtil.giveDamage(shooter, target, 47.0, "spWeapon")
                            }
                            if (projectile.getCustomName()!!.contains("#QuadroArmsSpinner")) {
                                shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                var Quadrodamage = 1.0
                                var QuadroticksLived = projectile.getTicksLived().toDouble() * 12.5
                                if (QuadroticksLived > 60) QuadroticksLived = 60.0
                                Quadrodamage += Quadrodamage * (QuadroticksLived / 30)
                                SclatUtil.giveDamage(shooter, target, Quadrodamage, "spWeapon")
                                return
                            }
                            if (projectile.getCustomName()!!.contains("#QuadroArmsShotgun")) {
                                shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                var Quadrodamage = 9.0
                                var QuadroticksLived = projectile.getTicksLived().toDouble() * 10
                                if (QuadroticksLived > 30) QuadroticksLived = 30.0
                                Quadrodamage -= Quadrodamage * (QuadroticksLived / 100)
                                if (projectile.getCustomName()!!.contains("CounterShot")) {
                                    Quadrodamage = 6.5
                                }
                                SclatUtil.giveDamage(shooter, target, Quadrodamage, "spWeapon")
                                return
                            }

                            if (DataMgr.mws.contains(projectile.getCustomName())) {
                                var dmgDouble = 1.0
                                if (DataMgr.tsl.contains(projectile.getCustomName())) {
                                    if (projectile.getCustomName()!!.contains("#slided")) {
                                        dmgDouble =
                                            getPlayerData(shooter)!!
                                                .getWeaponClass()
                                                .mainWeapon!!
                                                .decreaseRate
                                    } else if (!projectile.getCustomName()!!.contains(":")) {
                                        shooter!!.playSound(
                                            shooter.getLocation(),
                                            Sound.ENTITY_ARROW_HIT_PLAYER,
                                            1.2f,
                                            1.3f,
                                        )
                                        shooter.spawnParticle(
                                            Particle.FLASH,
                                            projectile.getLocation(),
                                            1,
                                            0.1,
                                            0.1,
                                            0.1,
                                            0.1,
                                        )
                                    } else {
                                        val args: Array<String?>? =
                                            projectile
                                                .getCustomName()!!
                                                .split(":".toRegex())
                                                .dropLastWhile { it.isEmpty() }
                                                .toTypedArray()
                                        when (args!![1]) {
                                            "Burst" -> {
                                                if (DataMgr.oto.containsKey(args[2])) {
                                                    DataMgr.oto.put(args[2], DataMgr.oto.get(args[2])!! + 1)
                                                } else {
                                                    DataMgr.oto.put(args[2], 1)
                                                }
                                            }
                                        }

                                        if (DataMgr.oto.get(args[2]) ==
                                            getPlayerData(shooter)!!
                                                .getWeaponClass()
                                                .mainWeapon!!
                                                .rollerShootQuantity
                                        ) {
                                            shooter!!.playSound(
                                                shooter.getLocation(),
                                                Sound.ENTITY_ARROW_HIT_PLAYER,
                                                1.2f,
                                                1.3f,
                                            )
                                            shooter.spawnParticle(
                                                Particle.FLASH,
                                                projectile.getLocation(),
                                                1,
                                                0.1,
                                                0.1,
                                                0.1,
                                                0.1,
                                            )
                                        }
                                    }
                                }
                                shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                if (projectile.getCustomName()!!.contains("#slided")) {
                                    dmgDouble =
                                        getPlayerData(shooter)!!
                                            .getWeaponClass()
                                            .mainWeapon!!
                                            .decreaseRate
                                }
                                var damage = getPlayerData(shooter)!!.getWeaponClass().mainWeapon!!.damage
                                if (dmgDouble != 1.0) {
                                    damage = damage * dmgDouble
                                } else {
                                    damage = damage * Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                                }
                                val type =
                                    getPlayerData(shooter)!!
                                        .getWeaponClass()
                                        .mainWeapon!!
                                        .weaponType

                                if (type != "Blaster") {
                                    var ticksLived = projectile.getTicksLived().toDouble() * 1.2
                                    if (ticksLived > 20.0) ticksLived = 20.0
                                    damage -= damage * (ticksLived / 100)
                                }
                                if (type == "Funnel") {
                                    damage = damage + Funnel.FunnelPursuitPlayer(shooter, target)
                                    if (damage < 0.1) {
                                        damage = 0.1
                                    }
                                }

                                SclatUtil.giveDamage(shooter, target, damage, "killed")
                            }
                        }
                        // AntiNoDamageTime
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                var p: Player? = target

                                override fun run() {
                                    target.setNoDamageTicks(0)
                                }
                            }
                        task.runTaskLater(plugin, 1)

                        val timer = Timer(false)
                        val t: TimerTask =
                            object : TimerTask() {
                                var p: Player? = target

                                override fun run() {
                                    try {
                                        target.setNoDamageTicks(0)
                                        timer.cancel()
                                    } catch (e: Exception) {
                                        timer.cancel()
                                    }
                                }
                            }
                        timer.schedule(t, 25)
                    }
                } else if (event.getHitEntity() is ArmorStand) {
                    val `as` = event.getHitEntity() as ArmorStand?
                    var dmgDouble = 1.0
                    if (projectile.getCustomName() != null) {
                        if (DataMgr.mws.contains(projectile.getCustomName())) {
                            if (DataMgr.tsl.contains(projectile.getCustomName())) {
                                if (SclatUtil.isNumber(`as`!!.getCustomName()!!)) {
                                    if (`as`.getCustomName() != "21" && `as`.getCustomName() != "100") {
                                        if (`as`.isVisible()) {
                                            if (projectile.getCustomName()!!.contains("#slided")) {
                                                dmgDouble =
                                                    getPlayerData(shooter)!!
                                                        .getWeaponClass()
                                                        .mainWeapon!!
                                                        .decreaseRate
                                            } else if (!projectile.getCustomName()!!.contains(":")) {
                                                shooter!!.playSound(
                                                    shooter.getLocation(),
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                                shooter.spawnParticle(
                                                    Particle.FLASH,
                                                    projectile.getLocation(),
                                                    1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                )
                                            } else {
                                                val args: Array<String?>? =
                                                    projectile
                                                        .getCustomName()!!
                                                        .split(":".toRegex())
                                                        .dropLastWhile { it.isEmpty() }
                                                        .toTypedArray()
                                                when (args!![1]) {
                                                    "Burst" -> {
                                                        if (DataMgr.oto.containsKey(args[2])) {
                                                            DataMgr.oto.put(args[2], DataMgr.oto.get(args[2])!! + 1)
                                                        } else {
                                                            DataMgr.oto.put(args[2], 1)
                                                        }
                                                    }
                                                }

                                                if (DataMgr.oto.get(args[2]) ==
                                                    getPlayerData(shooter)!!
                                                        .getWeaponClass()
                                                        .mainWeapon!!
                                                        .rollerShootQuantity
                                                ) {
                                                    shooter!!.playSound(
                                                        shooter.getLocation(),
                                                        Sound.ENTITY_ARROW_HIT_PLAYER,
                                                        1.2f,
                                                        1.3f,
                                                    )
                                                    shooter.spawnParticle(
                                                        Particle.FLASH,
                                                        projectile.getLocation(),
                                                        1,
                                                        0.1,
                                                        0.1,
                                                        0.1,
                                                        0.1,
                                                    )
                                                }
                                            }
                                            // shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER,
                                            // 1.2F, 1.3F);
                                        }
                                    }
                                }
                            }
                        }

                        if (projectile.getCustomName() == "SuperShot") {
                            ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                            return
                        }
                        if (projectile.getCustomName()!!.contains("#QuadroArmsSpinner")) {
                            var Quadrodamage = 1.0
                            var QuadroticksLived = projectile.getTicksLived().toDouble() * 12.5
                            if (QuadroticksLived > 60) QuadroticksLived = 60.0
                            Quadrodamage += Quadrodamage * (QuadroticksLived / 30)
                            ArmorStandMgr.giveDamageArmorStand(`as`, Quadrodamage, shooter)
                            dmgDouble = 0.0
                        }
                        if (projectile.getCustomName()!!.contains("#QuadroArmsShotgun")) {
                            var Quadrodamage = 9.0
                            var QuadroticksLived = projectile.getTicksLived().toDouble() * 10
                            if (QuadroticksLived > 30) QuadroticksLived = 30.0
                            Quadrodamage -= Quadrodamage * (QuadroticksLived / 100)
                            if (projectile.getCustomName()!!.contains("CounterShot")) {
                                Quadrodamage = 6.5
                            }
                            ArmorStandMgr.giveDamageArmorStand(`as`, Quadrodamage, shooter)
                            dmgDouble = 0.0
                        }
                        if (projectile.getCustomName() == "JetPack") {
                            ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                            return
                        }
                    }
                    if (`as`!!.getCustomName() != null) {
                        if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") && (`as`.getCustomName() != "100") &&
                            (`as`.getCustomName() != "SplashShield") &&
                            (`as`.getCustomName() != "Kasa")
                        ) {
                            shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                        }
                    }
                    var damage = getPlayerData(shooter)!!.getWeaponClass().mainWeapon!!.damage
                    if (dmgDouble != 1.0) {
                        damage = damage * dmgDouble
                    } else {
                        damage = damage * Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                    }
                    val type = getPlayerData(shooter)!!.getWeaponClass().mainWeapon!!.weaponType

                    if (type != "Blaster") {
                        var ticksLived = projectile.getTicksLived().toDouble() * 1.2
                        if (ticksLived > 20.0) ticksLived = 20.0
                        damage -= damage * (ticksLived / 100)
                    }
                    if (type == "Blaster") {
                        try {
                            if (`as`.getCustomName() != null) {
                                if (`as`.getCustomName() == "Kasa") {
                                    val kasaData = getKasaDataFromArmorStand(`as` as ArmorStand)
                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        Blaster.Explode(shooter, `as`.getLocation().add(Vector(0.0, 1.0, 0.0)))
                                    }
                                } else if (`as`.getCustomName() == "SplashShield") {
                                    val splashShieldData = getSplashShieldDataFromArmorStand(`as` as ArmorStand)
                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        Blaster.Explode(shooter, `as`.getLocation().add(Vector(0.0, 1.0, 0.0)))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                    if (type == "Funnel") {
                        if (`as`.getCustomName() != null && (`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                            (`as`.getCustomName() != "100") &&
                            (`as`.getCustomName() != "SplashShield") &&
                            (`as`.getCustomName() != "Kasa")
                        ) {
                            damage = damage + Funnel.FunnelPursuit(shooter, `as`)
                            if (damage < 0.1) {
                                damage = 0.1
                            }
                        }
                    }
                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, shooter)
                }
            }
        }

        // Other
        if (snowballIsHitMap.containsKey(event.getEntity() as Snowball)) {
            if (event.getEntity().getCustomName() == null) {
                setSnowballIsHit(event.getEntity() as Snowball, true)
            } else {
                if (event.getHitBlock() != null) {
                    setSnowballIsHit(event.getEntity() as Snowball, true)
                }
                if (event.getHitEntity() != null) {
                    if (event.getEntity().getCustomName() == null) {
                        setSnowballIsHit(event.getEntity() as Snowball, true)
                    } else {
                        if (event.getEntity().getCustomName() == "JetPack") {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().getCustomName()!!.contains("#QuadroArmsSpinner")) {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().getCustomName()!!.contains("#QuadroArmsShotgun")) {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().getCustomName() == "SuperShot") return
                        val ball = event.getEntity() as Snowball
                        val vec = ball.getVelocity()
                        val loc = ball.getLocation()
                        val ball2 =
                            ball.getWorld().spawnEntity(
                                Location(
                                    loc.getWorld(),
                                    loc.getX() + vec.getX(),
                                    loc.getY() + vec.getY(),
                                    loc.getZ() + vec.getZ(),
                                ),
                                EntityType.SNOWBALL,
                            ) as Snowball
                        ball2.setVelocity(vec)
                        ball2.setCustomName(ball.getCustomName())
                        snowballNameMap.put(ball.getCustomName(), ball2)
                        setSnowballIsHit(ball2, false)
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            val connection = (o_player as CraftPlayer).getHandle().playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(ball2.getEntityId()))
                        }
                    }
                }
            }
            return
        }

        if (event.getEntity() is Snowball) {
            if (event.getHitEntity() != null) {
                if (event.getEntity().getCustomName() != null) {
                    if (mainSnowballNameMap.containsKey(event.getEntity().getCustomName())) {
                        if (event.getHitEntity() is ArmorStand) {
                            if (event.getHitEntity()!!.getCustomName() != null) {
                                if (event.getHitEntity()!!.getCustomName() == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(event.getHitEntity() as ArmorStand?)
                                    val ball = event.getEntity() as Snowball
                                    val shooter = ball.getShooter() as Player?
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(shooter).getTeam())
                                    // ssdata.setDamage(ssdata.damage +
                                    // DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().damage);
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        return
                                    }
                                    val vec = ball.getVelocity()
                                    val loc = ball.getLocation()
                                    val ball2 =
                                        ball.getWorld().spawnEntity(
                                            Location(
                                                loc.getWorld(),
                                                loc.getX() + vec.getX(),
                                                loc.getY() + vec.getY(),
                                                loc.getZ() + vec.getZ(),
                                            ),
                                            EntityType.SNOWBALL,
                                        ) as Snowball
                                    (ball2 as CraftSnowball).getHandle().setItem(
                                        CraftItemStack.asNMSCopy(
                                            ItemStack(getPlayerData(shooter)!!.team.teamColor!!.wool!!),
                                        ),
                                    )
                                    ball2.setShooter(shooter)
                                    ball2.setVelocity(vec)
                                    ball2.setCustomName(ball.getCustomName())
                                    // if(!DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().getWeaponType().equals("Blaster"))
                                    addSnowballHitCount(ball.getCustomName())
                                    mainSnowballNameMap.put(ball.getCustomName(), ball2)
                                }
                                if (event.getHitEntity()!!.getCustomName() == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(event.getHitEntity() as ArmorStand?)
                                    val ball = event.getEntity() as Snowball
                                    val shooter = ball.getShooter() as Player?
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(shooter).getTeam())
                                    // ssdata.setDamage(ssdata.damage +
                                    // DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().damage);
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        return
                                    }
                                    val vec = ball.getVelocity()
                                    val loc = ball.getLocation()
                                    val ball2 =
                                        ball.getWorld().spawnEntity(
                                            Location(
                                                loc.getWorld(),
                                                loc.getX() + vec.getX(),
                                                loc.getY() + vec.getY(),
                                                loc.getZ() + vec.getZ(),
                                            ),
                                            EntityType.SNOWBALL,
                                        ) as Snowball
                                    (ball2 as CraftSnowball).getHandle().setItem(
                                        CraftItemStack.asNMSCopy(
                                            ItemStack(getPlayerData(shooter)!!.team.teamColor!!.wool!!),
                                        ),
                                    )
                                    ball2.setShooter(shooter)
                                    ball2.setVelocity(vec)
                                    ball2.setCustomName(ball.getCustomName())
                                    // if(!DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().getWeaponType().equals("Blaster"))
                                    addSnowballHitCount(ball.getCustomName())
                                    mainSnowballNameMap.put(ball.getCustomName(), ball2)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (event.getEntity() is Snowball) {
            if (event.getHitBlock() != null) {
                val shooter = event.getEntity().getShooter() as Player?
                PaintMgr.Paint(event.getHitBlock()!!.getLocation(), shooter, true)
                shooter!!
                    .getWorld()
                    .playSound(event.getHitBlock()!!.getLocation(), Sound.ENTITY_SLIME_ATTACK, 0.3f, 2.0f)
            }
            if (event.getHitEntity() != null) {
                if (event.getHitEntity() is Player) {
                    // AntiNoDamageTime
                    val target = event.getHitEntity() as Player?
                    val task: BukkitRunnable =
                        object : BukkitRunnable() {
                            var p: Player? = target

                            override fun run() {
                                target!!.setNoDamageTicks(0)
                            }
                        }
                    task.runTaskLater(plugin, 1)

                    val timer = Timer(false)
                    val t: TimerTask =
                        object : TimerTask() {
                            var p: Player? = target

                            override fun run() {
                                try {
                                    target!!.setNoDamageTicks(0)
                                    timer.cancel()
                                } catch (e: Exception) {
                                    timer.cancel()
                                }
                            }
                        }
                    timer.schedule(t, 25)
                }
            }
        }
    }

    @EventHandler
    fun onEntityHit(event: EntityDamageByEntityEvent) {
        if (Sclat.type == ServerType.LOBBY) {
            if (event.getEntity() is Player) event.setCancelled(true)
            /*
             * if(event.getEntity() instanceof ArmorStand){ if(event.getDamager() instanceof
             * Player){ if(!((Player)event.getDamager()).hasPermission("sclat.admin"))
             * event.setCancelled(true); } }
             */
            return
        }

        event.setCancelled(true)

        if (event.getDamager() !is Projectile) return

        if (snowballIsHitMap.containsKey(event.getDamager() as Snowball)) {
            setSnowballIsHit(event.getDamager() as Snowball, true)
            if (event.getDamager().getCustomName() != null) {
                if (event.getDamager().getCustomName() == "JetPack" ||
                    event.getDamager().getCustomName() == "SuperShot"
                ) {
                    val projectile = event.getDamager() as Projectile
                    val shooter = projectile.getShooter() as Player?
                    if (event.getEntity() is Player) {
                        val target = event.getEntity() as Player
                        if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                            target.getGameMode() == GameMode.ADVENTURE
                        ) {
                            if (!getPlayerData(shooter)!!.getIsUsingSP()) SPWeaponMgr.addSPCharge(shooter)

                            giveDamage(shooter, target, 47.0, "spWeapon")
                        }
                    } else if (event.getEntity() is ArmorStand) {
                        val `as` = event.getEntity() as ArmorStand
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                    }
                }
            }
        } else {
            val projectile = event.getDamager() as Projectile
            val shooter = projectile.getShooter() as Player
            if (event.getEntity() is Player) {
                /*
//              * Player target = (Player)event.getEntity();
//              * if(DataMgr.getPlayerData(shooter).getTeam() !=
//              * DataMgr.getPlayerData(target).getTeam() &&
//              * target.getGameMode().equals(GameMode.ADVENTURE)){
//              * if(!DataMgr.getPlayerData(shooter).getIsUsingSP())
//              * SPWeaponMgr.addSPCharge(shooter);
//              * if(DataMgr.getPlayerData(target).getArmor() > 0){
//              * target.getWorld().playSound(target.getLocation(),
//              * Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1.5F);
//              * if(DataMgr.getPlayerData(target).getArmor() > 10000) { Vector
//              * vec = projectile.getVelocity(); Vector v = new
//              * Vector(vec.getX(), 0, vec.getZ()).normalize();
//              * target.setVelocity(new Vector(v.getX(), 0.2,
//              * v.getZ()).multiply(0.33)); } } if(projectile.getCustomName()
//              * != null){ if(projectile.getCustomName().equals("Sprinkler")
//              * || projectile.getCustomName().equals("Amehurasi")){
//              * if(projectile.getCustomName().equals("Sprinkler"))
//              * Sclat.giveDamage(shooter, target, 4, "subWeapon"); else
//              * if(projectile.getCustomName().equals("Amehurasi"))
//              * Sclat.giveDamage(shooter, target, 4, "spWeapon");
//              * PaintMgr.Paint(target.getLocation(), shooter, true); }
//              *
//              * if(projectile.getCustomName().equals("SuperShot")){
//              * shooter.playSound(shooter.getLocation(),
//              * Sound.ENTITY_PLAYER_HURT, 0.5F, 1F);
//              * Sclat.giveDamage(shooter, target, 20, "spWeapon"); }
//              *
//              * if(DataMgr.mws.contains(projectile.getCustomName())){
//              * if(DataMgr.tsl.contains(projectile.getCustomName())) {
//              * if(!projectile.getCustomName().contains(":")) {
//              * shooter.playSound(shooter.getLocation(),
//              * Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
//              * shooter.spawnParticle(Particle.FLASH,
//              * projectile.getLocation(), 1, 0.1, 0.1, 0.1, 0.1); }else{
//              * String args[] = projectile.getCustomName().split(":");
//              * switch(args[1]){ case "Burst": {
//              * if(DataMgr.oto.containsKey(args[2])){
//              * DataMgr.oto.put(args[2], DataMgr.oto.get(args[2]) + 1);
//              * }else{ DataMgr.oto.put(args[2], 1); } break; } }
//              *
//              * if(DataMgr.oto.get(args[2]) ==
//              * DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon
//              * ().getRollerShootQuantity()){
//              * shooter.playSound(shooter.getLocation(),
//              * Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
//              * shooter.spawnParticle(Particle.FLASH,
//              * projectile.getLocation(), 1, 0.1, 0.1, 0.1, 0.1); } } }
//              * shooter.playSound(shooter.getLocation(),
//              * Sound.ENTITY_PLAYER_HURT, 0.5F, 1F);
//              * Sclat.giveDamage(shooter, target,
//              * DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon
//              * ().damage, "killed"); } } //AntiNoDamageTime BukkitRunnable
//              * task = new BukkitRunnable(){ Player p = target;
//              *
//              * @Override public void run(){ target.setNoDamageTicks(0); } };
//              * task.runTaskLater(Main.getPlugin(), 1);
//              *
//              * Timer timer = new Timer(false); TimerTask t = new
//              * TimerTask(){ Player p = target;
//              *
//              * @Override public void run(){ try{ target.setNoDamageTicks(0);
//              * timer.cancel(); }catch(Exception e){ timer.cancel(); } } };
//              * timer.schedule(t, 25);
//              *
//              * }
//              */
            } else if (event.getEntity() is ArmorStand) {
                var dmgDouble = 1.0
                val `as` = event.getEntity() as ArmorStand
                if (projectile.getCustomName() != null) {
                    if (DataMgr.mws.contains(projectile.getCustomName())) {
                        if (DataMgr.tsl.contains(projectile.getCustomName())) {
                            if (SclatUtil.isNumber(`as`.getCustomName()!!)) {
                                if (`as`.getCustomName() != "21" && `as`.getCustomName() != "100") {
                                    if (`as`.isVisible()) {
                                        if (projectile.getCustomName()!!.contains("#slided")) {
                                            dmgDouble =
                                                getPlayerData(shooter)!!
                                                    .getWeaponClass()
                                                    .mainWeapon!!
                                                    .decreaseRate
                                        } else if (!projectile.getCustomName()!!.contains(":")) {
                                            shooter!!.playSound(
                                                shooter.getLocation(),
                                                Sound.ENTITY_ARROW_HIT_PLAYER,
                                                1.2f,
                                                1.3f,
                                            )
                                            shooter.spawnParticle(
                                                Particle.FLASH,
                                                projectile.getLocation(),
                                                1,
                                                0.1,
                                                0.1,
                                                0.1,
                                                0.1,
                                            )
                                        } else {
                                            val args: Array<String?>? =
                                                projectile
                                                    .getCustomName()!!
                                                    .split(":".toRegex())
                                                    .dropLastWhile { it.isEmpty() }
                                                    .toTypedArray()
                                            when (args!![1]) {
                                                "Burst" -> {
                                                    if (DataMgr.oto.containsKey(args[2])) {
                                                        DataMgr.oto.put(args[2], DataMgr.oto.get(args[2])!! + 1)
                                                    } else {
                                                        DataMgr.oto.put(args[2], 1)
                                                    }
                                                }
                                            }

                                            if (DataMgr.oto.get(args[2]) ==
                                                getPlayerData(shooter)!!
                                                    .getWeaponClass()
                                                    .mainWeapon!!
                                                    .rollerShootQuantity
                                            ) {
                                                shooter!!.playSound(
                                                    shooter.getLocation(),
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                                shooter.spawnParticle(
                                                    Particle.FLASH,
                                                    projectile.getLocation(),
                                                    1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                )
                                            }
                                        }
                                        // shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F,
                                        // 1.3F);
                                    }
                                }
                            }
                        }
                    }

                    if (projectile.getCustomName() == "SuperShot") {
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                        return
                    }
                    if (projectile.getCustomName() == "JetPack") {
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                        return
                    }
                    if (projectile.getCustomName()!!.contains("#QuadroArmsSpinner")) {
                        var Quadrodamage = 1.0
                        var QuadroticksLived = projectile.getTicksLived().toDouble() * 12.5
                        if (QuadroticksLived > 60) QuadroticksLived = 60.0
                        Quadrodamage += Quadrodamage * (QuadroticksLived / 30)
                        ArmorStandMgr.giveDamageArmorStand(`as`, Quadrodamage, shooter)
                        return
                    }
                    if (projectile.getCustomName()!!.contains("#QuadroArmsShotgun")) {
                        var Quadrodamage = 9.0
                        var QuadroticksLived = projectile.getTicksLived().toDouble() * 10
                        if (QuadroticksLived > 30) QuadroticksLived = 30.0
                        Quadrodamage -= Quadrodamage * (QuadroticksLived / 100)
                        if (projectile.getCustomName()!!.contains("CounterShot")) {
                            Quadrodamage = 6.5
                        }
                        ArmorStandMgr.giveDamageArmorStand(`as`, Quadrodamage, shooter)
                        return
                    }
                }
                if (`as`.getCustomName() != null) {
                    if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") && (`as`.getCustomName() != "100") &&
                        (`as`.getCustomName() != "SplashShield") &&
                        (`as`.getCustomName() != "Kasa")
                    ) {
                        shooter!!.playSound(shooter.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                    }
                }

                var damage = getPlayerData(shooter)!!.getWeaponClass().mainWeapon!!.damage
                if (dmgDouble != 1.0) {
                    damage = damage * dmgDouble
                } else {
                    damage = damage * Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                }
                val type = getPlayerData(shooter)!!.getWeaponClass().mainWeapon!!.weaponType

                if (type != "Burst" && type != "Blaster") {
                    var ticksLived = projectile.getTicksLived().toDouble() * 1.2
                    if (ticksLived > 20.0) ticksLived = 20.0
                    damage -= damage * (ticksLived / 100)
                }
                if (type == "Funnel") {
                    if (`as`.getCustomName() != null && (`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                        (`as`.getCustomName() != "100") &&
                        (`as`.getCustomName() != "SplashShield") &&
                        (`as`.getCustomName() != "Kasa")
                    ) {
                        damage = damage + Funnel.FunnelPursuit(shooter, `as`)
                        if (damage < 0.1) {
                            damage = 0.1
                        }
                    }
                }
                ArmorStandMgr.giveDamageArmorStand(`as`, damage, shooter)
            }
        }
    }
}
