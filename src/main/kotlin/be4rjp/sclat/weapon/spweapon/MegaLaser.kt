package be4rjp.sclat.weapon.spweapon

import be4rjp.blockstudio.BlockStudio
import be4rjp.blockstudio.api.BSObject
import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.async.AsyncTask
import be4rjp.sclat.api.async.AsyncThreadManager
import be4rjp.sclat.api.async.AsyncThreadManager.sync
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object MegaLaser {
    @JvmStatic
    fun megaLaserRunnable(player: Player) {
        val api = BlockStudio.getBlockStudioAPI()
        val objectData = api.getObjectData("mega")
        val bsObject = api.createObjectFromObjectData("mega", player.location, objectData, false)
        bsObject.startTaskAsync(40)
        bsObject.move()

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    if (c == 0) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100000, 2))
                        p.inventory.clear()
                        p.inventory.clear()
                        player.updateInventory()
                        getPlayerData(p)!!.isUsingSP = true
                        val item = ItemStack(Material.SHULKER_SHELL)
                        val meta = item.itemMeta
                        meta!!.setDisplayName("狙って右クリックで発射")
                        item.itemMeta = meta
                        for (count in 0..8) {
                            player.inventory.setItem(count, item)
                        }
                        player.updateInventory()

                        getPlayerData(p)!!.isUsingMM = true
                    }

                    val direction = player.eyeLocation.direction
                    var xz = Vector(direction.getX(), 0.0, direction.getZ())
                    if (xz.lengthSquared() == 0.0) xz = Vector(1, 0, 1)
                    val normXZ = xz.normalize()

                    val objectLoc = player.location.add(normXZ.getX(), 0.6, normXZ.getZ())
                    bsObject.baseLocation = objectLoc
                    bsObject.setDirection(player.eyeLocation.direction)
                    bsObject.move()

                    if (!getPlayerData(p)!!.isUsingMM || c == 400) {
                        getPlayerData(p)!!.isUsingMM = false
                        megaLaserShootRunnable(player, bsObject)
                        WeaponClassMgr.setWeaponClass(p)
                        if (p.hasPotionEffect(PotionEffectType.SLOW)) p.removePotionEffect(PotionEffectType.SLOW)
                        cancel()
                    }

                    if (!p.isOnline || !getPlayerData(p)!!.isInMatch || p.gameMode == GameMode.SPECTATOR) {
                        if (p.hasPotionEffect(PotionEffectType.SLOW)) p.removePotionEffect(PotionEffectType.SLOW)
                        getPlayerData(p)!!.isUsingMM = false
                        bsObject.remove()
                        cancel()
                    }

                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun playSound(
        targetLoc: Location,
        sound: Sound,
        v: Float,
        p: Float,
    ) {
        for (target in AsyncThreadManager.onlinePlayers) {
            val loc = target!!.location
            if (loc.world === targetLoc.world) {
                if (loc.distanceSquared(targetLoc) < 500) {
                    target.playSound(targetLoc, sound, v, p)
                }
            }
        }
    }

    fun megaLaserShootRunnable(
        player: Player,
        bsObject: BSObject,
    ) {
        val direction = player.eyeLocation.direction.normalize()
        var xz = Vector(direction.getX(), 0.0, direction.getZ())
        if (xz.lengthSquared() == 0.0) xz = Vector(1, 0, 1)
        val normXZ = xz.normalize()
        val objectLoc = player.location.add(normXZ.getX(), 0.6, normXZ.getZ())

        val rayTrace = RayTrace(objectLoc.toVector(), direction)
        val positions: ArrayList<Vector> = rayTrace.traverse(300.0, 1.0)

        SPWeaponMgr.setSPCoolTimeAnimation(player, 130)

        val playerData = getPlayerData(player)

        val task: AsyncTask =
            object : AsyncTask() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    // 終了処理

                    if (c == 13 || !playerData!!.isInMatch || !p.isOnline) {
                        playerData!!.isUsingSP = false
                        for (target in AsyncThreadManager.onlinePlayers) {
                            SclatUtil.sendWorldBorderWarningClearPacket(target!!)
                        }
                        bsObject.remove()
                        cancel()
                    }

                    // 音
                    if (c <= 3) {
                        playSound(objectLoc, Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.5f)
                    } else {
                        playSound(objectLoc, Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.6f)
                    }

                    val xzVector = Vector(direction.getX(), 0.0, direction.getZ())
                    val xzAngle = xzVector.angle(Vector(0, 0, 1)) * (if (direction.getX() >= 0) 1 else -1)
                    val x = Vector(1, 0, 0)
                    x.rotateAroundY(xzAngle.toDouble())

                    val plusList: MutableList<Vector> = ArrayList()
                    var angle = 0
                    while (angle <= 360) {
                        plusList.add(x.clone().rotateAroundAxis(direction, angle.toDouble()).normalize())
                        angle += 15
                    }

                    // 動作処理
                    val damageTargets: MutableSet<Player> = HashSet()
                    val damage = 9.5
                    for (i in 1..<positions.size) {
                        if (c % 2 == 0) {
                            if (i % 2 != 0) {
                                continue
                            }
                        }
                        if (c % 2 != 0) {
                            if (i % 2 == 0) {
                                continue
                            }
                        }

                        var r = 1

                        if (i == 2) r = 1
                        if (i == 3) r = 3
                        if (i >= 4) r = 5

                        val position = positions[i].toLocation(objectLoc.world!!)

                        for (plus in plusList) {
                            val eloc = position.clone().add(plus.clone().multiply(r))
                            for (target in AsyncThreadManager.onlinePlayers) {
                                if (p.world !== target!!.world) continue
                                if (eloc.distanceSquared(target.location) < Sclat.particleRenderDistanceSquared) {
                                    val targetData = getPlayerData(target)
                                    if (targetData == null) continue
                                    if (targetData.settings!!.showEffectSPWeaponRegion()) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                playerData.team!!.teamColor!!.bukkitColor!!,
                                                (if (c <= 3) 1 else 2).toFloat(),
                                            )
                                        target.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            eloc,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            30.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }

                        // 音
                        if (i > 5 && i % 5 == 0) {
                            if (c <= 3) {
                                playSound(position, Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.5f)
                            } else {
                                playSound(position, Sound.ENTITY_WITHER_SHOOT, 0.3f, 0.6f)
                            }
                        }

                        // 画面エフェクト
                        val maxDistSquared = 25.0 // 5^2
                        // List<Player> list = new ArrayList<>();
                        if (i > 5) {
                            for (target in AsyncThreadManager.onlinePlayers) {
                                val targetData = getPlayerData(target)
                                if (targetData == null) continue
                                if (!targetData.isInMatch) continue
                                if (target!!.world !== p.world) continue
                                if (targetData.team == playerData.team) continue
                                if (target
                                        .location
                                        .distanceSquared(position.clone().add(0.0, 1.0, 0.0)) <= maxDistSquared
                                ) {
                                    // list.add(target);
                                    SclatUtil.sendWorldBorderWarningPacket(target)
                                } else {
                                    SclatUtil.sendWorldBorderWarningClearPacket(target)
                                }
                            }
                            // ここは上のループに含ませちゃってもいいのか...?
//                        /*
//                         * for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) { if
//                         * (list.contains(target)) Sclat.sendWorldBorderWarningPacket(target); else
//                         * Sclat.sendWorldBorderWarningClearPacket(target); }
//                         *
//                         */
                        }

                        // 攻撃判定
                        if (i > 5 && c > 3) {
                            for (target in AsyncThreadManager.onlinePlayers) {
                                val targetData = getPlayerData(target)
                                if (targetData == null) continue
                                if (!targetData.isInMatch) continue
                                if (target!!.world !== p.world) continue
                                if (target
                                        .location
                                        .distanceSquared(position.clone().add(0.0, -1.0, 0.0)) <= maxDistSquared
                                ) {
                                    if (playerData.team != targetData.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        if (targetData.armor > 10000.0 && target.gameMode != GameMode.SPECTATOR) {
                                            target.velocity = Vector(direction.getX(), 0.0, direction.getZ()).multiply(2.0)
                                            target.world.playSound(
                                                target.location,
                                                Sound.ENTITY_SPLASH_POTION_BREAK,
                                                1f,
                                                1.5f,
                                            )
                                        }

                                        damageTargets.add(target)

                                        // AntiNoDamageTime
                                        val task: BukkitRunnable =
                                            object : BukkitRunnable() {
                                                var p: Player? = target

                                                override fun run() {
                                                    target.noDamageTicks = 0
                                                }
                                            }
                                        task.runTaskLater(plugin, 1)
                                    }
                                }
                            }

                            sync {
                                for (`as` in player.world.entities) {
                                    if (`as` is ArmorStand &&
                                        `as`
                                            .location
                                            .distanceSquared(position.clone().add(0.0, -1.0, 0.0)) <= maxDistSquared
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    }
                                }
                            }
                        }
                    }

                    sync {
                        for (target in damageTargets) {
                            giveDamage(p, target, damage, "spWeapon")
                        }
                    }

                    c++
                }
            }
        task.runTaskTimer(0, 10)
    }
}
