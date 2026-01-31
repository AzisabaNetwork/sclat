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
        if (event.hitEntity != null) {
            if (snowballIsHitMap.containsKey(event.getEntity() as Snowball)) {
                setSnowballIsHit(event.getEntity() as Snowball, true)
                if (event.getEntity().customName != null) {
                    if (event.getEntity().customName == "JetPack" ||
                        event.getEntity().customName == "SuperShot"
                    ) {
                        val projectile = event.getEntity()
                        val shooter = projectile.shooter as Player?
                        if (event.hitEntity is Player) {
                            val target = event.hitEntity as Player?
                            if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                                target!!.gameMode == GameMode.ADVENTURE
                            ) {
                                if (!getPlayerData(shooter)!!.isUsingSP) SPWeaponMgr.addSPCharge(shooter)

                                giveDamage(shooter, target, 47.0, "spWeapon")
                            }
                        } else if (event.hitEntity is ArmorStand) {
                            val `as` = event.hitEntity as ArmorStand?
                            ArmorStandMgr.giveDamageArmorStand(`as`!!, 20.0, shooter!!)
                        }
                    }
                }
            } else {
                val projectile = event.getEntity()
                val shooter = projectile.shooter as Player
                if (event.hitEntity is Player) {
                    val target = event.hitEntity as Player
                    if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                        target.gameMode == GameMode.ADVENTURE
                    ) {
                        if (!getPlayerData(shooter)!!.isUsingSP) SPWeaponMgr.addSPCharge(shooter)
                        if (getPlayerData(target)!!.armor > 0) {
                            target.world.playSound(
                                target.location,
                                Sound.ENTITY_SPLASH_POTION_BREAK,
                                1f,
                                1.5f,
                            )
                            if (getPlayerData(target)!!.armor > 10000) {
                                val vec = projectile.velocity
                                val v = Vector(vec.getX(), 0.0, vec.getZ()).normalize()
                                target.velocity = Vector(v.getX(), 0.2, v.getZ()).multiply(0.33)
                            }
                        }
                        if (projectile.customName != null) {
                            if (projectile.customName == "Sprinkler" ||
                                projectile.customName == "Amehurasi"
                            ) {
                                if (projectile.customName == "Sprinkler") {
                                    giveDamage(
                                        shooter,
                                        target,
                                        4.0,
                                        "subWeapon",
                                    )
                                } else if (projectile.customName == "Amehurasi") {
                                    giveDamage(
                                        shooter,
                                        target,
                                        4.0,
                                        "spWeapon",
                                    )
                                }
                                PaintMgr.paint(target.location, shooter, true)
                            }

                            if (projectile.customName == "SuperShot") {
                                shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                giveDamage(shooter, target, 47.0, "spWeapon")
                            }
                            if (projectile.customName!!.contains("#QuadroArmsSpinner")) {
                                shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                var quadroDamage = 1.0
                                var quadroTicksLived = projectile.ticksLived.toDouble() * 12.5
                                if (quadroTicksLived > 60) quadroTicksLived = 60.0
                                quadroDamage += quadroDamage * (quadroTicksLived / 30)
                                giveDamage(shooter, target, quadroDamage, "spWeapon")
                                return
                            }
                            if (projectile.customName!!.contains("#QuadroArmsShotgun")) {
                                shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                var quadroDamage = 9.0
                                var quadroTicksLived = projectile.ticksLived.toDouble() * 10
                                if (quadroTicksLived > 30) quadroTicksLived = 30.0
                                quadroDamage -= quadroDamage * (quadroTicksLived / 100)
                                if (projectile.customName!!.contains("CounterShot")) {
                                    quadroDamage = 6.5
                                }
                                giveDamage(shooter, target, quadroDamage, "spWeapon")
                                return
                            }

                            if (DataMgr.mws.contains(projectile.customName)) {
                                var dmgDouble = 1.0
                                if (DataMgr.tsl.contains(projectile.customName)) {
                                    if (projectile.customName!!.contains("#slided")) {
                                        dmgDouble =
                                            getPlayerData(shooter)!!
                                                .weaponClass!!
                                                .mainWeapon!!
                                                .decreaseRate
                                    } else if (!projectile.customName!!.contains(":")) {
                                        shooter.playSound(
                                            shooter.location,
                                            Sound.ENTITY_ARROW_HIT_PLAYER,
                                            1.2f,
                                            1.3f,
                                        )
                                        shooter.spawnParticle(
                                            Particle.FLASH,
                                            projectile.location,
                                            1,
                                            0.1,
                                            0.1,
                                            0.1,
                                            0.1,
                                        )
                                    } else {
                                        val args: Array<String?> =
                                            projectile
                                                .customName!!
                                                .split(":".toRegex())
                                                .dropLastWhile { it.isEmpty() }
                                                .toTypedArray()
                                        when (args[1]) {
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
                                                .weaponClass!!
                                                .mainWeapon!!
                                                .rollerShootQuantity
                                        ) {
                                            shooter.playSound(
                                                shooter.location,
                                                Sound.ENTITY_ARROW_HIT_PLAYER,
                                                1.2f,
                                                1.3f,
                                            )
                                            shooter.spawnParticle(
                                                Particle.FLASH,
                                                projectile.location,
                                                1,
                                                0.1,
                                                0.1,
                                                0.1,
                                                0.1,
                                            )
                                        }
                                    }
                                }
                                shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                                if (projectile.customName!!.contains("#slided")) {
                                    dmgDouble =
                                        getPlayerData(shooter)!!
                                            .weaponClass!!
                                            .mainWeapon!!
                                            .decreaseRate
                                }
                                var damage = getPlayerData(shooter)!!.weaponClass!!.mainWeapon!!.damage
                                if (dmgDouble != 1.0) {
                                    damage = damage * dmgDouble
                                } else {
                                    damage = damage * Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                                }
                                val type =
                                    getPlayerData(shooter)!!
                                        .weaponClass!!
                                        .mainWeapon!!
                                        .weaponType

                                if (type != "Blaster") {
                                    var ticksLived = projectile.ticksLived.toDouble() * 1.2
                                    if (ticksLived > 20.0) ticksLived = 20.0
                                    damage -= damage * (ticksLived / 100)
                                }
                                if (type == "Funnel") {
                                    damage = damage + Funnel.funnelPursuitPlayer(shooter, target)
                                    if (damage < 0.1) {
                                        damage = 0.1
                                    }
                                }

                                giveDamage(shooter, target, damage, "killed")
                            }
                        }
                        // AntiNoDamageTime
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                var p: Player? = target

                                override fun run() {
                                    target.noDamageTicks = 0
                                }
                            }
                        task.runTaskLater(plugin, 1)

                        val timer = Timer(false)
                        val t: TimerTask =
                            object : TimerTask() {
                                var p: Player? = target

                                override fun run() {
                                    try {
                                        target.noDamageTicks = 0
                                        timer.cancel()
                                    } catch (e: Exception) {
                                        timer.cancel()
                                    }
                                }
                            }
                        timer.schedule(t, 25)
                    }
                } else if (event.hitEntity is ArmorStand) {
                    val armorStand = event.hitEntity as ArmorStand?
                    var dmgDouble = 1.0
                    if (projectile.customName != null) {
                        if (DataMgr.mws.contains(projectile.customName)) {
                            if (DataMgr.tsl.contains(projectile.customName)) {
                                if (SclatUtil.isNumber(armorStand!!.customName!!)) {
                                    if (armorStand.customName != "21" && armorStand.customName != "100") {
                                        if (armorStand.isVisible) {
                                            if (projectile.customName!!.contains("#slided")) {
                                                dmgDouble =
                                                    getPlayerData(shooter)!!
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .decreaseRate
                                            } else if (!projectile.customName!!.contains(":")) {
                                                shooter.playSound(
                                                    shooter.location,
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                                shooter.spawnParticle(
                                                    Particle.FLASH,
                                                    projectile.location,
                                                    1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                    0.1,
                                                )
                                            } else {
                                                val args: Array<String?> =
                                                    projectile
                                                        .customName!!
                                                        .split(":".toRegex())
                                                        .dropLastWhile { it.isEmpty() }
                                                        .toTypedArray()
                                                when (args[1]) {
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
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerShootQuantity
                                                ) {
                                                    shooter.playSound(
                                                        shooter.location,
                                                        Sound.ENTITY_ARROW_HIT_PLAYER,
                                                        1.2f,
                                                        1.3f,
                                                    )
                                                    shooter.spawnParticle(
                                                        Particle.FLASH,
                                                        projectile.location,
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

                        if (projectile.customName == "SuperShot") {
                            ArmorStandMgr.giveDamageArmorStand(armorStand!!, 20.0, shooter)
                            return
                        }
                        if (projectile.customName!!.contains("#QuadroArmsSpinner")) {
                            var quadroDamage = 1.0
                            var quadroTicksLived = projectile.ticksLived.toDouble() * 12.5
                            if (quadroTicksLived > 60) quadroTicksLived = 60.0
                            quadroDamage += quadroDamage * (quadroTicksLived / 30)
                            ArmorStandMgr.giveDamageArmorStand(armorStand!!, quadroDamage, shooter)
                            dmgDouble = 0.0
                        }
                        if (projectile.customName!!.contains("#QuadroArmsShotgun")) {
                            var quadroDamage = 9.0
                            var quadroTicksLived = projectile.ticksLived.toDouble() * 10
                            if (quadroTicksLived > 30) quadroTicksLived = 30.0
                            quadroDamage -= quadroDamage * (quadroTicksLived / 100)
                            if (projectile.customName!!.contains("CounterShot")) {
                                quadroDamage = 6.5
                            }
                            ArmorStandMgr.giveDamageArmorStand(armorStand!!, quadroDamage, shooter)
                            dmgDouble = 0.0
                        }
                        if (projectile.customName == "JetPack") {
                            ArmorStandMgr.giveDamageArmorStand(armorStand!!, 20.0, shooter)
                            return
                        }
                    }
                    if (armorStand!!.customName != null) {
                        if ((armorStand.customName != "Path") && (armorStand.customName != "21") && (armorStand.customName != "100") &&
                            (armorStand.customName != "SplashShield") &&
                            (armorStand.customName != "Kasa")
                        ) {
                            shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                        }
                    }
                    var damage = getPlayerData(shooter)!!.weaponClass!!.mainWeapon!!.damage
                    if (dmgDouble != 1.0) {
                        damage *= dmgDouble
                    } else {
                        damage *= Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                    }
                    val type = getPlayerData(shooter)!!.weaponClass!!.mainWeapon!!.weaponType

                    if (type != "Blaster") {
                        var ticksLived = projectile.ticksLived.toDouble() * 1.2
                        if (ticksLived > 20.0) ticksLived = 20.0
                        damage -= damage * (ticksLived / 100)
                    }
                    if (type == "Blaster") {
                        try {
                            if (armorStand.customName != null) {
                                if (armorStand.customName == "Kasa") {
                                    val kasaData = getKasaDataFromArmorStand(armorStand)
                                    if (getPlayerData(kasaData!!.player)!!.team !=
                                        getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        Blaster.Explode(shooter, armorStand.location.add(Vector(0.0, 1.0, 0.0)))
                                    }
                                } else if (armorStand.customName == "SplashShield") {
                                    val splashShieldData = getSplashShieldDataFromArmorStand(armorStand)
                                    if (getPlayerData(splashShieldData!!.player)!!.team !=
                                        getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        Blaster.Explode(shooter, armorStand.location.add(Vector(0.0, 1.0, 0.0)))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                    if (type == "Funnel") {
                        if (armorStand.customName != null && (armorStand.customName != "Path") && (armorStand.customName != "21") &&
                            (armorStand.customName != "100") &&
                            (armorStand.customName != "SplashShield") &&
                            (armorStand.customName != "Kasa")
                        ) {
                            damage = damage + Funnel.funnelPursuit(shooter, armorStand)
                            if (damage < 0.1) {
                                damage = 0.1
                            }
                        }
                    }
                    ArmorStandMgr.giveDamageArmorStand(armorStand, damage, shooter)
                }
            }
        }

        // Other
        if (snowballIsHitMap.containsKey(event.getEntity() as Snowball)) {
            if (event.getEntity().customName == null) {
                setSnowballIsHit(event.getEntity() as Snowball, true)
            } else {
                if (event.hitBlock != null) {
                    setSnowballIsHit(event.getEntity() as Snowball, true)
                }
                if (event.hitEntity != null) {
                    if (event.getEntity().customName == null) {
                        setSnowballIsHit(event.getEntity() as Snowball, true)
                    } else {
                        if (event.getEntity().customName == "JetPack") {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().customName!!.contains("#QuadroArmsSpinner")) {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().customName!!.contains("#QuadroArmsShotgun")) {
                            setSnowballIsHit(event.getEntity() as Snowball, true)
                            return
                        }
                        if (event.getEntity().customName == "SuperShot") return
                        val ball = event.getEntity() as Snowball
                        val vec = ball.velocity
                        val loc = ball.location
                        val ball2 =
                            ball.world.spawnEntity(
                                Location(
                                    loc.world,
                                    loc.x + vec.getX(),
                                    loc.y + vec.getY(),
                                    loc.z + vec.getZ(),
                                ),
                                EntityType.SNOWBALL,
                            ) as Snowball
                        ball2.velocity = vec
                        ball2.customName = ball.customName
                        snowballNameMap.put(ball.customName, ball2)
                        setSnowballIsHit(ball2, false)
                        for (o_player in plugin.server.onlinePlayers) {
                            val connection = (o_player as CraftPlayer).handle.playerConnection
                            connection.sendPacket(PacketPlayOutEntityDestroy(ball2.entityId))
                        }
                    }
                }
            }
            return
        }

        if (event.getEntity() is Snowball) {
            if (event.hitEntity != null) {
                if (event.getEntity().customName != null) {
                    if (mainSnowballNameMap.containsKey(event.getEntity().customName)) {
                        if (event.hitEntity is ArmorStand) {
                            if (event.hitEntity!!.customName != null) {
                                if (event.hitEntity!!.customName == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(event.hitEntity as ArmorStand?)
                                    val ball = event.getEntity() as Snowball
                                    val shooter = ball.shooter as Player?
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(shooter).getTeam())
                                    // ssdata.setDamage(ssdata.damage +
                                    // DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().damage);
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        return
                                    }
                                    val vec = ball.velocity
                                    val loc = ball.location
                                    val ball2 =
                                        ball.world.spawnEntity(
                                            Location(
                                                loc.world,
                                                loc.x + vec.getX(),
                                                loc.y + vec.getY(),
                                                loc.z + vec.getZ(),
                                            ),
                                            EntityType.SNOWBALL,
                                        ) as Snowball
                                    (ball2 as CraftSnowball).handle.setItem(
                                        CraftItemStack.asNMSCopy(
                                            ItemStack(getPlayerData(shooter)!!.team!!.teamColor!!.wool!!),
                                        ),
                                    )
                                    ball2.shooter = shooter
                                    ball2.velocity = vec
                                    ball2.customName = ball.customName
                                    // if(!DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().getWeaponType().equals("Blaster"))
                                    addSnowballHitCount(ball.customName)
                                    mainSnowballNameMap.put(ball.customName, ball2)
                                }
                                if (event.hitEntity!!.customName == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(event.hitEntity as ArmorStand?)
                                    val ball = event.getEntity() as Snowball
                                    val shooter = ball.shooter as Player?
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(shooter).getTeam())
                                    // ssdata.setDamage(ssdata.damage +
                                    // DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().damage);
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(shooter)!!
                                            .team
                                    ) {
                                        return
                                    }
                                    val vec = ball.velocity
                                    val loc = ball.location
                                    val ball2 =
                                        ball.world.spawnEntity(
                                            Location(
                                                loc.world,
                                                loc.x + vec.getX(),
                                                loc.y + vec.getY(),
                                                loc.z + vec.getZ(),
                                            ),
                                            EntityType.SNOWBALL,
                                        ) as Snowball
                                    (ball2 as CraftSnowball).handle.setItem(
                                        CraftItemStack.asNMSCopy(
                                            ItemStack(getPlayerData(shooter)!!.team!!.teamColor!!.wool!!),
                                        ),
                                    )
                                    ball2.shooter = shooter
                                    ball2.velocity = vec
                                    ball2.customName = ball.customName
                                    // if(!DataMgr.getPlayerData(shooter).getWeaponClass().getMainWeapon().getWeaponType().equals("Blaster"))
                                    addSnowballHitCount(ball.customName)
                                    mainSnowballNameMap.put(ball.customName, ball2)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (event.getEntity() is Snowball) {
            if (event.hitBlock != null) {
                val shooter = event.getEntity().shooter as Player?
                PaintMgr.paint(event.hitBlock!!.location, shooter, true)
                shooter!!
                    .world
                    .playSound(event.hitBlock!!.location, Sound.ENTITY_SLIME_ATTACK, 0.3f, 2.0f)
            }
            if (event.hitEntity != null) {
                if (event.hitEntity is Player) {
                    // AntiNoDamageTime
                    val target = event.hitEntity as Player?
                    val task: BukkitRunnable =
                        object : BukkitRunnable() {
                            var p: Player? = target

                            override fun run() {
                                target!!.noDamageTicks = 0
                            }
                        }
                    task.runTaskLater(plugin, 1)

                    val timer = Timer(false)
                    val t: TimerTask =
                        object : TimerTask() {
                            var p: Player? = target

                            override fun run() {
                                try {
                                    target!!.noDamageTicks = 0
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
            if (event.getEntity() is Player) event.isCancelled = true
            /*
             * if(event.getEntity() instanceof ArmorStand){ if(event.getDamager() instanceof
             * Player){ if(!((Player)event.getDamager()).hasPermission("sclat.admin"))
             * event.setCancelled(true); } }
             */
            return
        }

        event.isCancelled = true

        if (event.damager !is Projectile) return

        if (snowballIsHitMap.containsKey(event.damager as Snowball)) {
            setSnowballIsHit(event.damager as Snowball, true)
            if (event.damager.customName != null) {
                if (event.damager.customName == "JetPack" ||
                    event.damager.customName == "SuperShot"
                ) {
                    val projectile = event.damager as Projectile
                    val shooter = projectile.shooter as Player?
                    if (event.getEntity() is Player) {
                        val target = event.getEntity() as Player
                        if (getPlayerData(shooter)!!.team != getPlayerData(target)!!.team &&
                            target.gameMode == GameMode.ADVENTURE
                        ) {
                            if (!getPlayerData(shooter)!!.isUsingSP) SPWeaponMgr.addSPCharge(shooter)

                            giveDamage(shooter, target, 47.0, "spWeapon")
                        }
                    } else if (event.getEntity() is ArmorStand) {
                        val `as` = event.getEntity() as ArmorStand
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter!!)
                    }
                }
            }
        } else {
            val projectile = event.damager as Projectile
            val shooter = projectile.shooter as Player
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
                if (projectile.customName != null) {
                    if (DataMgr.mws.contains(projectile.customName)) {
                        if (DataMgr.tsl.contains(projectile.customName)) {
                            if (SclatUtil.isNumber(`as`.customName!!)) {
                                if (`as`.customName != "21" && `as`.customName != "100") {
                                    if (`as`.isVisible) {
                                        if (projectile.customName!!.contains("#slided")) {
                                            dmgDouble =
                                                getPlayerData(shooter)!!
                                                    .weaponClass!!
                                                    .mainWeapon!!
                                                    .decreaseRate
                                        } else if (!projectile.customName!!.contains(":")) {
                                            shooter.playSound(
                                                shooter.location,
                                                Sound.ENTITY_ARROW_HIT_PLAYER,
                                                1.2f,
                                                1.3f,
                                            )
                                            shooter.spawnParticle(
                                                Particle.FLASH,
                                                projectile.location,
                                                1,
                                                0.1,
                                                0.1,
                                                0.1,
                                                0.1,
                                            )
                                        } else {
                                            val args: Array<String?> =
                                                projectile
                                                    .customName!!
                                                    .split(":".toRegex())
                                                    .dropLastWhile { it.isEmpty() }
                                                    .toTypedArray()
                                            when (args[1]) {
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
                                                    .weaponClass!!
                                                    .mainWeapon!!
                                                    .rollerShootQuantity
                                            ) {
                                                shooter.playSound(
                                                    shooter.location,
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                                shooter.spawnParticle(
                                                    Particle.FLASH,
                                                    projectile.location,
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

                    if (projectile.customName == "SuperShot") {
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                        return
                    }
                    if (projectile.customName == "JetPack") {
                        ArmorStandMgr.giveDamageArmorStand(`as`, 20.0, shooter)
                        return
                    }
                    if (projectile.customName!!.contains("#QuadroArmsSpinner")) {
                        var quadroDamage = 1.0
                        var quadroTicksLived = projectile.ticksLived.toDouble() * 12.5
                        if (quadroTicksLived > 60) quadroTicksLived = 60.0
                        quadroDamage += quadroDamage * (quadroTicksLived / 30)
                        ArmorStandMgr.giveDamageArmorStand(`as`, quadroDamage, shooter)
                        return
                    }
                    if (projectile.customName!!.contains("#QuadroArmsShotgun")) {
                        var quadroDamage = 9.0
                        var quadroTicksLived = projectile.ticksLived.toDouble() * 10
                        if (quadroTicksLived > 30) quadroTicksLived = 30.0
                        quadroDamage -= quadroDamage * (quadroTicksLived / 100)
                        if (projectile.customName!!.contains("CounterShot")) {
                            quadroDamage = 6.5
                        }
                        ArmorStandMgr.giveDamageArmorStand(`as`, quadroDamage, shooter)
                        return
                    }
                }
                if (`as`.customName != null) {
                    if ((`as`.customName != "Path") && (`as`.customName != "21") && (`as`.customName != "100") &&
                        (`as`.customName != "SplashShield") &&
                        (`as`.customName != "Kasa")
                    ) {
                        shooter.playSound(shooter.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1f)
                    }
                }

                var damage = getPlayerData(shooter)!!.weaponClass!!.mainWeapon!!.damage
                if (dmgDouble != 1.0) {
                    damage = damage * dmgDouble
                } else {
                    damage = damage * Gear.getGearInfluence(shooter, Gear.Type.MAIN_SPEC_UP)
                }
                val type = getPlayerData(shooter)!!.weaponClass!!.mainWeapon!!.weaponType

                if (type != "Burst" && type != "Blaster") {
                    var ticksLived = projectile.ticksLived.toDouble() * 1.2
                    if (ticksLived > 20.0) ticksLived = 20.0
                    damage -= damage * (ticksLived / 100)
                }
                if (type == "Funnel") {
                    if (`as`.customName != null && (`as`.customName != "Path") && (`as`.customName != "21") &&
                        (`as`.customName != "100") &&
                        (`as`.customName != "SplashShield") &&
                        (`as`.customName != "Kasa")
                    ) {
                        damage = damage + Funnel.funnelPursuit(shooter, `as`)
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
