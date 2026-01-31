package be4rjp.sclat.weapon

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
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
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Brush {
    @JvmStatic
    fun HoldRunnable(player: Player) {
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

                    val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)

                    if ( /* data.getTick() >= 6 */clickType == ClickType.NO_CLICK && data.isInMatch) {
                        data.tick = 7
                        data.setIsHolding(false)
                        data.canPaint = false
                        data.canShoot = true
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    @JvmStatic
    fun RollPaintRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    try {
                        val data = getPlayerData(p)
                        if (!data!!.isInMatch || !p.isOnline) cancel()

                        if (data.getIsHolding() && data.canPaint && data.isInMatch &&
                            Sclat.dadadaCheckerAPI!!.getPlayerClickType(p) != ClickType.RENDA && p.gameMode != GameMode.SPECTATOR
                        ) {
                            if (player.exp <=
                                (
                                    data.weaponClass.mainWeapon!!.rollerNeedInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                                    ).toFloat()
                            ) {
                                player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
                                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                                return
                            }
                            p.exp = p.exp -
                                (
                                    data.weaponClass.mainWeapon!!.rollerNeedInk
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
                            var front = eloc.add(vec.getX() * 2, -0.9, vec.getZ() * 2)
                            if (data.weaponClass.mainWeapon!!.isHude) {
                                front =
                                    eloc.add(vec.getX() * 1.5, -0.9, vec.getZ() * 1.5)
                            }
                            val bd =
                                getPlayerData(p)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (target in plugin.server.onlinePlayers) {
                                if (getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) {
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
                            Vector(vec.getZ() * -1, 0.0, vec.getX())
                            Vector(vec.getZ(), 0.0, vec.getX() * -1)

                            // 筆系武器
                            if (data.weaponClass.mainWeapon!!.isHude) {
                                val position = p.location
                                PaintMgr.PaintHightestBlock(front, p, false, true)
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
                                    if (getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) {
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
                                    if (getPlayerData(p)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        if (target.location.distanceSquared(position) <= maxDistSquad) {
                                            val damage =
                                                getPlayerData(p)!!
                                                    .weaponClass
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
                                                        .weaponClass
                                                        .mainWeapon!!
                                                        .rollerDamage
                                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                            }
                                        }
                                    }
                                }
                                p.walkSpeed =
                                    (
                                        data.weaponClass.mainWeapon!!.usingWalkSpeed
                                            * Gear.getGearInfluence(p, Gear.Type.MAIN_SPEC_UP)
                                        ).toFloat()
                                return
                            }
                            PaintMgr.PaintHightestBlock(eloc, p, false, true)
                            p.walkSpeed =
                                (
                                    data.weaponClass.mainWeapon!!.usingWalkSpeed
                                        * Gear.getGearInfluence(p, Gear.Type.MAIN_SPEC_UP)
                                    ).toFloat()
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        if (getPlayerData(player)!!.weaponClass.mainWeapon!!.isHude) {
            task.runTaskTimer(plugin, 0, 1)
        } else {
            task.runTaskTimer(plugin, 0, 5)
        }
    }

    @JvmStatic
    fun ShootPaintRunnable(player: Player) {
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
                    if (p.gameMode != GameMode.ADVENTURE || p
                            .inventory
                            .itemInMainHand
                            .type == Material.AIR
                    ) {
                        return
                    }
                    if (player.exp >= data!!.weaponClass.mainWeapon!!.needInk) {
                        p
                            .world
                            .playSound(p.location, Sound.ITEM_BUCKET_EMPTY, 1f, 1f)
                    } else {
                        return
                    }
                    val vec =
                        player
                            .location
                            .direction
                            .multiply(getPlayerData(player)!!.weaponClass.mainWeapon!!.shootSpeed)
                    val random = data!!.weaponClass.mainWeapon!!.hudeRandom
                    vec.add(
                        Vector(
                            Math.random() * random - random / 2,
                            Math.random() * random / 4 - random / 8,
                            Math.random() * random - random / 2,
                        ),
                    )
                    for (i in 0..<data!!.weaponClass.mainWeapon!!.rollerShootQuantity) {
                        if (data!!.weaponClass.mainWeapon!!.isHude) {
                            Shoot(p, vec)
                        } else {
                            Shoot(p, null)
                        }
                    }
                    // ShootRunnable(p);
                    data!!.canPaint = true
                }
            }
        if (pdata!!.canRollerShoot) {
            task.runTaskLater(
                plugin,
                pdata
                    .weaponClass
                    .mainWeapon!!
                    .shootTick
                    .toLong(),
            )
            pdata.canRollerShoot = false
        }
    }

    fun ShootRunnable(player: Player?) {
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
                .weaponClass
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun Shoot(
        player: Player,
        v: Vector?,
    ) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 13, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.exp = player.exp -
            (
                data.weaponClass.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).handle.item = CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!))
        var vec: Vector? =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass.mainWeapon!!.shootSpeed)
        if (v != null) vec = v
        val random = getPlayerData(player)!!.weaponClass.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass.mainWeapon!!.distanceTick
        if (!data.weaponClass.mainWeapon!!.isHude) {
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
                if (data.weaponClass.mainWeapon!!.canTatehuri) {
                    vec!!.add(
                        Vector(
                            Math.random() * random / 4 - random / 8,
                            Math.random() * random,
                            Math.random() * random / 4 - random / 8,
                        ),
                    )
                }
                if (!data.weaponClass.mainWeapon!!.canTatehuri) {
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
        mainSnowballNameMap.put(name, ball)
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
                    ).multiply(getPlayerData(p)!!.weaponClass.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }
                    if (i != 0) {
                        for (target in plugin.server.onlinePlayers) {
                            if (target.world !== p.world) continue
                            if (!getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) continue
                            val bd =
                                getPlayerData(p)!!
                                    .team.teamColor!!
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
                    if (i != tick) PaintMgr.PaintHightestBlock(inkball!!.location, p, true, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
