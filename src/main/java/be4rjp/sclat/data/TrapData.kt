package be4rjp.sclat.data

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.repelBarrier
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.Sphere.getXZCircle
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class TrapData(
    private val location: Location,
    private val player: Player,
    private val team: Team?,
    number: Int,
) {
    private val task: BukkitRunnable
    private val effect: BukkitRunnable

    var number: Int = 0
        private set
    private var near = false

    init {
        this.number = number

        this.effect =
            object : BukkitRunnable() {
                override fun run() {
                    val sLocs: MutableList<Location> = getXZCircle(location.clone().add(0.0, 1.0, 0.0), 3.0, 2.0, 40)
                    for (oPlayer in plugin.getServer().getOnlinePlayers()) {
                        if (DataMgr.getPlayerData(oPlayer)?.settings?.ShowEffect_Bomb()!!) {
                            for (loc in sLocs) {
                                if (oPlayer.getWorld() === loc.getWorld()) {
                                    if (oPlayer.getLocation().distanceSquared(loc) < Sclat.particleRenderDistanceSquared &&
                                        (DataMgr.getPlayerData(oPlayer)?.team!! == team || near)
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                if (near) {
                                                    DataMgr
                                                        .getPlayerData(player)
                                                        ?.team
                                                        ?.teamColor
                                                        ?.bukkitColor!!
                                                } else {
                                                    Color.BLACK
                                                },
                                                1f,
                                            )
                                        oPlayer.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            loc,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            5.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        effect.runTaskTimer(plugin, 0, 5)

        this.task =
            object : BukkitRunnable() {
                override fun run() {
                    val block = location.getBlock()
                    if (DataMgr.blockDataMap.containsKey(block)) {
                        val pdata = DataMgr.getPaintDataFromBlock(block)
                        if (team != pdata?.team!!) explosion()
                    }

                    if (number + 2 < DataMgr.getPlayerData(player)?.trapCount!!) explosion()

                    for (target in plugin.getServer().getOnlinePlayers()) {
                        if (!DataMgr
                                .getPlayerData(target)
                                ?.isInMatch()!! || target.getWorld() !== location.getWorld()
                        ) {
                            continue
                        }
                        if (target.getGameMode() == GameMode.SPECTATOR) continue
                        if (target.getLocation().distance(location) <= 3 && DataMgr.getPlayerData(target)?.team!! != team) {
                            explosion()
                        }
                    }

                    for (`as` in player.getWorld().getEntities()) {
                        if (`as` is ArmorStand && `as`.getLocation().distanceSquared(location) <= 9) { // 3^2
                            if (`as`.getCustomName() != null) {
                                if (`as`.getCustomName() == null) continue
                                if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") && (`as`.getCustomName() != "100") &&
                                    (`as`.getCustomName() != "SplashShield") &&
                                    (`as`.getCustomName() != "Kasa")
                                ) {
                                    explosion()
                                }
                            }
                        }
                    }

                    if (!DataMgr.getPlayerData(player)?.isInMatch!! || !player.isOnline()) {
                        task.cancel()
                        effect.cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }

    fun explosion() {
        near = true
        task.cancel()

        val ex: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    if (i >= 0 && i <= 4) {
                        if (i % 2 == 0) player.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 1.8f)
                    }

                    if (i == 20) {
                        // 半径
                        val maxDist = 4.0
                        val maxDistSquared = 16.0 // 4^2

                        // 爆発音
                        player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(location, maxDist, 15, player)

                        // バリアをはじく
                        repelBarrier(location, maxDist, player)

                        // センサーエフェクト
                        val sLocs = getSphere(location, maxDist + 1, 25)
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (DataMgr.getPlayerData(o_player)?.settings?.ShowEffect_BombEx()!!) {
                                for (loc in sLocs) {
                                    if (o_player.getWorld() === loc.getWorld()) {
                                        if (o_player
                                                .getLocation()
                                                .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                        ) {
                                            val dustOptions = Particle.DustOptions(Color.BLACK, 1f)
                                            o_player.spawnParticle<Particle.DustOptions?>(
                                                Particle.REDSTONE,
                                                loc,
                                                1,
                                                0.0,
                                                0.0,
                                                0.0,
                                                1.0,
                                                dustOptions,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 塗る
                        run {
                            var i = 0
                            while (i <= maxDist) {
                                val pLocs = getSphere(location, i.toDouble(), 20)
                                for (loc in pLocs) {
                                    PaintMgr.Paint(loc, player, false)
                                }
                                i++
                            }
                        }

                        // 発光効果
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!DataMgr
                                    .getPlayerData(target)
                                    ?.isInMatch()!! || target.getWorld() !== player.getWorld()
                            ) {
                                continue
                            }
                            if (target.getLocation().distance(location) <= maxDist + 1) {
                                if (DataMgr.getPlayerData(player)?.team!!.iD !=
                                    DataMgr
                                        .getPlayerData(target)
                                        ?.team!!
                                        .iD
                                ) {
                                    target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
                                }
                            }
                        }

                        for (`as` in player.getWorld().getEntities()) {
                            if (`as` is ArmorStand && `as`.getLocation().distance(location) <= maxDist + 1) {
                                if (`as`.getCustomName() != null) {
                                    if ((`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                        (`as`.getCustomName() != "100") &&
                                        (`as`.getCustomName() != "SplashShield") &&
                                        (`as`.getCustomName() != "Kasa")
                                    ) {
                                        `as`
                                            .addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 1))
                                    }
                                }
                            }
                        }

                        // 攻撃判定の処理
                        for (`as` in player.getWorld().getEntities()) {
                            if (`as` is ArmorStand) {
                                if (`as`.getLocation().distanceSquared(location) <= maxDistSquared) {
                                    if (`as`.getCustomName() != null) {
                                        try {
                                            if (`as`.getCustomName() == "Kasa") {
                                                val kasaData = DataMgr.getKasaDataFromArmorStand(`as`)
                                                if (DataMgr.getPlayerData(kasaData?.player!!)?.team!! !=
                                                    DataMgr
                                                        .getPlayerData(
                                                            player,
                                                        )?.team!!
                                                ) {
                                                    cancel()
                                                }
                                            } else if (`as`.getCustomName() == "SplashShield") {
                                                val splashShieldData = DataMgr.getSplashShieldDataFromArmorStand(`as`)
                                                if (DataMgr.getPlayerData(splashShieldData?.player!!)?.team!! !=
                                                    DataMgr
                                                        .getPlayerData(
                                                            player,
                                                        )?.team!!
                                                ) {
                                                    cancel()
                                                }
                                            }
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            }
                        }

                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!DataMgr
                                    .getPlayerData(target)
                                    ?.isInMatch()!! || target.getWorld() !== player.getWorld()
                            ) {
                                continue
                            }
                            if (target.getLocation().distanceSquared(location) <= maxDistSquared) {
                                val damage = (
                                    (maxDist - target.getLocation().distance(location)) * 5.0 *
                                        Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                    )
                                if (DataMgr.getPlayerData(player)?.team!! != DataMgr.getPlayerData(target)?.team!! &&
                                    target.getGameMode() == GameMode.ADVENTURE
                                ) {
                                    giveDamage(player, target, damage, "subWeapon")

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

                        for (`as` in player.getWorld().getEntities()) {
                            if (`as` is ArmorStand) {
                                if (`as`.getLocation().distanceSquared(location) <= maxDistSquared) {
                                    val damage = (
                                        (maxDist - `as`.getLocation().distance(location)) * 2.5 *
                                            Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
                                        )
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    if (`as`.getCustomName() != null) {
                                        if (`as`.getCustomName() == "SplashShield" || `as`.getCustomName() == "Kasa") break
                                    }
                                }
                            }
                        }

                        effect.cancel()
                        cancel()
                    }
                    i++
                }
            }
        ex.runTaskTimer(plugin, 0, 1)
    }

    fun addNumber() {
        this.number++
    }
}
