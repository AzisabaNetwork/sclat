package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Roller {
    @JvmStatic
    fun holdRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(p)

                    data!!.tick = data.tick + 1

                    if (!data.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    if (data.tick >= 6 && data.isInMatch) {
                        data.tick = 7
                        data.isHolding = false
                        data.canPaint = false
                        data.canShoot = true
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    @JvmStatic
    fun rollPaintRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    try {
                        val data = getPlayerData(p)
                        if (!data!!.isInMatch || !p.isOnline) cancel()

                        if (data.isHolding && data.canPaint && data.isInMatch) {
                            if (player.exp <=
                                (
                                    data.weaponClass?.mainWeapon!!.rollerNeedInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                                ).toFloat()
                            ) {
                                player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
                                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                                return
                            }
                            p.exp -=
                                (
                                    data.weaponClass?.mainWeapon!!.rollerNeedInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                                ).toFloat()
                            val locvec = p.eyeLocation.direction
                            val eloc = p.eyeLocation
                            val vec = Vector(locvec.getX(), 0.0, locvec.getZ()).normalize()
                            // RayTrace rayTrace1 = new RayTrace(front.toVector(), vec1);
                            // ArrayList<Vector> positions1 =
                            // rayTrace1.traverse(data.getWeaponClass().getMainWeapon().rollerWidth,
                            // 0.5);
                            var front = eloc.add(vec.getX() * 2.5, -0.9, vec.getZ() * 2.5)
                            if (data.weaponClass?.mainWeapon!!.isHude) {
                                front =
                                    eloc.add(vec.getX() * 1.5, -0.9, vec.getZ() * 1.5)
                            }
                            val bd =
                                getPlayerData(p)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.server.onlinePlayers) {
                                if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
                                    if (target.world ===
                                        p.world
                                    ) {
                                        if (target
                                                .location
                                                .distanceSquared(front) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            target.spawnParticle<BlockData?>(
                                                Particle.BLOCK_DUST,
                                                front,
                                                2,
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
                            val vec1 = Vector(vec.getZ() * -1, 0.0, vec.getX())
                            val vec2 = Vector(vec.getZ(), 0.0, vec.getX() * -1)

                            // 筆系武器
                            if (data.weaponClass?.mainWeapon!!.isHude) {
                                val position = p.location
                                PaintMgr.paintHightestBlock(front, p, false, true)
                                p.location.world!!.spawnParticle<BlockData?>(
                                    Particle.BLOCK_DUST,
                                    position,
                                    2,
                                    0.0,
                                    0.0,
                                    0.0,
                                    1.0,
                                    bd,
                                )

                                for (target in plugin.server.onlinePlayers) {
                                    if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
                                        if (target.world ===
                                            p.world
                                        ) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                target.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    position,
                                                    2,
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

                                val maxDistSquad = 4.0 // 2*2
                                for (target in plugin.server.onlinePlayers) {
                                    if (!getPlayerData(target)!!.isInMatch) continue
                                    if (target.location.distanceSquared(position) <= maxDistSquad) {
                                        if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                            target.gameMode == GameMode.ADVENTURE
                                        ) {
                                            val damage =
                                                getPlayerData(p)!!
                                                    .weaponClass!!
                                                    .mainWeapon!!
                                                    .rollerDamage

                                            giveDamage(p, target, damage, "killed")
                                        }
                                    }
                                }

                                for (`as` in player.world.entities) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                                                val damage =
                                                    getPlayerData(p)!!
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerDamage
                                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                            }
                                        }
                                    }
                                }
                                p.walkSpeed =
                                    (
                                        data.weaponClass?.mainWeapon!!.usingWalkSpeed
                                            * Gear.getGearInfluence(p, Gear.Type.MAIN_SPEC_UP)
                                    ).toFloat()
                                return
                            }

                            // 法線ベクトルでロール部分の取得
                            val rayTrace1 = RayTrace(front.toVector(), vec1)
                            val positions1 =
                                rayTrace1
                                    .traverse(
                                        data
                                            .weaponClass!!
                                            .mainWeapon!!
                                            .rollerWidth
                                            .toDouble(),
                                        0.5,
                                    )
                            loop@ for (vector in positions1) {
                                val position = vector.toLocation(p.location.world!!)
                                val block = p.location.world!!.getBlockAt(position)
                                if (block.type != Material.AIR) break
                                PaintMgr.paintHightestBlock(position, p, false, true)
                                p.location.world!!.spawnParticle<BlockData?>(
                                    Particle.BLOCK_DUST,
                                    position,
                                    2,
                                    0.0,
                                    0.0,
                                    0.0,
                                    1.0,
                                    bd,
                                )

                                for (target in plugin.server.onlinePlayers) {
                                    if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
                                        if (target.world ===
                                            p.world
                                        ) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                target.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    position,
                                                    2,
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

                                val maxDistSquad = 4.0 // 2*2

                                for (`as` in p.world.entities) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                                                try {
                                                    if (`as`.customName == "Kasa") {
                                                        val kasaData = getKasaDataFromArmorStand(`as`)
                                                        if (getPlayerData(kasaData!!.player)!!.team !=
                                                            getPlayerData(p)!!
                                                                .team
                                                        ) {
                                                            break@loop
                                                        }
                                                    } else if (`as`.customName == "SplashShield") {
                                                        val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                        if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                            getPlayerData(p)!!
                                                                .team
                                                        ) {
                                                            break@loop
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                }
                                            }
                                        }
                                    }
                                }

                                for (target in plugin.server.onlinePlayers) {
                                    if (!getPlayerData(target)!!.isInMatch) continue
                                    if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        if (target.location.distanceSquared(position) <= maxDistSquad) {
                                            if (rayTrace1.intersects(
                                                    BoundingBox(target as Entity),
                                                    data
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerWidth
                                                        .toDouble(),
                                                    0.05,
                                                )
                                            ) {
                                                val damage =
                                                    getPlayerData(p)!!
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerDamage

                                                giveDamage(p, target, damage, "killed")

                                                // AntiNoDamageTime
                                                val task: BukkitRunnable =
                                                    object : BukkitRunnable() {
                                                        var p: Player = target

                                                        override fun run() {
                                                            target.noDamageTicks = 0
                                                        }
                                                    }
                                                task.runTaskLater(plugin, 1)
                                                break@loop
                                            }
                                        }
                                    }
                                }

                                for (`as` in player.world.entities) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                                                val damage =
                                                    getPlayerData(p)!!
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerDamage
                                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                            }
                                        }
                                    }
                                }
                            }

                            val rayTrace2 = RayTrace(front.toVector(), vec2)
                            val positions2 =
                                rayTrace2
                                    .traverse(
                                        data
                                            .weaponClass!!
                                            .mainWeapon!!
                                            .rollerWidth
                                            .toDouble(),
                                        0.5,
                                    )
                            loop@ for (vector in positions2) {
                                val position = vector.toLocation(p.location.world!!)
                                val block = p.location.world!!.getBlockAt(position)
                                if (block.type != Material.AIR) break
                                PaintMgr.paintHightestBlock(position, p, false, true)
                                for (target in plugin.server.onlinePlayers) {
                                    if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
                                        if (target.world ===
                                            p.world
                                        ) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                target.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    position,
                                                    2,
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

                                val maxDistSquad = 4.0 // 2*2

                                for (`as` in p.world.entities) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.customName != null) {
                                            if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                                                try {
                                                    if (`as`.customName == "Kasa") {
                                                        val kasaData = getKasaDataFromArmorStand(`as`)
                                                        if (getPlayerData(kasaData!!.player)!!.team !=
                                                            getPlayerData(p)!!
                                                                .team
                                                        ) {
                                                            break@loop
                                                        }
                                                    } else if (`as`.customName == "SplashShield") {
                                                        val splashShieldData = getSplashShieldDataFromArmorStand(`as`)
                                                        if (getPlayerData(splashShieldData!!.player)!!.team !=
                                                            getPlayerData(p)!!
                                                                .team
                                                        ) {
                                                            break@loop
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                }
                                            }
                                        }
                                    }
                                }

                                for (target in plugin.server.onlinePlayers) {
                                    if (!getPlayerData(target)!!.isInMatch) continue
                                    if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        if (rayTrace1.intersects(
                                                BoundingBox(target as Entity),
                                                data
                                                    .weaponClass!!
                                                    .mainWeapon!!
                                                    .rollerWidth
                                                    .toDouble(),
                                                0.05,
                                            )
                                        ) {
                                            if (target.location.distanceSquared(position) <= maxDistSquad) {
                                                val damage =
                                                    getPlayerData(p)!!
                                                        .weaponClass!!
                                                        .mainWeapon!!
                                                        .rollerDamage

                                                giveDamage(p, target, damage, "killed")

                                                // AntiNoDamageTime
                                                val task: BukkitRunnable =
                                                    object : BukkitRunnable() {
                                                        var p: Player = target

                                                        override fun run() {
                                                            target.noDamageTicks = 0
                                                        }
                                                    }
                                                task.runTaskLater(plugin, 1)
                                                break@loop
                                            }
                                        }
                                    }
                                }

                                for (`as` in player.world.entities) {
                                    if (`as` is ArmorStand) {
                                        if (`as`.location.distance(position) <= maxDistSquad) {
                                            val damage =
                                                getPlayerData(p)!!
                                                    .weaponClass!!
                                                    .mainWeapon!!
                                                    .rollerDamage
                                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                        }
                                    }
                                }
                            }
                            PaintMgr.paintHightestBlock(eloc, p, false, true)
                            p.walkSpeed =
                                (
                                    data.weaponClass?.mainWeapon!!.usingWalkSpeed
                                        * Gear.getGearInfluence(p, Gear.Type.MAIN_SPEC_UP)
                                ).toFloat()
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        if (getPlayerData(player)!!.weaponClass!!.mainWeapon!!.isHude) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            task.runTaskTimer(plugin, 0, 5)
        }
    }

    @JvmStatic
    fun shootPaintRunnable(player: Player) {
        val pdata = getPlayerData(player)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = pdata

                override fun run() {
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }
                    data!!.canRollerShoot = true
                    if (p.gameMode != GameMode.ADVENTURE ||
                        p
                            .inventory
                            .itemInMainHand
                            .type == Material.AIR
                    ) {
                        return
                    }
                    if (p.exp >= data!!.weaponClass!!.mainWeapon!!.needInk) {
                        p
                            .world
                            .playSound(p.location, Sound.ITEM_BUCKET_EMPTY, 1f, 1f)
                    } else {
                        return
                    }
                    val vec =
                        p
                            .location
                            .direction
                            .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.shootSpeed)
                    val random = data!!.weaponClass!!.mainWeapon!!.hudeRandom
                    vec.add(
                        Vector(
                            Math.random() * random - random / 2,
                            Math.random() * random / 4 - random / 8,
                            Math.random() * random - random / 2,
                        ),
                    )

                    var sound = false
                    for (i in 0..<data!!.weaponClass!!.mainWeapon!!.rollerShootQuantity) {
                        val `is`: Boolean =
                            if (data!!.weaponClass!!.mainWeapon!!.isHude) {
                                shoot(p, vec)
                            } else {
                                shoot(p, null)
                            }
                        if (`is`) sound = true
                    }
                    if (sound) {
                        p.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                    }
                    // ShootRunnable(p);
                    data!!.canPaint = true
                }
            }
        if (pdata!!.canRollerShoot) {
            task.runTaskLater(
                plugin,
                pdata
                    .weaponClass!!
                    .mainWeapon!!
                    .shootTick
                    .toLong(),
            )
            pdata.canRollerShoot = false
        }
    }

    fun shootRunnable(player: Player?) {
        val data = getPlayerData(player)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    data!!.canRollerShoot = true
                }
            }
        task.runTaskLater(
            plugin,
            data!!
                .weaponClass!!
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun shoot(
        player: Player,
        v: Vector?,
    ): Boolean {
        if (player.gameMode == GameMode.SPECTATOR) return false

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass!!.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
            return true
        }
        player.exp -=
            (
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        var vec: Vector? =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.shootSpeed)
        if (v != null) vec = v
        val random = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.distanceTick
        if (!data.weaponClass?.mainWeapon!!.isHude) {
            if (player.isOnGround) {
                vec!!.add(
                    Vector(
                        Math.random() * random - random / 2,
                        Math.random() * random / 4 - random / 8,
                        Math.random() * random - random / 2,
                    ),
                )
            }
            if (!player.isOnGround) {
                if (data.weaponClass?.mainWeapon!!.canTatehuri) {
                    vec!!.add(
                        Vector(
                            Math.random() * random / 4 - random / 8,
                            Math.random() * random * 1.8 - random * 0.8,
                            Math.random() * random / 4 - random / 8,
                        ),
                    )
                }
                if (!data.weaponClass?.mainWeapon!!.canTatehuri) {
                    vec!!.add(
                        Vector(
                            Math.random() * random - random / 2,
                            Math.random() * random / 4 - random / 8,
                            Math.random() * random - random / 2,
                        ),
                    )
                }
                // player.sendMessage(String.valueOf(player.isOnGround()));
            }
        } else {
            vec!!.add(
                Vector(
                    Math.random() * random - random / 2,
                    Math.random() * random / 4 - random / 8,
                    Math.random() * random - random / 2,
                ),
            )
        }
        ball.velocity = vec!!
        ball.shooter = player
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        ball.customName = name
        mainSnowballNameMap[name] = ball
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick
                var inkball: Snowball? = ball
                var p: Player = player
                var addedFallVec: Boolean = false
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }
                    if (i != 0) {
                        for (target in plugin.server.onlinePlayers) {
                            if (target.world !== p.world) continue
                            if (!getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) continue
                            val bd =
                                getPlayerData(p)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            target.spawnParticle<BlockData?>(
                                Particle.BLOCK_DUST,
                                inkball!!.location,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                1.0,
                                bd,
                            )
                        }
                    }

                    if (i >= tick && !addedFallVec) {
                        inkball!!.velocity = fallvec
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.velocity = inkball!!.velocity.add(Vector(0.0, -0.1, 0.0))
                    }
                    if (i != tick) PaintMgr.paintHightestBlock(inkball!!.location, p, true, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)

        return false
    }
}
